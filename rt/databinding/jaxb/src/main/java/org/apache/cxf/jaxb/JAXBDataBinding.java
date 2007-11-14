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

package org.apache.cxf.jaxb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.xml.bind.v2.ContextFactory;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.CacheMap;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.databinding.source.AbstractDataBinding;
import org.apache.cxf.jaxb.io.DataReaderImpl;
import org.apache.cxf.jaxb.io.DataWriterImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.addressing.ObjectFactory;

public final class JAXBDataBinding extends AbstractDataBinding implements DataBinding {
    public static final String SCHEMA_RESOURCE = "SCHEMRESOURCE";
    
    public static final String UNWRAP_JAXB_ELEMENT = "unwrap.jaxb.element";

    private static final Logger LOG = LogUtils.getLogger(JAXBDataBinding.class);


    private static final Class<?> SUPPORTED_READER_FORMATS[] = new Class<?>[] {Node.class,
                                                                               XMLEventReader.class,
                                                                               XMLStreamReader.class};
    private static final Class<?> SUPPORTED_WRITER_FORMATS[] = new Class<?>[] {OutputStream.class,
                                                                               Node.class,
                                                                               XMLEventWriter.class,
                                                                               XMLStreamWriter.class};

    private static final Map<Set<Class<?>>, JAXBContext> JAXBCONTEXT_CACHE = 
        new CacheMap<Set<Class<?>>, JAXBContext>();

    Class[] extraClass;
    
    JAXBContext context;
    Set<Class<?>> contextClasses;

    Class<?> cls;

    
    public JAXBDataBinding() {
    }
    
    public JAXBDataBinding(Class<?>...classes) throws JAXBException {
        contextClasses = new HashSet<Class<?>>();
        contextClasses.addAll(Arrays.asList(classes));
        setContext(createJAXBContext(contextClasses));
    }

    public JAXBDataBinding(JAXBContext context) {
        this();
        setContext(context);
    }

    public JAXBContext getContext() {
        return context;
    }

