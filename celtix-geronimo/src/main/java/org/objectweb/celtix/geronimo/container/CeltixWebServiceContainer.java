package org.objectweb.celtix.geronimo.container;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.geronimo.builder.PortInfo;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class CeltixWebServiceContainer implements WebServiceContainer, GBeanLifecycle {

    private static final Logger LOG = Logger.getLogger(CeltixWebServiceContainer.class.getName());
    
    private final PortInfo portInfo; 

    private transient  GeronimoTransportFactory factory;
    private transient GeronimoServerTransport serverTransport;
    private transient EndpointImpl publishedEndpoint;
    private transient Bus bus; 
    private transient boolean started; 
    
    
    public CeltixWebServiceContainer(PortInfo pi) {
        portInfo = pi;
    }
    
    public void invoke(Request request, Response response) throws Exception {

        Object target = request.getAttribute(WebServiceContainer.POJO_INSTANCE);
        assert target != null : "target object not available in request"; 
        
        ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
        
        try {
            Thread.currentThread().setContextClassLoader(target.getClass().getClassLoader());
            if (!isEndpointPublished()) {
                doStart();
                publishEndpoint(target);
            }            
            serverTransport.invoke(request, response);
        } finally { 
            Thread.currentThread().setContextClassLoader(origLoader);
        }
    }

    
    public void getWsdl(Request request, Response response) throws Exception {
        // TODO Auto-generated method stub
        System.out.println(this + " getWsdl called " + request.getParameters());       
    }

    public void doStart() throws Exception {
        
        if (started) {
            return;
        }
        
        TransportFactoryManager tfm = getBus().getTransportFactoryManager();
        getTransportFactory().init(getBus());
        tfm.registerTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/",
                factory);
        tfm.registerTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/http",
                factory);
        tfm.registerTransportFactory("http://celtix.objectweb.org/transports/http/configuration",
                factory);
        started = true;
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
        // TODO Auto-generated method stub
    }
    
    
    void setServerTransport(GeronimoServerTransport st) {        
        serverTransport = st;
    }
    
    GeronimoServerTransport getServerTransport() {
        return serverTransport;
    }
    
    private synchronized Bus getBus() throws BusException { 
        if (bus ==  null) {
            // TODO: have the option of getting init values here
            bus = Bus.init();
        }
        return bus;
    }
    
    // setter for bus for unit testing purposes 
    void setBus(Bus b) {
        bus = b;
    }
    
    private boolean isEndpointPublished() {
        return publishedEndpoint != null;
    }
    
    private synchronized void publishEndpoint(Object target) throws BusException, MalformedURLException {

        assert target != null : "null target received";
        
        URL url = resolveWSDL(portInfo.getWsdlFile(), target.getClass().getClassLoader()); 
        assert url != null; 
        
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(url,
                QName.valueOf(portInfo.getServiceName()),
                portInfo.getPortName());

        GeronimoTransportFactory tfactory = getTransportFactory(); 
        assert tfactory != null : "failed to get transport factory";
        
        try {
            tfactory.setCurrentContainer(this);
            EndpointImpl ep = new EndpointImpl(getBus(), target,
                                               "http://schemas.xmlsoap.org/wsdl/soap/http", ref);
            LOG.fine("publishing endpoint " + ep);
            ep.publish("http://localhost/"); 
            publishedEndpoint = ep; 
            
            assert serverTransport != null : "server transport not initialized";
        } finally {
            tfactory.setCurrentContainer(null);
        }
    }
    
    private URL resolveWSDL(String resource, ClassLoader loader) { 
        
        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return loader.getResource(resource);
        }
    }
    

    private synchronized GeronimoTransportFactory getTransportFactory() {
        if (factory == null) {
            factory = new GeronimoTransportFactory();
        }
        return factory;
    }
}
