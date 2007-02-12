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

import javax.xml.namespace.QName;

import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class ProviderServiceFactoryBean extends AbstractJaxWsServiceFactoryBean {

    private String bindingURI;
    
    public ProviderServiceFactoryBean(JaxWsImplementorInfo implInfo) {
        setJaxWsImplementorInfo(implInfo);
        this.bindingURI = implInfo.getBindingType();
        getServiceConfigurations().add(0, new WebServiceProviderConfiguration());
        setServiceClass(implInfo.getImplementorClass());
        setWrapped(false);
    }
    
    @Override
    protected void initializeWSDLOperations() {
        Type[] genericInterfaces = getServiceClass().getGenericInterfaces();
        ParameterizedType pt = (ParameterizedType) genericInterfaces[0];
        Class c = (Class) pt.getActualTypeArguments()[0];
        
        try {
            Method invoke = getServiceClass().getMethod("invoke", c);
            
            // Bind each operation to the invoke method.
            for (OperationInfo o : getService().getServiceInfo().getInterface().getOperations()) {
                getMethodDispatcher().bind(o, invoke);
            }
            
        } catch (SecurityException e) {
            throw new ServiceConstructionException(e);
        } catch (NoSuchMethodException e) {
            throw new ServiceConstructionException(e);
        }
    }

    @Override
    public Endpoint createEndpoint(EndpointInfo ei) throws EndpointException  {
        return new JaxWsEndpointImpl(getBus(), getService(), ei);
    }
    
    /**
     * Create a mock service model with two operations - invoke and invokeOneway.
     */
    @Override
    protected InterfaceInfo createInterface(ServiceInfo serviceInfo) {
        InterfaceInfo intf = new InterfaceInfo(serviceInfo, getInterfaceName());
        
        String ns = getServiceNamespace();
        OperationInfo invoke = intf.addOperation(new QName(ns, "invoke"));
        
        MessageInfo input = invoke.createMessage(new QName(ns, "input"));
        invoke.setInput("input", input);
        
        input.addMessagePart("in");
        
        MessageInfo output = invoke.createMessage(new QName(ns, "output"));
        invoke.setOutput("output", output);

        output.addMessagePart("out");
        
        OperationInfo invokeOneWay = intf.addOperation(new QName(getServiceNamespace(), "invokeOneWay"));
        invokeOneWay.setInput("input", input);

        return intf;
    }
    
    
    @Override
    public Service create() {
        Service s = super.create();
        
        if (getJaxWsImplementorInfo().getWsdlLocation().length() == 0) {
            initializeBindings();
        }
        
        return s;
    }

    protected void initializeBindings() {
        ServiceInfo si = getService().getServiceInfo();
        if (XMLConstants.NS_XML_FORMAT.equals(bindingURI)) {
            BindingInfo bi = new BindingInfo(si, bindingURI);
            
            BindingOperationInfo bop = 
                bi.buildOperation(new QName(getServiceNamespace(), 
                                            "invoke"), "input", "output");
            bi.addOperation(bop);
            bi.setName(new QName(getServiceNamespace(), getServiceName() + "Binding"));
            si.addBinding(bi);
            
            EndpointInfo ei = new EndpointInfo(si, bindingURI);
            ei.setBinding(bi);
            ei.setName(getEndpointName());
            si.addEndpoint(ei);
        } else if ("soapns".equals(bindingURI)) {
            // TODO
        }
    }

    @Override
    protected void initializeDataBindings() {   
        setDataBinding(new JAXBDataBinding());
        
        super.initializeDataBindings();
    }
    
    public String getBindingURI() {
        return bindingURI;
    }

    public void setBindingURI(String bindingURI) {
        this.bindingURI = bindingURI;
    }
}
