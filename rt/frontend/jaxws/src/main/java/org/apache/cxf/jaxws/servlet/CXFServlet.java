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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;


public class CXFServlet extends HttpServlet {
    static final String HTTP_REQUEST =
        CXFServlet.class.getName() + ".REQUEST";
    static final String HTTP_RESPONSE =
        CXFServlet.class.getName() + ".RESPONSE";
    
    static final Map<String, WeakReference<Bus>> BUS_MAP = new Hashtable<String, WeakReference<Bus>>();
    
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
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (ParserConfigurationException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
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
                             String urlPat) {

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
            JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(impl.getClass());
            // build up the Service model
            JaxWsServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean(implInfo);
            serviceFactory.setBus(bus);
            serviceFactory.setServiceClass(impl.getClass());
            Service service = serviceFactory.create();

            // create the endpoint        
            QName endpointName = implInfo.getEndpointName();
            ei = service.getServiceInfo().getEndpoint(endpointName);

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }    
    }

    private void replaceDestionFactory() {
        DestinationFactory factory = createServletTransportFactory();
        deregisterTransport("http://schemas.xmlsoap.org/wsdl/soap/");
        deregisterTransport("http://schemas.xmlsoap.org/wsdl/soap/http");
        deregisterTransport("http://schemas.xmlsoap.org/wsdl/http/");
        deregisterTransport("http://celtix.objectweb.org/bindings/xmlformat");
        deregisterTransport("http://celtix.objectweb.org/transports/http/configuration");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/http");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/http/");
        registerTransport(factory, "http://celtix.objectweb.org/bindings/xmlformat");
        registerTransport(factory, "http://celtix.objectweb.org/transports/http/configuration");
    }

    public void loadEndpoint(ServletConfig servletConfig, Node node) {
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
            ((ServletDestination)ep.getServer().getDestination()).doService(request, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
      
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            ((ServletDestination)servletTransportFactory.
                getDestination(ei)).doService(request, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
