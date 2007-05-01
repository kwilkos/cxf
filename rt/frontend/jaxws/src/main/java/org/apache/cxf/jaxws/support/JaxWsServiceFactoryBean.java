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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Service;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.databinding.source.SourceDataBinding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.JAXWSMethodDispatcher;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.interceptors.ProviderInDatabindingInterceptor;
import org.apache.cxf.jaxws.interceptors.ProviderOutDatabindingInterceptor;
import org.apache.cxf.jaxws.interceptors.WebFaultOutInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

/**
 * Constructs a service model from JAX-WS service endpoint classes. Works
 * with both @@WebServiceProvider and @@WebService annotated classes.
 *
 * @see org.apache.cxf.jaxws.JaxWsServerFactoryBean
 */
public class JaxWsServiceFactoryBean extends AbstractJaxWsServiceFactoryBean {
    
    private AbstractServiceConfiguration jaxWsConfiguration;

    private JaxWsImplementorInfo implInfo;

    private JAXWSMethodDispatcher methodDispatcher;

    public JaxWsServiceFactoryBean() {
        getIgnoredClasses().add(Service.class.getName());
    }

    public JaxWsServiceFactoryBean(JaxWsImplementorInfo implInfo) {
        this();
        this.implInfo = implInfo;
        initConfiguration(implInfo);
        this.serviceClass = implInfo.getEndpointClass();
    }

    @Override
    protected Invoker createInvoker() {
        return null;
    }
    
    protected SimpleMethodDispatcher getMethodDispatcher() {
        return methodDispatcher;
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

        if (implInfo.isWebServiceProvider()) {
            Class<?> type = implInfo.getProviderParameterType();
            if (type.equals(SOAPMessage.class)) {
                getService().getInInterceptors().add(new ProviderInDatabindingInterceptor(type));
                // hack to get the SOAPMessage set before the SOAPHandlerInterceptor
                ProviderOutDatabindingInterceptor out = new ProviderOutDatabindingInterceptor();
                out.setPhase(Phase.PRE_PROTOCOL);
                out.addBefore(SOAPHandlerInterceptor.class.getName());
                getService().getOutInterceptors().add(out);
                
                getService().getInInterceptors().add(new SAAJInInterceptor());
                getService().getOutInterceptors().add(new SAAJOutInterceptor());
            } else {
                getService().getInInterceptors().add(new ProviderInDatabindingInterceptor(type));
                getService().getOutInterceptors().add(new ProviderOutDatabindingInterceptor());
            }

            boolean messageMode = implInfo.getServiceMode().equals(javax.xml.ws.Service.Mode.MESSAGE);
            if (getEndpointInfo() != null) {
                for (BindingInfo bi : getEndpointInfo().getService().getBindings()) {
                    if ((bi instanceof SoapBindingInfo) 
                        && messageMode && !type.equals(SOAPMessage.class)) {
                        bi.setProperty(SoapBindingFactory.MESSAGE_PROCESSING_DISABLED, Boolean.TRUE);
                    }
                }
            }
        }
    }

    @Override
    public Endpoint createEndpoint(EndpointInfo ei) throws EndpointException {
        return new JaxWsEndpointImpl(getBus(), getService(), ei);
    }

