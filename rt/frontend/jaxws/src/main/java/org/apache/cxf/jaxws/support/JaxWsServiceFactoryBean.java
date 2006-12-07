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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxws.interceptors.WebFaultOutInterceptor;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

public class JaxWsServiceFactoryBean extends AbstractJaxWsServiceFactoryBean {

    public static final String MODE_OUT = "messagepart.mode.out";

    public static final String MODE_INOUT = "messagepart.mode.inout";

    public static final String HOLDER = "messagepart.isholder";

    private JaxWsServiceConfiguration jaxWsConfiguration;

    public JaxWsServiceFactoryBean() {
        jaxWsConfiguration = new JaxWsServiceConfiguration();
        getServiceConfigurations().add(0, jaxWsConfiguration);
        getIgnoredClasses().add(Service.class.getName());
    }

    public JaxWsServiceFactoryBean(JaxWsImplementorInfo implInfo) {
        this();
        setJaxWsImplementorInfo(implInfo);
        this.serviceClass = implInfo.getImplementorClass();
    }

    @Override
    public void setServiceClass(Class<?> serviceClass) {
        if (getJaxWsImplementorInfo() == null) {
            setJaxWsImplementorInfo(new JaxWsImplementorInfo(serviceClass));
        }

        super.setServiceClass(serviceClass);
    }

    @Override
    protected void initializeDefaultInterceptors() {
        super.initializeDefaultInterceptors();

        getService().getOutFaultInterceptors().add(new WebFaultOutInterceptor());
    }

    @Override
    protected Endpoint createEndpoint(EndpointInfo ei) throws EndpointException {
        return new JaxWsEndpointImpl(getBus(), getService(), ei);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method method) {
        method = jaxWsConfiguration.getDeclaredMethod(method);

        super.initializeWSDLOperation(intf, o, method);

        initializeWrapping(o, method);

        try {
            // Find the Async method which returns a Response
            Method responseMethod = method.getDeclaringClass().getDeclaredMethod(method.getName() + "Async",
                    method.getParameterTypes());

            // Find the Async method whic has a Future & AsyncResultHandler
            List<Class<?>> asyncHandlerParams = new ArrayList(Arrays.asList(method.getParameterTypes()));
            asyncHandlerParams.add(AsyncHandler.class);
            Method futureMethod = method.getDeclaringClass().getDeclaredMethod(method.getName() + "Async",
                    asyncHandlerParams.toArray(new Class<?>[asyncHandlerParams.size()]));

            getMethodDispatcher().bind(o, method, responseMethod, futureMethod);

        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            getMethodDispatcher().bind(o, method);
        }

        // rpc out-message-part-info class mapping
        Operation op = (Operation)o.getProperty(WSDLServiceBuilder.WSDL_OPERATION);
        initializeClassInfo(o, method, op == null ? null : op.getParameterOrdering());

    }


    void initializeWrapping(OperationInfo o, Method selected) {
        Class responseWrapper = getResponseWrapper(selected);
        if (responseWrapper != null) {
            o.getOutput().getMessageParts().get(0).setTypeClass(responseWrapper);
        }
        Class<?> requestWrapper = getRequestWrapper(selected);
        if (requestWrapper != null) {
            o.getInput().getMessageParts().get(0).setTypeClass(requestWrapper);
        }
    }
    
    private void setFaultClassInfo(OperationInfo o, Method selected) {
        Class[] types = selected.getExceptionTypes();
        for (int i = 0; i < types.length; i++) {
            Class exClass = types[i];
            Class beanClass = getBeanClass(exClass);
            
            QName name = getFaultName(o.getInterface(), o, exClass, beanClass);
            
            for (FaultInfo fi : o.getFaults()) {
                for (MessagePartInfo mpi : fi.getMessageParts()) {
                    String ns = null;
                    if (mpi.isElement()) {
                        ns = mpi.getElementQName().getNamespaceURI();
                    } else {
                        ns = mpi.getTypeQName().getNamespaceURI();
                    }
                    if (mpi.getConcreteName().getLocalPart().equals(name.getLocalPart()) 
                            && name.getNamespaceURI().equals(ns)) {
                        fi.setProperty(Class.class.getName(), exClass);
                        mpi.setTypeClass(beanClass);
                    }
                }
            }
        }
    }
    
    
    @Override
    protected Class getBeanClass(Class exClass) {
        try {
            Method getFaultInfo = exClass.getMethod("getFaultInfo", new Class[0]);
            
            return getFaultInfo.getReturnType();
        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            return super.getBeanClass(exClass);
        }
    }

