/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.endpoint.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;

/**
 * 
 *
 */
public final class DynamicClientFactory {

    private static final Logger LOG = Logger.getLogger(DynamicClientFactory.class.getName());
    
    private Bus bus;

    private String tmpdir = System.getProperty("java.io.tmpdir");

    private DynamicClientFactory(Bus bus) {
        this.bus = bus;
    }

    public void setTemporaryDirectory(String dir) {
        tmpdir = dir;
    }

    /**
     * Create a new instance using a specific <tt>Bus</tt>.
     * 
     * @param b the <tt>Bus</tt> to use in subsequent operations with the
     *            instance
     * @return the new instance
     */
    public static DynamicClientFactory newInstance(Bus b) {
        return new DynamicClientFactory(b);
    }

    /**
     * Create a new instance using a default <tt>Bus</tt>.
     * 
     * @return the new instance
     * @see CXFBusFactory#getDefaultBus()
     */
    public static DynamicClientFactory newInstance() {
        Bus bus = CXFBusFactory.getDefaultBus();
        return new DynamicClientFactory(bus);
    }

    /**
     * Create a new <code>Client</code> instance using the WSDL to be loaded
     * from the specified URL and using the current classloading context.
     * 
     * @param wsdlURL the URL to load
     * @return
     */
    public Client createClient(String wsdlUrl) {
        return createClient(wsdlUrl, (QName)null, (QName)null);
    }

    /**
     * Create a new <code>Client</code> instance using the WSDL to be loaded
     * from the specified URL and with the specified <code>ClassLoader</code>
     * as parent.
     * 
     * @param wsdlUrl
     * @param classLoader
     * @return
     */
    public Client createClient(String wsdlUrl, ClassLoader classLoader) {
        return createClient(wsdlUrl, null, classLoader, null);
    }

    public Client createClient(String wsdlUrl, QName wsdlEndpoint) {
        return createClient(wsdlUrl, wsdlEndpoint, null);
    }

    public Client createClient(String wsdlUrl, QName wsdlEndpoint, QName port) {
        return createClient(wsdlUrl, wsdlEndpoint, Thread.currentThread().getContextClassLoader(), port);
    }

    public Client createClient(String wsdlUrl, QName wsdlEndpoint, ClassLoader classLoader, QName port) {
        URL u = composeUrl(wsdlUrl);
        // <<null>> could be passed, but that exploits knowledge of the way that
        // constructors
        // on WSDLServiceFactory are implemented, which we don't really know.
        // Wink wink.
        WSDLServiceFactory sf = (wsdlEndpoint == null)
            ? (new WSDLServiceFactory(bus, u)) : (new WSDLServiceFactory(bus, u, wsdlEndpoint));
        Service svc = sf.create();
        Collection<SchemaInfo> schemas = svc.getServiceInfo().getSchemas();

        SchemaCompiler compiler = XJC.createSchemaCompiler();
        ErrorListener elForRun = new InnerErrorListener(wsdlUrl);
        compiler.setErrorListener(elForRun);

        for (SchemaInfo schema : schemas) {
            Element el = schema.getElement();
            compiler.parseSchema(wsdlUrl, el);
        }
        
        S2JJAXBModel intermediateModel = compiler.bind();
        JCodeModel codeModel = intermediateModel.generateCode(null, elForRun);
        StringBuilder sb = new StringBuilder();
        boolean firstnt = false;
        
        for (Iterator<JPackage> packages = codeModel.packages(); packages.hasNext();) {
            JPackage packadge = packages.next();
            String name = packadge.name();
            if ("org.w3._2001.xmlschema".equals(name)) {
                continue;
            }
            if (firstnt) {
                sb.append(':');
            } else {
                firstnt = true;
            }
            sb.append(packadge.name());
        }
        
        String packageList = sb.toString();

        // our hashcode + timestamp ought to be enough.
        String stem = toString() + "-" + System.currentTimeMillis();
        File src = new File(tmpdir, stem + "-src");
        if (!src.mkdir()) {
            throw new IllegalStateException("Unable to create working directory " + src.getPath());
        }

        try {
            FileCodeWriter writer = new FileCodeWriter(src);
            codeModel.build(writer);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write generated Java files for schemas: "
                                            + e.getMessage(), e);
        }

        File classes = new File(tmpdir, stem + "-classes");
        if (!classes.mkdir()) {
            throw new IllegalStateException("Unable to create working directory " + src.getPath());
        }
        src.deleteOnExit();