    @Override
    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method method) {
        method = ((JaxWsServiceConfiguration)jaxWsConfiguration).getDeclaredMethod(method);

        super.initializeWSDLOperation(intf, o, method);

        initializeWrapping(o, method);

        try {
            // Find the Async method which returns a Response
            Method responseMethod = method.getDeclaringClass().getDeclaredMethod(method.getName() + "Async",
                                                                                 method.getParameterTypes());

            // Find the Async method whic has a Future & AsyncResultHandler
            List<Class<?>> asyncHandlerParams = Arrays.asList(method.getParameterTypes());
            //copy it to may it non-readonly
            asyncHandlerParams = new ArrayList<Class<?>>(asyncHandlerParams);
            asyncHandlerParams.add(AsyncHandler.class);
            Method futureMethod = method.getDeclaringClass()
                .getDeclaredMethod(method.getName() + "Async",
                                   asyncHandlerParams.toArray(new Class<?>[asyncHandlerParams.size()]));

            getMethodDispatcher().bind(o, method, responseMethod, futureMethod);

        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            getMethodDispatcher().bind(o, method);
        }

        // rpc out-message-part-info class mapping
        Operation op = (Operation)o.getProperty(WSDLServiceBuilder.WSDL_OPERATION);
        
        initializeClassInfo(o, method, op == null ? null
            : CastUtils.cast(op.getParameterOrdering(), String.class));
    }

    @Override
    protected void initializeWSDLOperations() {
        if (implInfo.isWebServiceProvider()) {
            initializeWSDLOperationsForProvider();
        } else {
            super.initializeWSDLOperations();
        }
    }
    
 
    protected void initializeWSDLOperationsForProvider() {
        Type[] genericInterfaces = getServiceClass().getGenericInterfaces();
        ParameterizedType pt = (ParameterizedType)genericInterfaces[0];
        Class c = (Class)pt.getActualTypeArguments()[0];

        try {
            Method invoke = getServiceClass().getMethod("invoke", c);

            // Bind each operation to the invoke method.
            for (OperationInfo o : getEndpointInfo().getService().getInterface().getOperations()) {
                getMethodDispatcher().bind(o, invoke);
            }

        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            throw new ServiceConstructionException(e);
        }

        for (BindingInfo bi : getEndpointInfo().getService().getBindings()) {
            bi.setProperty(AbstractBindingFactory.DATABINDING_DISABLED, Boolean.TRUE);
        }
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

    /**
     * Create a mock service model with two operations - invoke and
     * invokeOneway.
     */
    // @Override
    // protected InterfaceInfo createInterface(ServiceInfo serviceInfo) {
    // if (jaxWsImplementorInfo.isWebServiceProvider()) {
    // return createInterfaceForProvider(serviceInfo);
    // } else {
    // return super.createInterface(serviceInfo);
    // }
    // }
    //
    // protected InterfaceInfo createInterfaceForProvider(ServiceInfo
    // serviceInfo) {
    //
    // InterfaceInfo intf = new InterfaceInfo(serviceInfo, getInterfaceName());
    //
    // String ns = getServiceNamespace();
    // OperationInfo invoke = intf.addOperation(new QName(ns, "invoke"));
    //
    // MessageInfo input = invoke.createMessage(new QName(ns, "input"));
    // invoke.setInput("input", input);
    //
    // input.addMessagePart("in");
    //
    // MessageInfo output = invoke.createMessage(new QName(ns, "output"));
    // invoke.setOutput("output", output);
    //
    // output.addMessagePart("out");
    // //
    // // OperationInfo invokeOneWay = intf.addOperation(new
    // // QName(getServiceNamespace(), "invokeOneWay"));
    // // invokeOneWay.setInput("input", input);
    //
    // return intf;
    // }
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
    protected Class<?> getBeanClass(Class<?> exClass) {
        try {
            if (java.rmi.ServerException.class.isAssignableFrom(exClass)
                || java.rmi.RemoteException.class.isAssignableFrom(exClass)) {
                return null;
            }
            
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
                part.setIndex(-1);
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
        boolean isIn = isInParam(method, i);
        boolean isOut = isOutParam(method, i);

        MessagePartInfo part = null;
        if (isIn && !isOut) {
            QName name = getInPartName(o, method, i);
            part = o.getInput().getMessagePart(name);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i);
        } else if (!isIn && isOut) {
            QName name = getOutPartName(o, method, i);
            part = o.getOutput().getMessagePart(name);
            part.setProperty(ReflectionServiceFactoryBean.MODE_OUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i);
        } else if (isIn && isOut) {
            QName name = getOutPartName(o, method, i);
            part = o.getInput().getMessagePart(name);
            part.setProperty(ReflectionServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i);

            part = o.getOutput().getMessagePart(name);
            part.setProperty(ReflectionServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i);
        }
    }

    public void setJaxWsConfiguration(JaxWsServiceConfiguration jaxWsConfiguration) {
        this.jaxWsConfiguration = jaxWsConfiguration;
    }

    public JaxWsImplementorInfo getJaxWsImplementorInfo() {
        return implInfo;
    }

    public void setJaxWsImplementorInfo(JaxWsImplementorInfo jaxWsImplementorInfo) {
        this.implInfo = jaxWsImplementorInfo;

        initConfiguration(jaxWsImplementorInfo);
    }

    protected final void initConfiguration(JaxWsImplementorInfo ii) {
        if (ii.isWebServiceProvider()) {
            jaxWsConfiguration = new WebServiceProviderConfiguration();
            getServiceConfigurations().add(0, jaxWsConfiguration);
            setWrapped(false);
            setDataBinding(new SourceDataBinding());
        } else {
            jaxWsConfiguration = new JaxWsServiceConfiguration();
            jaxWsConfiguration.setServiceFactory(this);
            getServiceConfigurations().add(0, jaxWsConfiguration);
        }
        methodDispatcher = new JAXWSMethodDispatcher(implInfo);
    }
}
