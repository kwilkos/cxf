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
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.WebFault;

import org.apache.cxf.BusException;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.interceptors.WebFaultOutInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.MethodDispatcher;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.ChainInitiationObserver;

public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {

    public static final String HOLDER = "messagepart.isholder";

    private JAXBDataBinding dataBinding;

    private JaxWsServiceConfiguration jaxWsConfiguration;

    private JaxWsImplementorInfo jaxWsImplementorInfo;
    
    private JaxWsMethodDispatcher methodDispatcher = new JaxWsMethodDispatcher();
    
    public JaxWsServiceFactoryBean() {
        jaxWsConfiguration = new JaxWsServiceConfiguration();
        getServiceConfigurations().add(0, jaxWsConfiguration);
    }
    
    public JaxWsServiceFactoryBean(JaxWsImplementorInfo implInfo) {
        this();
        this.jaxWsImplementorInfo = implInfo;
        this.serviceClass = implInfo.getImplementorClass();
    }

    @Override
    public Service create() {
        Service service = super.create();
        
        service.put(MethodDispatcher.class.getName(), methodDispatcher);
        
        return service;
    }

    
    @Override
    public void setServiceClass(Class<?> serviceClass) {
        if (jaxWsImplementorInfo == null) {
            jaxWsImplementorInfo = new JaxWsImplementorInfo(serviceClass);
        }
        
        super.setServiceClass(serviceClass);
    }


    @Override
    protected void initializeDefaultInterceptors() {
        super.initializeDefaultInterceptors();
        
        getService().getOutFaultInterceptors().add(new WebFaultOutInterceptor());
    }

    public void activateEndpoints() throws IOException, WSDLException, BusException, EndpointException {
        Service service = getService();

        for (EndpointInfo ei : service.getServiceInfo().getEndpoints()) {
            activateEndpoint(service, ei);
        }
    }

    public void activateEndpoint(Service service, EndpointInfo ei) throws BusException, WSDLException,
                    IOException, EndpointException {
        JaxWsEndpointImpl ep = new JaxWsEndpointImpl(getBus(), service, ei);
        ChainInitiationObserver observer = new ChainInitiationObserver(ep, getBus());

        ServerImpl server = new ServerImpl(getBus(), ep, observer);

        server.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method method) {
        method = jaxWsConfiguration.getDeclaredMethod(method);
        
        super.initializeWSDLOperation(intf, o, method);

        intializeWrapping(o, method);
        
        try {
            // Find the Async method which returns a Response
            Method responseMethod = method.getDeclaringClass().getDeclaredMethod(method.getName() + "Async", 
                                                                              method.getParameterTypes());

            
            // Find the Async method whic has a Future & AsyncResultHandler
            List<Class<?>> asyncHandlerParams = new ArrayList(Arrays.asList(method.getParameterTypes()));
            asyncHandlerParams.add(AsyncHandler.class);
            Method futureMethod = method.getDeclaringClass().getDeclaredMethod(method.getName() + "Async", 
                asyncHandlerParams.toArray(new Class<?>[asyncHandlerParams.size()]));

            
            methodDispatcher.bind(o, method, responseMethod, futureMethod);

        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            methodDispatcher.bind(o, method);
        }
        
        // rpc out-message-part-info class mapping
        initalizeClassInfo(o, method);
    }

    void intializeWrapping(OperationInfo o, Method selected) {
        Class responseWrapper = getResponseWrapper(selected);
        if (responseWrapper != null) {
            o.getUnwrappedOperation().getOutput().setProperty(WrapperClassOutInterceptor.WRAPPER_CLASS,
                                                              responseWrapper);
        }
        Class<?> requestWrapper = getRequestWrapper(selected);
        if (requestWrapper != null) {
            o.getUnwrappedOperation().getInput().setProperty(WrappedInInterceptor.WRAPPER_CLASS,
                                                             requestWrapper);
        }
    }

    @Override
    protected void initializeDataBindings() {   
        try {
            dataBinding = new JAXBDataBinding(jaxWsConfiguration.getEndpointClass());
        } catch (JAXBException e) {
            throw new ServiceConstructionException(e);
        }
        
        setDataBinding(dataBinding);

        super.initializeDataBindings();
    }

    /**
     * set the holder generic type info into message part info
     * 
     * @param o
     * @param method
     */
    protected void initalizeClassInfo(OperationInfo o, Method selected) {
        if (o.getOutput() == null) {
            return;
        }
        Object[] para = selected.getParameterTypes();
        for (MessagePartInfo mpiOut : o.getOutput().getMessageParts()) {
            int idx = 0;
            boolean isHolder = false;
            MessagePartInfo mpiInHolder = null;
            for (MessagePartInfo mpiIn : o.getInput().getMessageParts()) {
                // check for sayHi() type no input param method
                if (para.length > 0) {
                    mpiIn.setProperty(Class.class.getName(), para[idx]);
                }
                if (mpiOut.getName().equals(mpiIn.getName())) {
                    if (mpiOut.isElement() && mpiIn.isElement()
                                    && mpiOut.getElementQName().equals(mpiIn.getElementQName())) {
                        isHolder = true;
                        mpiInHolder = mpiIn;
                        break;
                    } else if (!mpiOut.isElement() && !mpiIn.isElement()
                                    && mpiOut.getTypeQName().equals(mpiIn.getTypeQName())) {
                        isHolder = true;
                        mpiInHolder = mpiIn;
                        break;
                    }
                }
                idx++;
            }
            if (isHolder) {
                Object[] paraType = selected.getGenericParameterTypes();
                ParameterizedType paramType = (ParameterizedType) paraType[idx];
                if (((Class) paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
                    Object rawType = paramType.getActualTypeArguments()[0];
                    Class rawClass;
                    if (rawType instanceof GenericArrayType) {
                        rawClass = (Class) ((GenericArrayType) rawType).getGenericComponentType();
                        rawClass = Array.newInstance(rawClass, 0).getClass();
                    } else {
                        rawClass = (Class) rawType;
                    }
                    
                    mpiOut.setProperty(Class.class.getName(), rawClass);
                    mpiInHolder.setProperty(Class.class.getName(), rawClass);
                } else {
                    throw new RuntimeException("Expected Holder at " + idx
                                    + " parametor of input message");
                }

                mpiOut.setProperty(JaxWsServiceFactoryBean.HOLDER, Boolean.TRUE);
                mpiInHolder.setProperty(JaxWsServiceFactoryBean.HOLDER, Boolean.TRUE);
            } else {
                mpiOut.setProperty(Class.class.getName(), selected.getReturnType());
            }
        }
        for (FaultInfo fi : o.getFaults()) {
            int i = 0;
            Class<?> cls = selected.getExceptionTypes()[i];
            fi.getMessagePartByIndex(0).setProperty(Class.class.getName(), cls);                
            if (cls.isAnnotationPresent(WebFault.class)) {
                fi.getMessagePartByIndex(i).setProperty(WebFault.class.getName(), Boolean.TRUE);
            }
            i++;
        }
    }

    public JaxWsImplementorInfo getJaxWsImplementorInfo() {
        return jaxWsImplementorInfo;
    }

    public void setJaxWsImplementorInfo(JaxWsImplementorInfo jaxWsImplementorInfo) {
        this.jaxWsImplementorInfo = jaxWsImplementorInfo;
    }

    public void setJaxWsConfiguration(JaxWsServiceConfiguration jaxWsConfiguration) {
        this.jaxWsConfiguration = jaxWsConfiguration;
    }
}