        Project project = new Project();
        project.setBaseDir(new File(tmpdir));

        Javac javac = new Javac();
        javac.setProject(project);

        Path srcPath = new Path(project);
        DirSet dirSet = new DirSet();
        dirSet.setFile(src);
        srcPath.addDirset(dirSet);
        javac.setSrcdir(srcPath);
        javac.setDestdir(classes);
        javac.setTarget("1.5");
        javac.execute();

        URLClassLoader cl;
        try {
            cl = new URLClassLoader(new URL[] {classes.toURI().toURL()}, classLoader);
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Internal error; a directory returns a malformed URL: "
                                            + mue.getMessage(), mue);
        }

        JAXBContext context;

        try {
            context = JAXBContext.newInstance(packageList, cl);
        } catch (JAXBException jbe) {
            throw new IllegalStateException("Unable to create JAXBContext for generated packages: "
                                            + jbe.getMessage(), jbe);
        }

        JAXBDataBinding databinding = new JAXBDataBinding();
        databinding.setContext(context);
        svc.setDataBinding(databinding);

        ServiceInfo svcfo = svc.getServiceInfo();

        // Setup the new classloader!
        Thread.currentThread().setContextClassLoader(cl);

        TypeClassInitializer visitor = new TypeClassInitializer(svcfo, intermediateModel);
        visitor.walk();

        EndpointInfo epfo = findEndpoint(svcfo, port);

        EndpointImpl ep;
        try {
            ep = new EndpointImpl(bus, svc, epfo);
        } catch (EndpointException epex) {
            throw new IllegalStateException("Unable to create endpoint: " + epex.getMessage(), epex);
        }

        return new ClientImpl(bus, ep);
    }

    private EndpointInfo findEndpoint(ServiceInfo svcfo, QName port) {
        EndpointInfo epfo;
        if (port != null) {
            epfo = svcfo.getEndpoint(port);
            if (epfo == null) {
                throw new IllegalArgumentException("The service " + svcfo.getName()
                                                   + " does not have an endpoint " + port + ".");
            }
        } else {
            epfo = null;
            for (EndpointInfo e : svcfo.getEndpoints()) {
                BindingInfo bfo = e.getBinding();

                if (bfo.getBindingId().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
                    for (Object o : bfo.getExtensors().get()) {
                        if (o instanceof SOAPBindingImpl) {
                            SOAPBindingImpl soapB = (SOAPBindingImpl)o;
                            if (soapB.getTransportURI().equals("http://schemas.xmlsoap.org/soap/http")) {
                                epfo = e;
                                break;
                            }
                        }
                    }

                }
            }
            if (epfo == null) {
                throw new UnsupportedOperationException(
                     "Only document-style SOAP 1.1 http are supported "
                     + "for auto-selection of endpoint; none were found.");
            }
        }
        return epfo;
    }

    private URL composeUrl(String s) {
        try {
            URIResolver resolver = new URIResolver(null, s, getClass());
            
            if (resolver.isResolved()) {
                return resolver.getURI().toURL();
            } else {
                throw new ServiceConstructionException(new Message("COULD_NOT_RESOLVE_URL", LOG, s));
            }
        } catch (IOException e) {
            throw new ServiceConstructionException(new Message("COULD_NOT_RESOLVE_URL", LOG, s), e);
        }
    }

    private class InnerErrorListener implements ErrorListener {

        private String url;

        InnerErrorListener(String url) {
            this.url = url;
        }

        public void error(SAXParseException arg0) {
            throw new RuntimeException("Error compiling schema from WSDL at {" + url + "}: "
                                       + arg0.getMessage(), arg0);
        }

        public void fatalError(SAXParseException arg0) {
            throw new RuntimeException("Fatal error compiling schema from WSDL at {" + url + "}: "
                                       + arg0.getMessage(), arg0);
        }

        public void info(SAXParseException arg0) {
            // ignore
        }

        public void warning(SAXParseException arg0) {
            // ignore
        }
    }

    // sorry, but yuck. Try a file first?!?
    static class RelativeEntityResolver implements EntityResolver {
        private String baseURI;

        public RelativeEntityResolver(String baseURI) {
            super();
            this.baseURI = baseURI;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            // the system id is null if the entity is in the wsdl.
            if (systemId != null) {
                File file = new File(baseURI, systemId);
                if (file.exists()) {
                    return new InputSource(new FileInputStream(file));
                } else {
                    return new InputSource(systemId);
                }
            }
            return null;
        }
    }
}
