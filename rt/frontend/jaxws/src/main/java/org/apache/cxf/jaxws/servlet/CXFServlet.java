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
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.xmlsoap.schemas.wsdl.http.AddressType;


public class CXFServlet extends HttpServlet {
    
    
    static final String HTTP_REQUEST =
        "HTTP_SERVLET_REQUEST";
    static final String HTTP_RESPONSE =
        "HTTP_SERVLET_RESPONSE";
    
    static final Map<String, WeakReference<Bus>> BUS_MAP = new Hashtable<String, WeakReference<Bus>>();
    static final Logger LOG = Logger.getLogger(CXFServlet.class.getName());
    protected Bus bus;
    protected Map<String, ServletDestination> servantMap 
        = new HashMap<String, ServletDestination>();
    
    
    EndpointReferenceType reference;
    ServletTransportFactory servletTransportFactory;
    EndpointImpl ep;
    EndpointInfo ei;
    
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
            bus = BusFactoryHelper.newInstance().createBus();
        }
        if (null != busid) {
            BUS_MAP.put(busid, new WeakReference<Bus>(bus));
        }

        InputStream ins = servletConfig.getServletContext()
            .getResourceAsStream("/WEB-INF/cxf-servlet.xml");
        if (ins != null) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            
            
            try {
                Document doc = builderFactory.newDocumentBuilder().parse(ins);
                Node nd = doc.getDocumentElement().getFirstChild();
                while (nd != null) {
                    if ("endpoint".equals(nd.getLocalName())) {
                        loadEndpoint(servletConfig, nd);
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

    private void deregisterTransport(String transportId) {
        bus.getExtension(DestinationFactoryManager.class).deregisterDestinationFactory(transportId);        
    }

    /**
     * @return
     */
    protected DestinationFactory createServletTransportFactory() {
        if (servletTransportFactory == null) {
            servletTransportFactory = new ServletTransportFactory(bus, reference);
        }
        return servletTransportFactory;
    }

    private void registerTransport(DestinationFactory factory, String namespace) {
        bus.getExtension(DestinationFactoryManager.class).registerDestinationFactory(
                                                                  namespace,
                                                                  factory);
    }

    public void loadEndpoint(String implName,
                             String serviceName,
                             String wsdlName,
                             String portName,
                             String urlPat) throws ServletException {

        try {
            
            URL url = null;
            if (wsdlName != null) {
                try {
                    url = getServletConfig().getServletContext().getResource(wsdlName);
                } catch (MalformedURLException ex) {
                    try {
                        url = new URL(wsdlName);
                    } catch (MalformedURLException ex2) {
                        try {
                            url = getServletConfig().getServletContext().getResource("/" + wsdlName);
                        } catch (MalformedURLException ex3) {
                            url = null;
                        }
                    }
                }
            }
            Class cls = Class.forName(implName, false, Thread.currentThread().getContextClassLoader());
            Object impl = cls.newInstance();
            reference = EndpointReferenceUtils
                    .getEndpointReference(url,
                                      QName.valueOf(serviceName),
                                      portName);
            

            
            ep = new EndpointImpl(bus, impl, url.toString());
            replaceDestionFactory();
//          doesn't really matter what URL is used here
            ep.publish("http://localhost" + (urlPat.charAt(0) == '/' ? "" : "/") + urlPat);
            
            ei = ep.getServer().getEndpoint().getEndpointInfo();
            
            

        } catch (ClassNotFoundException ex) {
            throw new ServletException(ex);
        } catch (InstantiationException ex) {
            throw new ServletException(ex);
        } catch (IllegalAccessException ex) {
            throw new ServletException(ex);
        }    
    }

    private void replaceDestionFactory() {
        DestinationFactory factory = createServletTransportFactory();
        

        deregisterTransport("http://schemas.xmlsoap.org/wsdl/soap/http");
        deregisterTransport("http://schemas.xmlsoap.org/soap/http");
        deregisterTransport("http://www.w3.org/2003/05/soap/bindings/HTTP/");
        deregisterTransport("http://schemas.xmlsoap.org/wsdl/http/");
        deregisterTransport("http://cxf.apache.org/transports/http/configuration");
        deregisterTransport("http://cxf.apache.org/bindings/xformat");
        
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/http");
        registerTransport(factory, "http://schemas.xmlsoap.org/soap/http");
        registerTransport(factory, "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/http/");
        registerTransport(factory, "http://cxf.apache.org/transports/http/configuration");
        registerTransport(factory, "http://cxf.apache.org/bindings/xformat");
    }

    public void loadEndpoint(ServletConfig servletConfig, Node node) 
        throws ServletException {
        Element el = (Element)node;
        String implName = el.getAttribute("implementation");
        String serviceName = el.getAttribute("service");
        String wsdlName = el.getAttribute("wsdl");
        String portName = el.getAttribute("port");
        String urlPat = el.getAttribute("url-pattern");
        
        loadEndpoint(implName, serviceName, wsdlName, portName, urlPat);
    }

    public void destroy() {
        String s = bus.getId();
        BUS_MAP.remove(s);
        
        bus.shutdown(true);
    }
    
    void addServant(URL url, ServletDestination servant) {
        servantMap.put(url.getPath(), servant);
    }
    void removeServant(URL url, ServletDestination servant) {
        servantMap.remove(url.getPath());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Service http request on thread: " + Thread.currentThread());
            }
            
            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, request.getInputStream());
            inMessage.put(HTTP_REQUEST, request);
            inMessage.put(HTTP_RESPONSE, response);
            inMessage.put(Message.HTTP_REQUEST_METHOD, request.getMethod());
            inMessage.put(Message.PATH_INFO, request.getPathInfo());
            inMessage.put(Message.QUERY_STRING, request.getQueryString());
            
            ((ServletDestination)ep.getServer().getDestination()).doMessage(inMessage);
        } catch (IOException e) {
            throw new ServletException(e);
        }
      
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            
            
            response.setHeader("Content-Type", "text/xml");
            
            OutputStream os = response.getOutputStream();
            
            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            Definition def = new ServiceWSDLBuilder(ei.getService()).build();
            Port port = def.getService(ei.getService().getName()).getPort(
                                       ei.getName().getLocalPart());
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                if (SOAPBindingUtil.isSOAPAddress(el)) {
                    SoapAddress add = SOAPBindingUtil.getSoapAddress(el);
                    add.setLocationURI(request.getRequestURL().toString());
                }
                if (el instanceof AddressType) {
                    AddressType add = (AddressType)el;
                    add.setLocation(request.getRequestURL().toString());
                }
            }
            
            wsdlWriter.writeWSDL(def, os);
            response.getOutputStream().flush();
            return;
        } catch (Exception ex) {
            
            throw new ServletException(ex);
        }
    }

}
