package org.objectweb.celtix.bus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.util.*;

import javax.jws.WebService;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
//import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerRegistry;
import javax.xml.ws.security.SecurityConfiguration;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.wsdl.WSDLManager;

public class ServiceImpl implements Service, InvocationHandler {

    private URL wsdlLocation;
    private QName serviceName;
    private Vector<QName> endpointList;
    private final Bus bus;
    private final Class<? extends Service> serviceInterface;
    
    /**
     * Create a new Service.
     * @throws WebServiceException If there is an exception creating Service.
     */
    public ServiceImpl(Bus b, URL location, QName name, 
            Class<? extends Service> si) throws WebServiceException {
        bus = b;
        wsdlLocation = location;
        serviceName = name;
        serviceInterface = si;
        endpointList = new Vector<QName>();
    }
    
    public void createPort(QName portName, URI bindingId, String endpointAddress) throws WebServiceException {
        throw new UnsupportedOperationException("addPort not yet supported");        
    }   
    
    public <T> T getPort(QName portName, Class<T> serviceEndpointInterface) throws WebServiceException {
        if (portName == null) {
            throw new WebServiceException("No endpoint specified.");
        }
        
        return createPort(portName, serviceEndpointInterface);
    }

    public <T> T getPort(Class<T> serviceEndpointInterface) throws WebServiceException {
        return createPort(null, serviceEndpointInterface);
    }

    public <T> Dispatch<T> createDispatch(QName portName, Class<T> serviceEndpointInterface, 
                                    Mode mode) throws WebServiceException {
        return null;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode) {
        return null;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public Iterator<QName> getPorts() throws WebServiceException {
        return endpointList.iterator();
    }

    public URL getWSDLDocumentLocation() {
        return wsdlLocation;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        throw new UnsupportedOperationException("SecurityConfiguration not yet supported");
    }

    public HandlerRegistry getHandlerRegistry() {
        throw new UnsupportedOperationException("HandlerRegistry not yet supported");
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (serviceInterface.equals(method.getDeclaringClass())) {
            
            Class<?> returnType = method.getReturnType();
            
            if (returnType != null) {
                String endpointName = getEndpointName(method);

                return getPort(new QName("", endpointName), returnType);
            } else {
                StringBuilder str = new StringBuilder(method.getName());
                str.append(" must have a Return Type");
                throw new WebServiceException(str.toString());
            }
        } else {
            method.invoke(this, args);
        }
        
        return null;       
    }

    protected <T> T createPort(QName portName, 
                Class<T> serviceEndpointInterface) throws WebServiceException {

        Class <? extends Remote> clazz = null;
        try {
            clazz = serviceEndpointInterface.asSubclass(Remote.class);
        } catch (ClassCastException cce) {
            throw new WebServiceException("Invalid Interface Specified", cce);
        }
        
        //Assuming Annotation is Present
        javax.jws.WebService wsAnnotation = 
                (WebService) serviceEndpointInterface.getAnnotation(WebService.class);

        if (wsdlLocation == null) {
            wsdlLocation = getWsdlLocation(wsAnnotation);
        }

        if (wsdlLocation == null) {
            throw new WebServiceException("No wsdl url specified");
        }
        
        if (serviceName == null) {
            serviceName = getServiceName(wsAnnotation);
        }
        
        Port port = getWSDLPort(wsdlLocation, serviceName, portName);

        EndpointInvocationHandler endpointHandler = 
                new EndpointInvocationHandler(bus, port, clazz);
        
        Object obj = Proxy.newProxyInstance(serviceEndpointInterface.getClassLoader(),
                                            new Class[] {serviceEndpointInterface, Remote.class},
                                            (InvocationHandler) endpointHandler);
        
        endpointList.add(portName);
        
        return serviceEndpointInterface.cast(obj);
    }
    
    private String getEndpointName(Method method) {
        //Order of Search
        //a. Look for WebEndpoint Annotation on the method.
        //b. Get the endpoint name from the Method name
        String endpointName = null;
        
        //if (method.isAnnotationPresent(WebEndpoint.class)) {
        //    WebEndpoint wepAnnotation = 
        //        (WebEndpoint) method.getAnnotation(WebEndpoint.class);
        //    endpointName = wepAnnotation.name();
        //} else {
        endpointName = method.getName();
        if (endpointName.startsWith("get")) {
            return endpointName.substring(3);
        }
        //}
        return endpointName;
    }
    
    private URL getWsdlLocation(WebService wsAnnotation) {

        URL url = null;
        if (wsAnnotation != null) {
            try { 
                url = new URL(wsAnnotation.wsdlLocation());
            } catch (java.net.MalformedURLException mue) {
                mue.printStackTrace();
            }
        }
        
        return url;
    }
    
    private QName getServiceName(WebService wsAnnotation) {

        QName serviceQName = null;        
        if (wsAnnotation != null) {
            serviceQName = new QName(wsAnnotation.targetNamespace(), wsAnnotation.serviceName());
        }
        
        return serviceQName;
    }
    
    private Port getWSDLPort(URL wsdlUrl, QName service, QName endpointName) {

        WSDLManager wsdlManager = bus.getWSDLManager();
        Definition defs = null;

        try {
            defs = wsdlManager.getDefinition(wsdlUrl);
        } catch (WSDLException wex) {
            throw new WebServiceException(wex);
        }
        
        javax.wsdl.Service wsdlService = defs.getService(service);
        return wsdlService.getPort(endpointName.getLocalPart());
    }
    
}

