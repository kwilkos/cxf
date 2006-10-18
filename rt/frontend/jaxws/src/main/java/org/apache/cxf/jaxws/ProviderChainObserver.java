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

package org.apache.cxf.jaxws;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.interceptors.DispatchInInterceptor;
import org.apache.cxf.jaxws.interceptors.DispatchOutInterceptor;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.Service;
import org.apache.cxf.transport.MessageObserver;

public class ProviderChainObserver implements MessageObserver {

    Endpoint endpoint;
    Bus bus;
    JaxWsImplementorInfo implInfo;

    public ProviderChainObserver(Endpoint endpoint, Bus bus, JaxWsImplementorInfo implInfo) {
        super();
        this.endpoint = endpoint;
        this.bus = bus;
        this.implInfo = implInfo;
    }

    public void onMessage(Message m) {
        Message message = endpoint.getBinding().createMessage(m);
        Exchange exchange = new ExchangeImpl();
        exchange.setInMessage(message);
        message.setExchange(exchange);

        // message.setContent(Class.class, implInfo.getProviderParameterType());
        // message.setContent(javax.xml.ws.Service.Mode.class,
        // implInfo.getServiceMode());

        exchange.put(javax.xml.ws.Service.Mode.class, implInfo.getServiceMode());
        exchange.put(Class.class, implInfo.getProviderParameterType());

        exchange.put(Endpoint.class, endpoint);
        exchange.put(Service.class, endpoint.getService());
        exchange.put(Binding.class, endpoint.getBinding());
        exchange.put(Bus.class, bus);
        exchange.setDestination(m.getDestination());

        // setup chain
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getExtension(PhaseManager.class)
            .getInPhases());

        message.setInterceptorChain(chain);
        chain.add(bus.getInInterceptors());
        chain.add(endpoint.getInInterceptors());

        // Modified the binding in interceptors
        endpoint.getBinding().getInInterceptors().clear();
        endpoint.getBinding().getInInterceptors().add(new DispatchInInterceptor());
        chain.add(endpoint.getBinding().getInInterceptors());
        // Modified the binding out interceptors
        endpoint.getBinding().getOutInterceptors().clear();
        endpoint.getBinding().getOutInterceptors().add(new DispatchOutInterceptor());
        chain.add(endpoint.getBinding().getOutInterceptors());

        chain.add(endpoint.getService().getInInterceptors());
        chain.setFaultObserver(endpoint.getOutFaultObserver());

        chain.doIntercept(message);

    }

}
