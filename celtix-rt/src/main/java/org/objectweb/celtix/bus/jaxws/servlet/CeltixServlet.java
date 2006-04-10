package org.objectweb.celtix.bus.jaxws.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class CeltixServlet extends HttpServlet {
    static final String HTTP_REQUEST =
        CeltixServlet.class.getName() + ".REQUEST";
    static final String HTTP_RESPONSE =
        CeltixServlet.class.getName() + ".RESPONSE";
    
    static final Map<String, WeakReference<Bus>> BUS_MAP = new Hashtable<String, WeakReference<Bus>>();
    
    protected Bus bus;
    protected Map<String, ServletServerTransport> servantMap 
        = new HashMap<String, ServletServerTransport>();

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        List<String> list = new ArrayList<String>();
        String busid = servletConfig.getInitParameter("bus.id");
        if (null != busid) {
            list.add("-BUSid");
            list.add(busid);
            WeakReference<Bus> ref = BUS_MAP.get(busid);
            if (null != ref) {
                bus = ref.get();
            }
        }
        try {
            if (null == bus) {
                bus = Bus.init(list.toArray(new String[list.size()]));
                
                TransportFactory factory = new ServletTransportFactory(this);
                factory.init(bus);
                registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/");
                registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/http");
                registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/http/");
                registerTransport(factory, "http://celtix.objectweb.org/bindings/xmlformat");
                registerTransport(factory, "http://celtix.objectweb.org/transports/http/configuration");
            }
        } catch (BusException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        if (null != busid) {
            BUS_MAP.put(busid, new WeakReference<Bus>(bus));
        }

        InputStream ins = servletConfig.getServletContext()
            .getResourceAsStream("/WEB-INF/celtix-servlet.xml");
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

    private void registerTransport(TransportFactory factory, String namespace) throws BusException {
        this.bus.getTransportFactoryManager().registerTransportFactory(namespace,
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
            EndpointReferenceType ref;
            if (url != null) {
                ref = EndpointReferenceUtils
                    .getEndpointReference(url,
                                      QName.valueOf(serviceName),
                                      portName);
            } else {
                ref = 
                    EndpointReferenceUtils.getEndpointReference(bus.getWSDLManager(),
                                                            impl);
            }
            EndpointImpl ep = new EndpointImpl(bus, impl, null, ref);
            
            //doesn't really matter what URL is used here
            ep.publish("http://localhost" + (urlPat.charAt(0) == '/' ? "" : "/") + urlPat); 
        } catch (ClassNotFoundException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }    
    }

    public void loadEndpoint(ServletConfig servletConfig, Node node) {
        Element el = (Element)node;
        String implName = el.getAttribute("implementation");
        String serviceName = el.getAttribute("service");
        String wsdlName = el.getAttribute("wsdl");
        String portName = el.getAttribute("port");
        String urlPat = el.getAttribute("url-pattern");
        /*
        String intfName = el.getAttribute("interface");
        String name = el.getAttribute("name");
        */

        loadEndpoint(implName, serviceName, wsdlName, portName, urlPat);
    }

    public void destroy() {
        try {
            String s = bus.getBusID();
            BUS_MAP.remove(s);
            
            bus.shutdown(true);
        } catch (BusException ex) {
            ex.printStackTrace();
            //ignore
        }
    }
    
    void addServant(URL url, ServletServerTransport servant) {
        servantMap.put(url.getPath(), servant);
    }
    void removeServant(URL url, ServletServerTransport servant) {
        servantMap.remove(url.getPath());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        ServletServerTransport tp = servantMap.get(request.getPathInfo());
        if (tp == null) {
            throw new ServletException("Unknown servlet mapping " + request.getPathInfo());
        }
        try {
            tp.doPost(request, response);
        } catch (IOException ex) {
            throw new ServletException(ex.getMessage());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        ServletServerTransport tp = servantMap.get(request.getPathInfo());
        if (tp == null) {
            throw new ServletException("Unknown servlet mapping " + request.getPathInfo());
        }
        try {
            tp.doGet(request, response);
        } catch (IOException ex) {
            throw new ServletException(ex.getMessage());
        }
    }

}
