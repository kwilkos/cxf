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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.interceptors.WebFaultOutInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {

    public static final String MODE_OUT = "messagepart.mode.out";

    public static final String MODE_INOUT = "messagepart.mode.inout";

    public static final String HOLDER = "messagepart.isholder";

    private JAXBDataBinding dataBinding;

    private JaxWsServiceConfiguration jaxWsConfiguration;

    private JaxWsImplementorInfo jaxWsImplementorInfo;

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
        initalizeClassInfo(o, method, op == null ? null : op.getParameterOrdering());

    }


    void initializeWrapping(OperationInfo o, Method selected) {
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

    
    private void setInputClassInfo(OperationInfo o, Method selected) {
        Object[] para = selected.getParameterTypes();
        int inIdx = 0;
        for (MessagePartInfo mpiIn : o.getInput().getMessageParts()) {
            // check for sayHi() type no input param method
            if (para.length > 0 && !para[inIdx].equals(Holder.class)) {
                mpiIn.setProperty(Class.class.getName(), para[inIdx]);
            }
            inIdx++;
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
                    if (mpi.getConcreteName().equals(name)) {
                        fi.setProperty(Class.class.getName(), exClass);
                        mpi.setProperty(Class.class.getName(), beanClass);
                    }
                }
            }
        }
    }
    /**
     * set the holder generic type info into message part info
     * 
     * @param o
     * @param method
     */
    protected void initalizeClassInfo(OperationInfo o, Method selected, List<String> paramOrder) {
        if (o.getOutput() == null) {
            return;
        }
        int outPartCount = o.getOutput().getMessageParts().size();
        int inPartCount = o.getInput().getMessageParts().size();        
        int outIdx = 0;
        int offset = 0;
        setInputClassInfo(o, selected);
        for (MessagePartInfo mpiOut : o.getOutput().getMessageParts()) {
            int inIdx = 0;
            boolean isInOut = false;
            MessagePartInfo mpiInHolder = null;
            Object[] paraType = selected.getGenericParameterTypes();
            for (MessagePartInfo mpiIn : o.getInput().getMessageParts()) {
                if (mpiOut.getName().equals(mpiIn.getName())) {
                    if (mpiOut.isElement() && mpiIn.isElement()
                            && mpiOut.getElementQName().equals(mpiIn.getElementQName())) {
                        isInOut = true;
                        mpiInHolder = mpiIn;
                    } else if (!mpiOut.isElement() && !mpiIn.isElement()
                            && mpiOut.getTypeQName().equals(mpiIn.getTypeQName())) {
                        isInOut = true;
                        mpiInHolder = mpiIn;
                    }
                }
                if (isInOut) {
                    ParameterizedType paramType = (ParameterizedType) paraType[inIdx];
                    Class rawClass = getHolderClass(paramType, inIdx);
                    mpiOut.setProperty(Class.class.getName(), rawClass);
                    mpiInHolder.setProperty(Class.class.getName(), rawClass);
                    mpiOut.setProperty(JaxWsServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
                    mpiInHolder.setProperty(JaxWsServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
                    offset++;
                    break;
                }
                inIdx++;
            }
            if (!isInOut) {
                if (((Class) selected.getReturnType()).getName().equals("void")) {
                    // to avoid <element name="..."><complexType/></element> in
                    // output message part
                    if (paraType.length > inPartCount + outIdx) {
                        ParameterizedType paramType = (ParameterizedType) paraType[inPartCount + outIdx];
                        Class rawClass = getHolderClass(paramType, inPartCount + outIdx);
                        mpiOut.setProperty(Class.class.getName(), rawClass);
                        mpiOut.setProperty(JaxWsServiceFactoryBean.MODE_OUT, Boolean.TRUE);
                    }
                } else {
                    String local = mpiOut.isElement() ? mpiOut.getElementQName().getLocalPart() : mpiOut
                            .getTypeQName().getLocalPart();
                    if ("return".equalsIgnoreCase(mpiOut.getName().getLocalPart()) || outPartCount == 1) {
                        mpiOut.setProperty(Class.class.getName(), selected.getReturnType());
                        offset++;
                    } else {
                        if (paramOrder != null && !paramOrder.contains(local)) {
                            int idx = inPartCount + outIdx - offset;
                            ParameterizedType paramType = (ParameterizedType) paraType[idx];
                            Class rawClass = getHolderClass(paramType, idx);
                            mpiOut.setProperty(Class.class.getName(), rawClass);
                            mpiOut.setProperty(JaxWsServiceFactoryBean.MODE_OUT, Boolean.TRUE);
                        }
                    }
                }
            }
            outIdx++;
        }
        setFaultClassInfo(o, selected);
    }

    private static Class getHolderClass(ParameterizedType paramType, int idx) {
        if (((Class) paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
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
        } else {
            throw new RuntimeException("Expected Holder at " + idx + " parametor of input message");
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