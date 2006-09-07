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

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.binding.BindingImpl;
import org.apache.cxf.jaxws.binding.soap.SOAPBindingImpl;
import org.apache.cxf.jaxws.handler.LogicalHandlerInterceptor;
import org.apache.cxf.jaxws.handler.StreamHandlerInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;

/**
 * A JAX-WS specific implementation of the CXF {@link Endpoint} interface.
 * Extends the interceptor provider functionality of its base class by adding 
 * interceptors in which to execute the JAX-WS handlers.
 * Creates and owns an implementation of {@link Binding} in addition to the
 * CXF {@link org.apache.cxf.binding.Binding}. 
 *
 */
public class JaxwsEndpointImpl extends EndpointImpl {

    private Binding binding;
    
    public JaxwsEndpointImpl(Bus bus, Service s, EndpointInfo ei) throws EndpointException {
        super(bus, s, ei);

        createJaxwsBinding();
        
        List<Interceptor> handlerInterceptors;
                
        handlerInterceptors = new ArrayList<Interceptor>();
        handlerInterceptors.add(new LogicalHandlerInterceptor(binding));
        if (getBinding() instanceof SoapBinding) {
            handlerInterceptors.add(new SOAPHandlerInterceptor(binding));
        } else {
             // TODO: what for non soap bindings?
        }
        handlerInterceptors.add(new StreamHandlerInterceptor(binding));
        
        List<Interceptor> fault = super.getOutFaultInterceptors();
        fault.addAll(handlerInterceptors);
        List<Interceptor> in = super.getInInterceptors();
        in.addAll(handlerInterceptors);
        in.add(new WrapperClassInInterceptor());
        
        List<Interceptor> out = super.getOutInterceptors();
        out.addAll(handlerInterceptors);
        out.add(new WrapperClassOutInterceptor());
    }
    
    public Binding getJaxwsBinding() {
        return binding;
    }
    
    final void createJaxwsBinding() {
        if (getBinding() instanceof SoapBinding) {
            binding = new SOAPBindingImpl((SoapBinding)getBinding());
        } else {
            binding = new BindingImpl();
        }
    }
}
