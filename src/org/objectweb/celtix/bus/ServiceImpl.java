package org.objectweb.celtix.bus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerRegistry;
import javax.xml.ws.security.SecurityConfiguration;

public class ServiceImpl implements Service, InvocationHandler {

    private URL wsdlLocation;
    private QName serviceName;
    private Vector<QName> endpointList;
    
    /**
     * Create a new Service.
     * @throws WebServiceException If there is an exception creating Service.
     */
    public ServiceImpl(QName name, URL location) throws WebServiceException {
        wsdlLocation = location;
        serviceName = name;
        endpointList = new Vector<QName>();
    }
    
    public void addPort(QName portName, URI bindingId, String endpointAddress) throws WebServiceException {
        throw new UnsupportedOperationException("addPort not yet supported");        
    }   
    
    public <T> T getPort(QName portName, Class<T> serviceEndpointInterface) throws WebServiceException {
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
        
        if (portName == null) {
            portName = getPortName(wsAnnotation);
        } else if (portName.getNamespaceURI() == null) {
            portName = new QName(wsAnnotation.targetNamespace(), portName.getLocalPart());  
        }
        
        //Parse WSDL 
        
        //Create Binding,Tranpsort and finally PortTypeProxy

        endpointList.add(portName);
       
        return null;
    }

    public <T> T getPort(Class<T> serviceEndpointInterface) throws WebServiceException {
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
        
        QName portName = getPortName(wsAnnotation);       
        endpointList.add(portName);
        
        return null;
    }

    public void createPort(QName portName, URI bindingId, String endpointAddress) throws WebServiceException {
        
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

        if (Proxy.isProxyClass(proxy.getClass())) {
            
            Class<?> returnType = method.getReturnType();
            
            if (returnType != null) {
                String endpointName = getEndpointName(method.getName());

                return getPort(new QName("", endpointName), returnType);
            } else {
                StringBuilder str = new StringBuilder(method.getName());
                str.append(" must have a Return Type");
                throw new WebServiceException(str.toString());
            }
        } else {
            method.invoke(proxy, args);
        }
        
        return null;       
    }

    private String getEndpointName(String endpointName) {
        if (endpointName.startsWith("get")) {
            return endpointName.substring(3);
        }
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
    
    private QName getPortName(WebService wsAnnotation) {

        QName portName = null;
        if (wsAnnotation != null) {
            portName = new QName(wsAnnotation.targetNamespace(), wsAnnotation.name());
        }
        
        return portName;
    }
}
