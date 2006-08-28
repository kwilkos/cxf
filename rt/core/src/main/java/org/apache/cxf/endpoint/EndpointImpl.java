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

package org.apache.cxf.endpoint;

import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;

public class EndpointImpl extends AbstractBasicInterceptorProvider implements Endpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Service service;
    private Binding binding;
    private EndpointInfo endpointInfo;
    private Executor executor;
    private Bus bus;
    private Interceptor faultInterceptor;
    
    public EndpointImpl(Bus bus, Service s, EndpointInfo ei) {
        this.bus = bus;
        service = s;
        endpointInfo = ei;
        createBinding(endpointInfo.getBinding());
    }

    public Interceptor getFaultInterceptor() {
        return faultInterceptor;
    }

    public void setFaultInterceptor(Interceptor faultInterceptor) {
        this.faultInterceptor = faultInterceptor;
    }

    public EndpointInfo getEndpointInfo() {
        return endpointInfo;
    }

    public Service getService() {
        return service;
    }

    public Binding getBinding() {
        return binding;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor e) {
        executor = e;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    final void createBinding(BindingInfo bi) {
        String namespace = bi.getBindingId();
        BindingFactory bf = null;
        try {
            bf = bus.getExtension(BindingFactoryManager.class).getBindingFactory(namespace);
            binding = bf.createBinding(bi);
        } catch (BusException ex) {
            throw new WebServiceException(ex);
        }
        if (null == bf) {
            Message msg = new Message("NO_BINDING_FACTORY", BUNDLE, namespace);
            throw new WebServiceException(msg.toString());
        }
    }

}