    public void setContext(JAXBContext ctx) {
        context = ctx;
    }    
    
    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> c) {
        if (c == XMLStreamWriter.class) {
            return (DataWriter<T>)new DataWriterImpl<XMLStreamWriter>(context);
        } else if (c == OutputStream.class) {
            return (DataWriter<T>)new DataWriterImpl<OutputStream>(context);            
        } else if (c == XMLEventWriter.class) {
            return (DataWriter<T>)new DataWriterImpl<XMLEventWriter>(context);           
        } else if (c == Node.class) {
            return (DataWriter<T>)new DataWriterImpl<Node>(context);      
        }
        
        return null;
    }

    public Class<?>[] getSupportedWriterFormats() {
        return SUPPORTED_WRITER_FORMATS;
    }

    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> c) {
        DataReader<T> dr = null;
        if (c == XMLStreamReader.class) {
            dr = (DataReader<T>)new DataReaderImpl<XMLStreamReader>(context);
        } else if (c == XMLEventReader.class) {
            dr = (DataReader<T>)new DataReaderImpl<XMLEventReader>(context);
        } else if (c == Node.class) {
            dr = (DataReader<T>)new DataReaderImpl<Node>(context);
        }
        
        return dr;
    }

    public Class<?>[] getSupportedReaderFormats() {
        return SUPPORTED_READER_FORMATS;
    }
    
    public void initialize(Service service) {
        //context is already set, don't redo it
        if (context != null) {
            return;
        }
        
        contextClasses = new HashSet<Class<?>>();
        for (ServiceInfo serviceInfo : service.getServiceInfos()) {
            JAXBContextInitializer initializer = 
                new JAXBContextInitializer(serviceInfo, contextClasses);
            initializer.walk();
    
        }
                
        String tns = service.getName().getNamespaceURI(); 
        JAXBContext ctx = null;
        try {           
            if (service.getServiceInfos().size() > 0) {
                tns = service.getServiceInfos().get(0).getInterface().getName().getNamespaceURI();
            }
            ctx = createJAXBContext(contextClasses, tns);
        } catch (JAXBException e1) {
            //load jaxb needed class and try to create jaxb context for more times 
            boolean added = addJaxbObjectFactory(e1);           
            while (ctx == null && added) {
                try {
                      synchronized (JAXBCONTEXT_CACHE) {
                        ctx = JAXBContext.newInstance(contextClasses.toArray(new Class[contextClasses.size()])
                                                      , null);
                        JAXBCONTEXT_CACHE.put(contextClasses, ctx);
                    }               
                } catch (JAXBException e) {
                    e1 = e;
                    added = addJaxbObjectFactory(e1);           
                }
            }
            if (ctx == null) {
                throw new ServiceConstructionException(e1);
            }
        }
            
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "CREATED_JAXB_CONTEXT", new Object[] {ctx, contextClasses});
        }
        setContext(ctx);
        
            
        for (ServiceInfo serviceInfo : service.getServiceInfos()) {
            SchemaCollection col = serviceInfo.getXmlSchemaCollection();

            if (col.getXmlSchemas().length > 1) {
                // someone has already filled in the types
                continue;
            }
    
            Collection<DOMSource> schemas = getSchemas();
            if (schemas != null) {
                for (DOMSource r : schemas) {
                    addSchemaDocument(serviceInfo, col, 
                                      (Document)r.getNode(), r.getSystemId());
                }
            } else {
                try {
                    for (DOMResult r : generateJaxbSchemas()) {
                        addSchemaDocument(serviceInfo, col, 
                                          (Document)r.getNode(), r.getSystemId());
                    }
                } catch (IOException e) {
                    throw new ServiceConstructionException(new Message("SCHEMA_GEN_EXC", LOG), e);
                }
            }
            
            JAXBContextImpl riContext;
            if (context instanceof JAXBContextImpl) {
                riContext = (JAXBContextImpl) context;
            } else {
                // fall back if we're using another jaxb implementation
                try {
                    riContext = (JAXBContextImpl)
                        ContextFactory.createContext(
                            contextClasses.toArray(new Class[contextClasses.size()]), null);
                } catch (JAXBException e) {
                    throw new ServiceConstructionException(e);
                }
            }
            
            JAXBSchemaInitializer schemaInit = new JAXBSchemaInitializer(serviceInfo, col, riContext);
            schemaInit.walk();
        }
    }

    public void setExtraClass(Class[] userExtraClass) {
        extraClass = userExtraClass;
    }
    
    public Class[] getExtraClass() {
        return extraClass;
    }

    private List<DOMResult> generateJaxbSchemas() throws IOException {
        final List<DOMResult> results = new ArrayList<DOMResult>();

        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String ns, String file) throws IOException {
                DOMResult result = new DOMResult();
                result.setSystemId(file);
                // Don't include WS-Addressing bits
                if ("http://www.w3.org/2005/02/addressing/wsdl".equals(ns)) {
                    return result;
                }
                results.add(result);
                return result;
            }
        });

        return results;
    }
    

    public JAXBContext createJAXBContext(Set<Class<?>> classes) throws JAXBException {
        return createJAXBContext(classes, null);
    }
    
    public JAXBContext createJAXBContext(Set<Class<?>> classes,
                                          String defaultNs) throws JAXBException {
        Iterator it = classes.iterator();
        String className = "";
        Object remoteExceptionObject = null;
        while (it.hasNext()) {
            remoteExceptionObject = (Class)it.next();
            className = remoteExceptionObject.toString();
            if (!("".equals(className)) && className.contains("RemoteException")) {
                it.remove();
            }
        }
        // add user extra class into jaxb context
        if (extraClass != null && extraClass.length > 0) {
            for (Class clz : extraClass) {
                classes.add(clz);
            }
        }
        
        for (Class<?> clz : classes) {
            if (clz.getName().endsWith("ObjectFactory")) {
                //kind of a hack, but ObjectFactories may be created with empty namespaces
                defaultNs = null;
            }
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        if (defaultNs != null) {
            map.put("com.sun.xml.bind.defaultNamespaceRemap", defaultNs);
        }
        
        //try and read any jaxb.index files that are with the other classes.  This should 
        //allow loading of extra classes (such as subclasses for inheritance reasons) 
        //that are in the same package.
        Map<String, InputStream> packages = new HashMap<String, InputStream>();
        Map<String, ClassLoader> packageLoaders = new HashMap<String, ClassLoader>();
        for (Class<?> jcls : classes) {
            if (!packages.containsKey(PackageUtils.getPackageName(jcls))) {
                packages.put(PackageUtils.getPackageName(jcls), jcls.getResourceAsStream("jaxb.index"));
                packageLoaders.put(PackageUtils.getPackageName(jcls), jcls.getClassLoader());
            }
        }
        for (Map.Entry<String, InputStream> entry : packages.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(entry.getValue(),
                                                                                     "UTF-8"));
                    String pkg = entry.getKey();
                    ClassLoader loader = packageLoaders.get(pkg);
                    if (!StringUtils.isEmpty(pkg)) {
                        pkg += ".";
                    }
                    
                    String line = reader.readLine();
                    while (line != null) {
                        line = line.trim();
                        if (line.indexOf("#") != -1) {
                            line = line.substring(0, line.indexOf("#"));
                        }
                        if (!StringUtils.isEmpty(line)) {
                            try {
                                Class<?> ncls = Class.forName(pkg + line, false, loader);
                                classes.add(ncls);
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    //ignore
                } finally {
                    try {
                        entry.getValue().close();
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
        }
        
        addWsAddressingTypes(classes);

        synchronized (JAXBCONTEXT_CACHE) {
            if (!JAXBCONTEXT_CACHE.containsKey(classes)) {
                JAXBContext ctx = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]), map);
                JAXBCONTEXT_CACHE.put(classes, ctx);
            }
        }

        return JAXBCONTEXT_CACHE.get(classes);
    }

    private void addWsAddressingTypes(Set<Class<?>> classes) {
        if (classes.contains(ObjectFactory.class)) {
            // ws-addressing is used, lets add the specific types
            try {
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.ObjectFactory"));
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.AttributedQNameType"));
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.ServiceNameType"));
            } catch (ClassNotFoundException unused) {
                // REVISIT - ignorable if WS-ADDRESSING not available?
                // maybe add a way to allow interceptors to add stuff to the
                // context?
            } 
        }
    }
    
    
    //Now we can not add all the classes that Jaxb needed into JaxbContext, especially when 
    //an ObjectFactory is pointed to by an jaxb @XmlElementDecl annotation
    //added this workaround method to load the jaxb needed ObjectFactory class
    public boolean addJaxbObjectFactory(JAXBException e1) {
        boolean added = false;
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        java.io.PrintStream pout = new java.io.PrintStream(bout);
        e1.printStackTrace(pout);
        String str = new String(bout.toByteArray());
        Pattern pattern = Pattern.compile("(?<=There's\\sno\\sObjectFactory\\swith\\san\\s" 
                                          + "@XmlElementDecl\\sfor\\sthe\\selement\\s\\{)\\S*(?=\\})");
        java.util.regex.Matcher  matcher = pattern.matcher(str);
        while (matcher.find()) {               
            String pkgName = JAXBUtils.namespaceURIToPackage(matcher.group());
            try {
                Class clz  = getClass().getClassLoader().loadClass(pkgName + "." + "ObjectFactory");
                
                if (!contextClasses.contains(clz)) {
                    contextClasses.add(clz);
                    added = true;
                }
            } catch (ClassNotFoundException e) {
                //do nothing
            }
            
        }
        return added;
    }

    /**
     * Jaxb has no declared namespace prefixes.
     * {@inheritDoc}
     */
    public Map<String, String> getDeclaredNamespaceMappings() {
        return null;
    }
    
    
}
