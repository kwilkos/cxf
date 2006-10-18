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

package org.apache.cxf.jaxws.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

public class JaxWsServiceConfiguration extends AbstractServiceConfiguration {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JaxWsServiceConfiguration.class);

    private JaxWsImplementorInfo implInfo;

    @Override
    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        super.setServiceFactory(serviceFactory);
        implInfo = ((JaxWsServiceFactoryBean) serviceFactory).getJaxWsImplementorInfo();        
    }

    WebService getConcreteWebServiceAttribute() {
        return getServiceFactory().getServiceClass().getAnnotation(WebService.class);
    }

    WebService getPortTypeWebServiceAttribute() {
        Class<?> epi = getEndpointClass();
        WebService ws = null;
        if (epi != null) {
            ws = epi.getAnnotation(WebService.class);
        }
        if (ws == null) {
            ws = getConcreteWebServiceAttribute();
        }
        return ws;
    }

    Class getEndpointClass() {
        Class endpointInterface = implInfo.getSEIClass();
        if (null == endpointInterface) {
            endpointInterface = implInfo.getImplementorClass();
        }
        return endpointInterface;
    }

    @Override
    public String getServiceName() {
        WebService ws = getConcreteWebServiceAttribute();
        if (ws != null && ws.serviceName().length() > 0) {
            return ws.serviceName();
        }

        return null;
    }

    @Override
    public String getServiceNamespace() {
        WebService ws = getConcreteWebServiceAttribute();
        if (ws != null && ws.targetNamespace().length() > 0) {
            return ws.targetNamespace();
        }

        return null;
    }

    @Override
    public QName getEndpointName() {
        return implInfo.getEndpointName();
    }

    @Override
    public URL getWsdlURL() {
        WebService ws = getPortTypeWebServiceAttribute();
        if (ws != null && ws.wsdlLocation().length() > 0) {
            try {
                URIResolver resolver = new URIResolver(null, ws.wsdlLocation(), getClass());
                if (resolver.isResolved()) {
                    return resolver.getURI().toURL();
                } else {
                    throw new WebServiceException("Could not find WSDL with URL " + ws.wsdlLocation());
                }
            } catch (IOException e) {
                throw new ServiceConstructionException(new Message("LOAD_WSDL_EXC", 
                                                                   BUNDLE, 
                                                                   ws.wsdlLocation()),
                                                       e);
            }
        }
        return null;
    }

    @Override
    public QName getOperationName(InterfaceInfo intf, Method method) {
        method = getDeclaredMethod(method);

        WebMethod wm = method.getAnnotation(WebMethod.class);
        if (wm != null) {
            String name = wm.operationName();
            if (name.length() == 0) {
                name = method.getName();
            }

            return new QName(intf.getName().getNamespaceURI(), name);
        }

        return null;
    }

    @Override
    public Boolean isOperation(Method method) {
        method = getDeclaredMethod(method);
        if (method != null) {
            WebMethod wm = method.getAnnotation(WebMethod.class);
            if (wm != null) {
                if (wm.exclude()) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if (!method.getDeclaringClass().isInterface()) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    Method getDeclaredMethod(Method method) {
        Class endpointClass = getEndpointClass();

        if (!method.getDeclaringClass().equals(endpointClass)) {
            try {
                method = endpointClass.getMethod(method.getName(), (Class[])method.getParameterTypes());
            } catch (SecurityException e) {
                throw new ServiceConstructionException(e);
            } catch (NoSuchMethodException e) {
                // Do nothing
            }
        }
        return method;
    }
    
    @Override
    public Class getResponseWrapper(Method selected) {
        Method m = getDeclaredMethod(selected);
        
        ResponseWrapper rw = m.getAnnotation(ResponseWrapper.class);
        if (rw == null) {
            return null;
        }
        
        String clsName = rw.className();
        if (clsName.length() > 0) {
            try {
                return ClassLoaderUtils.loadClass(clsName, getClass());
            } catch (ClassNotFoundException e) {
                throw new ServiceConstructionException(e);
            }
        }
        
        return null;
    }

    @Override
    public Class getRequestWrapper(Method selected) {
        Method m = getDeclaredMethod(selected);
        
        RequestWrapper rw = m.getAnnotation(RequestWrapper.class);
        if (rw == null) {
            return null;
        }
        
        String clsName = rw.className();

        if (clsName.length() > 0) {
            try {
                return ClassLoaderUtils.loadClass(clsName, getClass());
            } catch (ClassNotFoundException e) {
                throw new ServiceConstructionException(e);
            }
        }
        
        return null;
    }

    @Override
    public QName getFaultName(InterfaceInfo service, OperationInfo o, Class<?> exClass, Class<?> beanClass) {
        WebFault fault = exClass.getAnnotation(WebFault.class);
        if (fault != null) {
            String name = fault.name();
            if (name.length() == 0) {
                name = exClass.getSimpleName();
            }
            String ns = fault.targetNamespace();
            if (ns.length() == 0) {
                ns = service.getName().getNamespaceURI();
            }
            
            return new QName(ns, name);
        }
        return null;
    }
    
}
