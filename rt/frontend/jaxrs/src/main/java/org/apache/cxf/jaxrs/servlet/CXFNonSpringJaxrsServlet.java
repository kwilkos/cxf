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
package org.apache.cxf.jaxrs.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.ClassUtils;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class CXFNonSpringJaxrsServlet extends CXFNonSpringServlet {

    private static final String SERVICE_ADDRESS_PARAM = "jaxrs.address";
    private static final String SERVICE_CLASSES_PARAM = "jaxrs.serviceClasses";
    private static final String PROVIDERS_PARAM = "jaxrs.providers";
    private static final String SERVICE_SCOPE_PARAM = "jaxrs.scope";
    private static final String SERVICE_SCOPE_SINGLETON = "singleton";
    private static final String SERVICE_SCOPE_REQUEST = "prototype";
    
    private static final String JAXRS_APPLICATION_PARAM = "javax.ws.rs.Application";
    
    @Override
    public void loadBus(ServletConfig servletConfig) throws ServletException {
        super.loadBus(servletConfig);
        
        String applicationClass = servletConfig.getInitParameter(JAXRS_APPLICATION_PARAM);
        if (applicationClass != null) {
            createServerFromApplication(applicationClass);
            return;
        }
        
        String address = servletConfig.getInitParameter(SERVICE_ADDRESS_PARAM);
        if (address == null) {
            address = "/";
        }
        List<Class> resourceClasses = getServiceClasses(servletConfig);
        Map<Class, ResourceProvider> resourceProviders = 
            getResourceProviders(servletConfig, resourceClasses);
        
        List<?> providers = getProviders(servletConfig);
        
        JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
        bean.setAddress(address);
        bean.setResourceClasses(resourceClasses);
        bean.setProviders(providers);
        for (Map.Entry<Class, ResourceProvider> entry : resourceProviders.entrySet()) {
            bean.setResourceProvider(entry.getKey(), entry.getValue());
        }
        bean.create();
    }

    protected List<Class> getServiceClasses(ServletConfig servletConfig) throws ServletException {
        String serviceBeans = servletConfig.getInitParameter(SERVICE_CLASSES_PARAM);
        if (serviceBeans == null) {
            throw new ServletException("At least one resource class should be specified");
        }
        String[] classNames = serviceBeans.split(" ");
        List<Class> resourceClasses = new ArrayList<Class>();
        for (String cName : classNames) {
            if (cName.length() != 0) {
                Class<?> cls = loadClass(cName);
                resourceClasses.add(cls);
            }
        }
        if (resourceClasses.isEmpty()) {
            throw new ServletException("At least one resource class should be specified");
        }
        return resourceClasses;
    }
    
    protected List<?> getProviders(ServletConfig servletConfig) throws ServletException {
        String providersList = servletConfig.getInitParameter(PROVIDERS_PARAM);
        if (providersList == null) {
            return Collections.EMPTY_LIST;
        }
        String[] classNames = providersList.split(" ");
        List<Object> providers = new ArrayList<Object>();
        for (String cName : classNames) {
            if (cName.length() != 0) {
                Class<?> cls = loadClass(cName);
                providers.add(createSingletonInstance(cls, servletConfig));
            }
        }
        return providers;
    }
    
    protected Map<Class, ResourceProvider> getResourceProviders(ServletConfig servletConfig,
        List<Class> resourceClasses) throws ServletException {
        String scope = servletConfig.getInitParameter(SERVICE_SCOPE_PARAM);
        if (scope != null && !SERVICE_SCOPE_SINGLETON.equals(scope)
            && !SERVICE_SCOPE_REQUEST.equals(scope)) {
            throw new ServletException("Only singleton and prototype scopes are supported");
        }
        boolean isPrototype = SERVICE_SCOPE_REQUEST.equals(scope);
        Map<Class, ResourceProvider> map = new HashMap<Class, ResourceProvider>();
        for (Class c : resourceClasses) {
            map.put(c, isPrototype ? new PerRequestResourceProvider(c)
                                   : new SingletonResourceProvider(
                                         createSingletonInstance(c, servletConfig)));
        }
        return map;
    }    
    
    
    private Object createSingletonInstance(Class<?> cls, ServletConfig sc) throws ServletException {
        Constructor c = ResourceUtils.findResourceConstructor(cls, false);
        if (c == null) {
            throw new ServletException("No valid constructor found for " + cls.getName());
        }
        boolean isDefault = c.getParameterTypes().length == 0; 
        if (!isDefault && (c.getParameterTypes().length != 1 
            || c.getParameterTypes()[0] != ServletConfig.class
            && c.getParameterTypes()[0] != ServletContext.class)) {
            throw new ServletException("Resource classes with singleton scope can only have "
                + "ServletConfig or ServletContext instances injected through their constructors");
        }
        Object[] values = isDefault ? new Object[]{} 
            : new Object[]{c.getParameterTypes()[0] == ServletConfig.class ? sc : sc.getServletContext()}; 
        try {
            return c.newInstance(values);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            throw new ServletException("Resource class " + cls.getName()
                                       + " can not be instantiated"); 
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new ServletException("Resource class " + cls.getName()
                                       + " can not be instantiated due to IllegalAccessException"); 
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new ServletException("Resource class " + cls.getName()
                                       + " can not be instantiated due to InvocationTargetException"); 
        }
    }
    
    protected void createServerFromApplication(String cName) throws ServletException {
        Class<?> appClass = loadClass(cName);
        Application app = null;
        try {
            app = (Application)appClass.newInstance();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            throw new ServletException("Application class " + cName
                                       + " can not be instantiated"); 
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new ServletException("Application class " + cName
                                       + " can not be instantiated due to IllegalAccessException"); 
        }
        
        List<Class> resourceClasses = new ArrayList<Class>();
        List<Object> providers = new ArrayList<Object>();
        Map<Class, ResourceProvider> map = new HashMap<Class, ResourceProvider>();
        
        // at the moment we don't support per-request providers, only resource classes
        for (Class<?> c : app.getClasses()) {
            resourceClasses.add(c);
            map.put(c, new PerRequestResourceProvider(c));
        }
        
        // we can get either a provider or resource class here        
        for (Object o : app.getSingletons()) {
            boolean isProvider = o.getClass().getAnnotation(Provider.class) != null;
            if (isProvider) {
                providers.add(o);
            } else {
                resourceClasses.add(o.getClass());
                map.put(o.getClass(), new SingletonResourceProvider(o));
            }
        }
        
        JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
        bean.setAddress("/");
        bean.setResourceClasses(resourceClasses);
        bean.setProviders(providers);
        for (Map.Entry<Class, ResourceProvider> entry : map.entrySet()) {
            bean.setResourceProvider(entry.getKey(), entry.getValue());
        }
        bean.create();
    }
    
    private Class<?> loadClass(String cName) throws ServletException {
        try {
            return ClassUtils.getClass(CXFNonSpringJaxrsServlet.class.getClassLoader(), cName.trim());
        } catch (ClassNotFoundException ex) {
            throw new ServletException("No resource class " + cName.trim() + " can be found", ex); 
        }
    }
}