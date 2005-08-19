package org.objectweb.celtix.bus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;

public class ServiceFactoryImpl extends ServiceFactory {

    public ServiceFactoryImpl() throws WebServiceException {
        //Complete
    }

    /**
    * Create a new instance of Service
    * 
    * @param url location of the wsdl
    * @param serviceName the name of the service in the wsdl.
    * @throws WebServiceException If there is an exception creating service.
    */
    public Service createService(URL url, QName serviceName) throws WebServiceException {
        return createProxyService(url, serviceName, null);
    }

    /**
    * Create a new instance of Service
    * 
    * @param serviceName the name of the service in the wsdl.
    * @throws WebServiceException If there is an exception creating service.
    */
    public Service createService(QName serviceName) throws WebServiceException {
        return createProxyService(null, serviceName, null);
    }

    /**
    * Create a new instance of Service
    * 
    * @param serviceInterface the class representing the generated Service.
    * @throws WebServiceException If there is an exception creating service.
    */
    public <T extends Service> T createService(Class<T> serviceInterface) throws WebServiceException {
        if (serviceInterface == null || !Service.class.isAssignableFrom(serviceInterface)) {
            throw new IllegalArgumentException("Invalid Service Class provided");
        }
        
        return serviceInterface.cast(createProxyService(null, null, serviceInterface));
    }
    
    /**
    * Create a new instance of Service
    * 
    * @param url location of the wsdl
    * @param serviceInterface the class representing the generated Service.
    * @throws WebServiceException If there is an exception creating service.
    */
    public <T extends Service> T createService(URL url, 
            Class<T> serviceInterface) throws WebServiceException {
        
        if (serviceInterface == null || !Service.class.isAssignableFrom(serviceInterface)) {
            throw new IllegalArgumentException("Invalid Service Class provided");
        }
        
        return serviceInterface.cast(createProxyService(url, null, serviceInterface));
    }

    private Service createProxyService(URL url, QName serviceName,
            Class<? extends Service> serviceInterface) {
        
        Service service = new ServiceImpl(Bus.getCurrent(), url, serviceName, serviceInterface);
        
        //Create a instance of a dynamic class that implements serviceInterface 
        if (serviceInterface != null) {
            return (Service)Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                                              new Class[] {serviceInterface, Service.class},
                                              (InvocationHandler) service);
        }
        
        return service;
    }
}
