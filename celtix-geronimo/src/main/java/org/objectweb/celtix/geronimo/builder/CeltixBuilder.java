package org.objectweb.celtix.geronimo.builder;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;

import com.sun.java.xml.ns.j2ee.PortComponentType;
import com.sun.java.xml.ns.j2ee.WebserviceDescriptionType;
import com.sun.java.xml.ns.j2ee.WebservicesType;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.StoredObject;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.geronimo.container.CeltixWebServiceContainer;
import org.objectweb.celtix.geronimo.container.GeronimoTransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public class CeltixBuilder implements WebServiceBuilder, GBeanLifecycle {

    public static final GBeanInfo GBEAN_INFO;
    static final String WEB_SERVICE_CONTAINER_ATTR = "webServiceContainer";
    private static final String POJO_CLASS_ATTR = "pojoClassName"; 
    private static final Logger LOG = Logger.getLogger(CeltixBuilder.class.getName());
    
    private final Bus bus;
    private JAXBContext ctx;
    private GeronimoTransportFactory factory = new GeronimoTransportFactory();
    private Collection<EndpointImpl> activeEndpoints = new ArrayList<EndpointImpl>();
    
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CeltixBuilder.class, 
                                                                     NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }


    public CeltixBuilder() {
        this(Bus.getCurrent());
    }

    CeltixBuilder(Bus aBus) {
        bus = aBus;
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    
    public Map<String, PortInfo> parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB, 
                                         Map correctedPortLocations)
        throws DeploymentException {
            
        LOG.info("parsing descriptor " + wsDDUrl);
        
        Map<String, PortInfo> map = new HashMap<String, PortInfo>();
        
        try { 
            InputStream in = wsDDUrl.openStream();
            if (in == null) {
                throw new DeploymentException("unable to read descriptor " + wsDDUrl);
            }
            
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            Object obj = unmarshaller.unmarshal(in);
            
            WebservicesType wst = null; 
            if (obj instanceof JAXBElement) {
                wst = (WebservicesType)((JAXBElement)obj).getValue();
            }
            
            for (WebserviceDescriptionType desc : wst.getWebserviceDescription()) {
                final String wsdlFile = desc.getWsdlFile().getValue();
                final String serviceName = desc.getWebserviceDescriptionName().getValue();
                
                for (PortComponentType port : desc.getPortComponent()) {
                    String servlet = port.getServiceImplBean().getServletLink().getValue();
                    String sei = port.getServiceEndpointInterface().getValue();
                    String portName = port.getPortComponentName().getValue();
                    
                    PortInfo portInfo = new PortInfo();
                    
                    portInfo.setServiceName(serviceName);                    
                    portInfo.setServletLink(servlet);
                    portInfo.setServiceEndpointInterfaceName(sei);
                    portInfo.setPortName(portName);
                    portInfo.setWsdlFile(wsdlFile);
                    portInfo.setHandlers(port.getHandler());
 
                    map.put(servlet, portInfo);
                }
            }
            
            return map;
       
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new DeploymentException("unable to read " + wsDDUrl, ex);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new DeploymentException("unable to parse webservices.xml", ex);
        }
    }

    
    public synchronized void configurePOJO(GBeanData targetGBean, JarFile moduleFile, 
                                           Object pi, String implClassName, ClassLoader classLoader)
        throws DeploymentException {

        assert pi instanceof PortInfo : "received incorrect portInfo object";

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        
        try { 
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            PortInfo portInfo = (PortInfo)pi;
            String seiClassName = portInfo.getServiceEndpointInterfaceName();
            
            LOG.info("configuring POJO webservice: " + pi + " sei: " + seiClassName);
            
            Class<?> seiClass = loadSEI(seiClassName, classLoader);
            targetGBean.setAttribute(POJO_CLASS_ATTR, seiClassName); 
            /*List<Handler> handlers =*/ buildHandlerChain(portInfo);
            
            CeltixWebServiceContainer container = new CeltixWebServiceContainer();
            
            URL url = resolveWSDL(portInfo.getWsdlFile(), classLoader); 
            assert url != null; 
            
            EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(url,
                    QName.valueOf(portInfo.getServiceName()),
                    portInfo.getPortName());
            
            factory.setCurrentContainer(container);
            EndpointImpl ep = new EndpointImpl(bus, createDelegatingProxyFor(seiClass), 
                                               "http://schemas.xmlsoap.org/wsdl/soap/http", ref);
            LOG.fine("publishing endpoint " + ep);
            ep.publish("http://localhost/"); 
            registerActiveEndpoint(ep);
            
            targetGBean.setAttribute(WEB_SERVICE_CONTAINER_ATTR, new StoredObject(container));
        } catch (IOException ex) {
            throw new DeploymentException("unable to store CeltixWebServiceContainer", ex);
        } finally {
            if (factory != null) {
                factory.setCurrentContainer(null);
            }
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    private URL resolveWSDL(String resource, ClassLoader loader) { 
        
        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return loader.getResource(resource);
        }
    }
    
    
    private Object createDelegatingProxyFor(Class<?> seiClass) {
        return Proxy.newProxyInstance(seiClass.getClassLoader(), new Class<?>[] {seiClass},
                                      new DelegatingProxyInvocationHandler());
    }
    
    private void registerActiveEndpoint(EndpointImpl endpoint) {
        activeEndpoints.add(endpoint);
    }
    
    
    public void configureEJB(GBeanData targetGBean, JarFile moduleFile, Object portInfo, 
                             ClassLoader classLoader)
        throws DeploymentException {

        throw new DeploymentException("configureEJB NYI");
    }

    public void doStart() throws Exception {
        
        TransportFactoryManager tfm = bus.getTransportFactoryManager();
        factory.init(getBus());
        tfm.registerTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/",
                factory);
        tfm.registerTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/http",
                factory);
        tfm.registerTransportFactory("http://celtix.objectweb.org/transports/http/configuration",
                factory);

    }

    public void doStop() throws Exception {
        
        for (EndpointImpl ep : activeEndpoints) {
            ep.stop();
        }
    }

    public void doFail() {
        // TODO Auto-generated method stub
        
    }
    
    private JAXBContext getJAXBContext() throws JAXBException {
        if (ctx == null) {
            ctx = JAXBContext.newInstance("com.sun.java.xml.ns.j2ee", getClass().getClassLoader());
        }
        return ctx;
    }
    
    Class<?> loadSEI(String className, ClassLoader loader) throws DeploymentException {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("unable to load Service Endpoint Interface: " + className, ex);
        }
    }
    
    private List<Handler> buildHandlerChain(PortInfo portInfo) {
        return new ArrayList<Handler>();
    }
    
    protected Bus getBus() {
        return bus;
    }
    
    static class DelegatingProxyInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) {
            System.out.println(this + " invoking method " + method);
            return null;
        }
    }
}
