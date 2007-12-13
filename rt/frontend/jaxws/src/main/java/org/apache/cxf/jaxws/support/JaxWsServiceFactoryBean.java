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
import java.util.Set;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.FaultAction;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceFeature;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.databinding.source.SourceDataBinding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.JAXWSMethodDispatcher;
import org.apache.cxf.jaxws.WrapperClassGenerator;
import org.apache.cxf.jaxws.interceptors.DispatchInDatabindingInterceptor;
import org.apache.cxf.jaxws.interceptors.DispatchOutDatabindingInterceptor;
import org.apache.cxf.jaxws.interceptors.WebFaultOutInterceptor;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

/**
 * Constructs a service model from JAX-WS service endpoint classes. Works
 * with both @@WebServiceProvider and @@WebService annotated classes.
 *
 * @see org.apache.cxf.jaxws.JaxWsServerFactoryBean
 */
public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {
    private AbstractServiceConfiguration jaxWsConfiguration;

    private JaxWsImplementorInfo implInfo;

    private JAXWSMethodDispatcher methodDispatcher;
    
    private List<WebServiceFeature> wsFeatures;

    private boolean wrapperBeanGenerated;
    private Set<Class<?>> wrapperClasses;
    
    
    public JaxWsServiceFactoryBean() {
        getIgnoredClasses().add(Service.class.getName());
        
        //the JAXWS-RI doesn't qualify the schemas for the wrapper types
        //and thus won't work if we do.
        setQualifyWrapperSchema(false);
    }

    public JaxWsServiceFactoryBean(JaxWsImplementorInfo implInfo) {
        this();
        this.implInfo = implInfo;
        initConfiguration(implInfo);
        this.serviceClass = implInfo.getEndpointClass();
    }

    @Override
    public org.apache.cxf.service.Service create() {
        org.apache.cxf.service.Service s = super.create();
        
        s.put(ENDPOINT_CLASS, implInfo.getEndpointClass());
        
        return s;
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
        setJaxWsImplementorInfo(new JaxWsImplementorInfo(serviceClass));
        super.setServiceClass(getJaxWsImplementorInfo().getEndpointClass());
    }

    @Override
    protected void initializeDefaultInterceptors() {
        super.initializeDefaultInterceptors();

        if (implInfo.isWebServiceProvider()) {
            Class<?> type = implInfo.getProviderParameterType();
            Mode mode = implInfo.getServiceMode();

            getService().getInInterceptors().add(new DispatchInDatabindingInterceptor(type, mode));
            getService().getOutInterceptors().add(new DispatchOutDatabindingInterceptor(mode));
        }
    }

    @Override
    protected void initializeFaultInterceptors() {
        getService().getOutFaultInterceptors().add(new WebFaultOutInterceptor());
    }

    @Override
    public Endpoint createEndpoint(EndpointInfo ei) throws EndpointException {        
        return new JaxWsEndpointImpl(getBus(), getService(), ei, implInfo, wsFeatures, 
                                     this.getFeatures(), this.isFromWsdl());
    }

    @Override
    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method method) {
        method = ((JaxWsServiceConfiguration)jaxWsConfiguration).getDeclaredMethod(method);

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
        if (getResponseWrapperClassName(selected) != null) {
            o.getOutput().getMessageParts().get(0).setProperty("RESPONSE.WRAPPER.CLASSNAME",
                                                           getResponseWrapperClassName(selected));
        }
        Class<?> requestWrapper = getRequestWrapper(selected);
        if (requestWrapper != null) {
            o.getInput().getMessageParts().get(0).setTypeClass(requestWrapper);
        }
        if (getRequestWrapperClassName(selected) != null) {
            o.getInput().getMessageParts().get(0).setProperty("REQUEST.WRAPPER.CLASSNAME",
                                                           getRequestWrapperClassName(selected));
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
            //ignore for now
        }
        WebFault fault = exClass.getAnnotation(WebFault.class);
        if (fault != null && !StringUtils.isEmpty(fault.faultBean())) {
            try {
                return ClassLoaderUtils.loadClass(fault.faultBean(),
                                                   exClass);
            } catch (ClassNotFoundException e1) {
                //ignore
            }
        }
        
        return super.getBeanClass(exClass);
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

    public List<WebServiceFeature> getWsFeatures() {
        return wsFeatures;
    }

    public void setWsFeatures(List<WebServiceFeature> wsFeatures) {
        this.wsFeatures = wsFeatures;
    }

    private FaultInfo getFaultInfo(final OperationInfo operation, final Class expClass) {
        for (FaultInfo fault : operation.getFaults()) {
            if (fault.getProperty(Class.class.getName()) == expClass) {
                return fault;
            }
        }
        return null;
    }
    
    private void buildWSAActions(OperationInfo operation, Method method) {
        Action action = method.getAnnotation(Action.class);
        if (action == null) {
            return;
        }
        String ns = operation.getName().getNamespaceURI();
        MessageInfo input = operation.getInput();
        if (action.input() != null) {
            input.addExtensionAttribute(JAXWSAConstants.WSAW_ACTION_QNAME, new QName(ns, action.input()));
        }
        
        MessageInfo output = operation.getOutput();
        if (output != null && action.output() != null) {
            output.addExtensionAttribute(JAXWSAConstants.WSAW_ACTION_QNAME, new QName(ns, action.output()));
        }
        
        FaultAction[] faultActions = action.fault();
        if (faultActions != null && operation.getFaults() != null) {
            for (FaultAction faultAction : faultActions) {                
                FaultInfo faultInfo = getFaultInfo(operation, faultAction.className());
                faultInfo.addExtensionAttribute(JAXWSAConstants.WSAW_ACTION_QNAME, 
                                                new QName(ns, faultAction.value()));
            }
        }        
    }
    
    @Override
    protected OperationInfo createOperation(ServiceInfo serviceInfo, InterfaceInfo intf, Method m) {
        OperationInfo op = super.createOperation(serviceInfo, intf, m);
        if (op.getUnwrappedOperation() != null) {
            op = op.getUnwrappedOperation();
        }
        buildWSAActions(op, m);
        return op;
    }
    
    @Override
    protected Set<Class<?>> getExtraClass() {
        if (!wrapperBeanGenerated) {
            wrapperClasses = generatedWrapperBeanClass();
        } 
        return wrapperClasses;
    }
    
    private Set<Class<?>> generatedWrapperBeanClass() {
        ServiceInfo serviceInfo = getService().getServiceInfos().get(0);
        WrapperClassGenerator wrapperGen = new WrapperClassGenerator(serviceInfo.getInterface());
        return wrapperGen.genearte();
    }
    
    
}
