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

import java.util.List;

import javax.xml.ws.Binding;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.xml.XMLBinding;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.interceptor.ClientFaultConverter;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.binding.BindingImpl;
import org.apache.cxf.jaxws.binding.http.HTTPBindingImpl;
import org.apache.cxf.jaxws.binding.soap.SOAPBindingImpl;
//import org.apache.cxf.jaxws.handler.StreamHandlerInterceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerFaultInInterceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerFaultOutInterceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerOutInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerFaultInInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerFaultOutInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.interceptors.HolderInInterceptor;
import org.apache.cxf.jaxws.interceptors.HolderOutInterceptor;
import org.apache.cxf.jaxws.interceptors.SwAInInterceptor;
import org.apache.cxf.jaxws.interceptors.SwAOutInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;

/**
 * A JAX-WS specific implementation of the CXF {@link org.apache.cxf.endpoint.Endpoint} interface.
 * Extends the interceptor provider functionality of its base class by adding 
 * interceptors in which to execute the JAX-WS handlers.
 * Creates and owns an implementation of {@link Binding} in addition to the
 * CXF {@link org.apache.cxf.binding.Binding}. 
 *
 */
public class JaxWsEndpointImpl extends EndpointImpl {

    private Binding jaxwsBinding;
    
    public JaxWsEndpointImpl(Bus bus, Service s, EndpointInfo ei) throws EndpointException {
        super(bus, s, ei);

        createJaxwsBinding();

        //Inbound chain
        List<Interceptor> in = super.getInInterceptors();
        in.add(new LogicalHandlerInInterceptor(jaxwsBinding));
        in.add(new WrapperClassInInterceptor());
        in.add(new HolderInInterceptor());
        if (getBinding() instanceof SoapBinding) {
            in.add(new SOAPHandlerInterceptor(jaxwsBinding));
            in.add(new SwAInInterceptor());
            getOutInterceptors().add(new SwAOutInterceptor());
        } else {
             // TODO: what for non soap bindings?
        }
        
        //Outbound chain       
        List<Interceptor> out = super.getOutInterceptors();
        out.add(new LogicalHandlerOutInterceptor(jaxwsBinding));
        out.add(new WrapperClassOutInterceptor());
        out.add(new HolderOutInterceptor());       
        if (getBinding() instanceof SoapBinding) {
            out.add(new SOAPHandlerInterceptor(jaxwsBinding));
        }
        
        //Outbound fault chain
        List<Interceptor> outFault = super.getOutFaultInterceptors();    
        outFault.add(new LogicalHandlerFaultOutInterceptor(jaxwsBinding));
        if (getBinding() instanceof SoapBinding) {
            outFault.add(new SOAPHandlerFaultOutInterceptor(jaxwsBinding));
        }

        //Inbound fault chain
        List<Interceptor> inFault = super.getInFaultInterceptors(); 
        inFault.add(new ClientFaultConverter());
        inFault.add(new LogicalHandlerFaultInInterceptor(jaxwsBinding));
        if (getBinding() instanceof SoapBinding) {
            inFault.add(new SOAPHandlerFaultInInterceptor(jaxwsBinding));
        }
    }
    
    public Binding getJaxwsBinding() {
        return jaxwsBinding;
    }
    
    final void createJaxwsBinding() {
        if (getBinding() instanceof SoapBinding) {
            jaxwsBinding = new SOAPBindingImpl(getEndpointInfo().getBinding());
        } else if (getBinding() instanceof XMLBinding) {
            jaxwsBinding = new HTTPBindingImpl(getEndpointInfo().getBinding());
        } else {
            jaxwsBinding = new BindingImpl();
        }
    }
}