    /**
     * set the holder generic type info into message part info
     * 
     * @param o
     * @param method
     */
    protected void initializeClassInfo(OperationInfo o, Method method, List<String> paramOrder) {
        if (isWrapped(method)) {
            if (o.hasInput()) {
                MessageInfo input = o.getInput();
                MessagePartInfo part = input.getMessageParts().get(0);
                part.setTypeClass(getRequestWrapper(method));
            }
            
            if (o.hasOutput()) {
                MessageInfo input = o.getOutput();
                MessagePartInfo part = input.getMessageParts().get(0);
                part.setTypeClass(getResponseWrapper(method));
            }
            
            setFaultClassInfo(o, method);
            o = o.getUnwrappedOperation();
        } else if (o.isUnwrappedCapable()) {
            // remove the unwrapped operation because it will break the
            // the WrapperClassOutInterceptor, and in general makes
            // life more confusing
            o.setUnwrappedOperation(null);
         
            setFaultClassInfo(o, method);
        }
        
        Class<?>[] paramTypes = method.getParameterTypes(); 
        Type[] genericTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class paramType = paramTypes[i];
            Type genericType = genericTypes[i];
            
            initializeParameter(o, method, i, paramType, genericType);
        }
        
        // Initialize return type
        Class paramType = method.getReturnType();
        Type genericType = method.getGenericReturnType();
        
        initializeParameter(o, method, -1, paramType, genericType);

        setFaultClassInfo(o, method);
    }

    private void initializeParameter(OperationInfo o, Method method, int i, 
                                     Class paramType, Type genericType) {
        if (isWrapped(method)) {
            return;
        }
        boolean isIn = isInParam(method, i);
        boolean isOut = isOutParam(method, i);

        if (isIn && !isOut) {
            QName name = getInPartName(o, method, i);
            MessagePartInfo part = o.getInput().getMessagePart(name);
            initializeParameter(part, paramType, genericType);
        } else if (!isIn && isOut) {
            QName name = getOutPartName(o, method, i);
            MessagePartInfo part = o.getOutput().getMessagePart(name);
            initializeParameter(part, paramType, genericType);
        } else if (isIn && isOut) {
            QName name = getOutPartName(o, method, i);
            MessagePartInfo part = o.getInput().getMessagePart(name);
            part.setProperty(JaxWsServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            
            part = o.getOutput().getMessagePart(name);
            part.setProperty(JaxWsServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
        }
    }

    private void initializeParameter(MessagePartInfo part, Class rawClass, Type type) {
        if (rawClass.equals(Holder.class) && type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            rawClass = getHolderClass(paramType);
        }
        part.setProperty(GENERIC_TYPE, type);
        part.setTypeClass(rawClass);
    }

    private static Class getHolderClass(ParameterizedType paramType) {
        Object rawType = paramType.getActualTypeArguments()[0];
        Class rawClass;
        if (rawType instanceof GenericArrayType) {
            rawClass = (Class) ((GenericArrayType) rawType).getGenericComponentType();
            rawClass = Array.newInstance(rawClass, 0).getClass();
        } else {
            if (rawType instanceof ParameterizedType) {
                rawType = (Class) ((ParameterizedType) rawType).getRawType();
            }
            rawClass = (Class) rawType;
        }
        return rawClass;
    }

    public void setJaxWsConfiguration(JaxWsServiceConfiguration jaxWsConfiguration) {
        this.jaxWsConfiguration = jaxWsConfiguration;
    }
}
