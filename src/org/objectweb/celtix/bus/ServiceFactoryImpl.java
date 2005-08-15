package org.objectweb.celtix.bus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.ws.WebServiceException;

public class ServiceFactoryImpl extends ServiceFactory {
    private Class serviceClazz = null;

    public ServiceFactoryImpl() throws WebServiceException {
        try {
            ClassLoader loader = getClass().getClassLoader();

            serviceClazz = Class.forName("org.objectweb.celtix.bus.ServiceImpl", true, loader);
        } catch (ClassNotFoundException cnfex) {
            throw new WebServiceException(cnfex);
        }
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
    public Service createService(Class serviceInterface) throws WebServiceException {
        if (serviceInterface == null || !Service.class.isAssignableFrom(serviceInterface)) {
            throw new IllegalArgumentException("Invalid Service Class provided");
        }
        
        return createProxyService(null, null, serviceInterface);
    }
    
    /**
    * Create a new instance of Service
    * 
    * @param url location of the wsdl
    * @param serviceInterface the class representing the generated Service.
    * @throws WebServiceException If there is an exception creating service.
    */
    public Service createService(URL url, Class serviceInterface) throws WebServiceException {
        if (serviceInterface == null || !Service.class.isAssignableFrom(serviceInterface)) {
            throw new IllegalArgumentException("Invalid Service Class provided");
        }
        
        return createProxyService(url, null, serviceInterface);
    }

    private Service createProxyService(URL url, QName serviceName, Class serviceInterface)
    {
        Service service = null;
        try {
            Class types[] = {javax.xml.namespace.QName.class, java.net.URL.class};
            Constructor constructor = serviceClazz.getConstructor(types);
            Object args[] = {serviceName, url};
            service = (Service) constructor.newInstance(args);
        } catch (InvocationTargetException ite) {
            throw new WebServiceException(ite.getCause());
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }
        
        //Create a instance of a dynamic class that implements serviceInterface 
        if (serviceInterface != null) {
            service = (Service)Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[] {
                serviceInterface, Service.class}, (InvocationHandler) service);
        }
        
        return service;
    }
}
