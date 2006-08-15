package org.objectweb.celtix.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.endpoint.Client;
import org.objectweb.celtix.endpoint.ClientImpl;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.endpoint.EndpointImpl;
import org.objectweb.celtix.jaxws.handlers.HandlerResolverImpl;
import org.objectweb.celtix.jaxws.support.JaxwsEndpointImpl;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.wsdl11.WSDLServiceFactory;

public class ServiceImpl extends ServiceDelegate {
    
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();    
    
    private Bus bus;
    private URL wsdlURL;
    
    private Service service;
    private Endpoint endpoint;
    private HandlerResolver handlerResolver;
    private final Collection<QName> ports = new HashSet<QName>();
    
    public ServiceImpl(Bus b, URL url, QName name, Class<?> cls) {
        bus = b;
        wsdlURL = url;
        
        WSDLServiceFactory sf = new WSDLServiceFactory(bus, url, name);
        service = sf.create(); 
        handlerResolver = new HandlerResolverImpl(bus, name);

    }
    

    public void addPort(QName portName, String bindingId, String address) {
        throw new WebServiceException(new Message("UNSUPPORTED_API_EXC", LOG, "addPort").toString());        
    }

    public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode) { 
        return null;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode) {
        return null;
    }

    public Executor getExecutor() {
        return service.getExecutor();
    }

    public HandlerResolver getHandlerResolver() {
        return handlerResolver;
    }

    public <T> T getPort(Class<T> type) {
        return createPort(null, type);
    }

    public <T> T getPort(QName portName, Class<T> type) {
        if (portName == null) {
            throw new WebServiceException(BUNDLE.getString("PORT_NAME_NULL_EXC"));
        }
        return createPort(portName, type);
    }

    public Iterator<QName> getPorts() {
        return ports.iterator();
    }

    public QName getServiceName() {
        return service.getName();
    }

    public URL getWSDLDocumentLocation() {
        return wsdlURL;
    }

    public void setExecutor(Executor e) {
        service.setExecutor(e);
    }

    public void setHandlerResolver(HandlerResolver hr) {
        handlerResolver = hr;   
    }
    
    public Bus getBus() {
        return bus;
    }

    public Service getService() {
        return service;
    }
    
    protected <T> T createPort(QName portName, Class<T> serviceEndpointInterface) {

        LOG.log(Level.FINE, "creating port for portName", portName);
        LOG.log(Level.FINE, "endpoint interface:", serviceEndpointInterface);
        
        QName pn = portName;
        ServiceInfo si = service.getServiceInfo();
        EndpointInfo ei = null;
        if (portName == null) {
            if (1 == si.getEndpoints().size()) {
                ei = si.getEndpoints().iterator().next();
                pn = new QName(service.getName().getNamespaceURI(), ei.getName().getLocalPart());
            }
        } else {
            ei = si.getEndpoint(portName);
        }
        if (null == pn) {
            throw new WebServiceException(BUNDLE.getString("COULD_NOT_DETERMINE_PORT"));  
        }
        
        endpoint = new EndpointImpl(bus, service, ei);
        
        JaxwsEndpointImpl jaxwsEndpoint = new JaxwsEndpointImpl(bus, service, ei);
        Client client = new ClientImpl(bus, endpoint);
        
        InvocationHandler ih = new EndpointInvocationHandler(endpoint, client, 
                                                             jaxwsEndpoint.getJaxwsBinding());
        
        // configuration stuff 
        // createHandlerChainForBinding(serviceEndpointInterface, portName, endpointHandler.getBinding());
        
        Object obj = Proxy.newProxyInstance(serviceEndpointInterface.getClassLoader(),
                                            new Class[] {serviceEndpointInterface, BindingProvider.class},
                                            ih);

        LOG.log(Level.FINE, "created proxy", obj);
        
        ports.add(pn);
        return serviceEndpointInterface.cast(obj);
    }

}
