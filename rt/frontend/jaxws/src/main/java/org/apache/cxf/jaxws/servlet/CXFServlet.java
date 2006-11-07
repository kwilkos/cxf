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

package org.apache.cxf.jaxws.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.springframework.context.ApplicationContext;

/**
 * A Servlet which supports loading of JAX-WS endpoints from an
 * XML file and handling requests for endpoints created via other means
 * such as Spring beans, or the Java API. All requests are passed on
 * to the {@link ServletController}.
 *
 */
public class CXFServlet extends HttpServlet {

    static final Map<String, WeakReference<Bus>> BUS_MAP = new Hashtable<String, WeakReference<Bus>>();
    static final Logger LOG = Logger.getLogger(CXFServlet.class.getName());
    protected Bus bus;

    private ServletTransportFactory servletTransportFactory;
    private ServletController controller;

    public ServletController createServletController() {
        return new ServletController(servletTransportFactory);
    }

    public ServletController getController() {
        return controller;
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String busid = servletConfig.getInitParameter("bus.id");
        if (null != busid) {
            WeakReference<Bus> ref = BUS_MAP.get(busid);
            if (null != ref) {
                bus = ref.get();
            }
        }
        if (null == bus) {
            // try to pull an existing ApplicationContext out of the
            // ServletContext
            ServletContext svCtx = getServletContext();
            
            // Spring 1.x
            ApplicationContext ctx = (ApplicationContext)svCtx
                .getAttribute("interface org.springframework.web.context.WebApplicationContext.ROOT");

            // Spring 2.0
            if (ctx == null) {
                ctx = (ApplicationContext)svCtx
                    .getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
            }
            
            // This constructor works whether there is a context or not
            bus = new SpringBusFactory(ctx).getDefaultBus();
        }
        if (null != busid) {
            BUS_MAP.put(busid, new WeakReference<Bus>(bus));
        }

        replaceDestionFactory();

        // Set up the servlet as the default server side destination factory
        controller = createServletController();

        // build endpoints from the web.xml or a config file
        buildEndpoints(servletConfig);
    }

    protected void buildEndpoints(ServletConfig servletConfig) throws ServletException {
        String location = servletConfig.getInitParameter("config-location");
        if (location == null) {
            location = "/WEB-INF/cxf-servlet.xml";
        }
        InputStream ins = servletConfig.getServletContext().getResourceAsStream(location);

        if (ins == null) {
            try {
                URIResolver resolver = new URIResolver(location);

                if (resolver.isResolved()) {
                    ins = resolver.getInputStream();
                }
            } catch (IOException e) {
                // ignore
            }

        }

        if (ins != null) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);

            try {
                Document doc = builderFactory.newDocumentBuilder().parse(ins);
                Node nd = doc.getDocumentElement().getFirstChild();
                while (nd != null) {
                    if ("endpoint".equals(nd.getLocalName())) {
                        buildEndpoint(servletConfig, nd);
                    }
                    nd = nd.getNextSibling();
                }
            } catch (SAXException ex) {
                throw new ServletException(ex);
            } catch (IOException ex) {
                throw new ServletException(ex);
            } catch (ParserConfigurationException ex) {
                throw new ServletException(ex);
            }
        }
    }

    /**
     * @return
     */
    protected DestinationFactory createServletTransportFactory() {
        if (servletTransportFactory == null) {
            servletTransportFactory = new ServletTransportFactory(bus);
        }
        return servletTransportFactory;
    }

    private void registerTransport(DestinationFactory factory, String namespace) {
        bus.getExtension(DestinationFactoryManager.class).registerDestinationFactory(namespace, factory);
    }

    public void buildEndpoint(ServletConfig servletConfig, Node node) throws ServletException {
        Element el = (Element)node;
        String implName = el.getAttribute("implementation");
        String serviceName = el.getAttribute("service");
        String wsdlName = el.getAttribute("wsdl");
        String portName = el.getAttribute("port");
        String urlPat = el.getAttribute("url-pattern");

        buildEndpoint(implName, serviceName, wsdlName, portName, urlPat);
    }

    public void buildEndpoint(String implName, String serviceName, String wsdlName, String portName,
                              String urlPat) throws ServletException {

        try {

            // TODO: This wasn't doing anything before. We need to pass this to
            // the
            // EndpointImpl so the service factory can use it...
            // URL url = null;
            // if (wsdlName != null && wsdlName.length() > 0) {
            // try {
            // url =
            // getServletConfig().getServletContext().getResource(wsdlName);
            // } catch (MalformedURLException ex) {
            // try {
            // url = new URL(wsdlName);
            // } catch (MalformedURLException ex2) {
            // try {
            // url = getServletConfig().getServletContext().getResource("/" +
            // wsdlName);
            // } catch (MalformedURLException ex3) {
            // url = null;
            // }
            // }
            // }
            // }
            Class cls = ClassLoaderUtils.loadClass(implName, getClass());
            Object impl = cls.newInstance();

            EndpointImpl ep = new EndpointImpl(bus, impl, (String)null);

            // doesn't really matter what URL is used here
            ep.publish("http://localhost" + (urlPat.charAt(0) == '/' ? "" : "/") + urlPat);
        } catch (ClassNotFoundException ex) {
            throw new ServletException(ex);
        } catch (InstantiationException ex) {
            throw new ServletException(ex);
        } catch (IllegalAccessException ex) {
            throw new ServletException(ex);
        }
    }

    private void replaceDestionFactory() throws ServletException {
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        try {
            DestinationFactory df = dfm
                .getDestinationFactory("http://cxf.apache.org/transports/http/configuration");
            if (df instanceof ServletTransportFactory) {
                servletTransportFactory = (ServletTransportFactory)df;
                return;
            }
        } catch (BusException e) {
            // why are we throwing a busexception if the DF isn't found?
        }

        DestinationFactory factory = createServletTransportFactory();

        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/http");
        registerTransport(factory, "http://schemas.xmlsoap.org/soap/http");
        registerTransport(factory, "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/http/");
        registerTransport(factory, "http://cxf.apache.org/transports/http/configuration");
        registerTransport(factory, "http://cxf.apache.org/bindings/xformat");
    }

    public void destroy() {
        String s = bus.getId();
        BUS_MAP.remove(s);

        bus.shutdown(true);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        controller.invoke(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        controller.invoke(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        controller.invoke(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        controller.invoke(request, response);
    }
}
