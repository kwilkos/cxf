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
package org.apache.cxf.binding.http;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.xml.XMLBinding;
import org.apache.cxf.binding.xml.interceptor.XMLMessageOutInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;

public class HttpBindingFactory extends AbstractBindingFactory {

    public static final String HTTP_BINDING_ID = "http://apache.org/cxf/binding/http";
    private Bus bus;
    private Collection<String> activationNamespaces;    
       
    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    @Resource
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }

    @PostConstruct
    void register() {
        if (null == bus) {
            return;
        }
        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);
        if (null != bfm) {
            for (String ns : activationNamespaces) {
                bfm.registerBindingFactory(ns, this);
            }
        }
    }

    public Binding createBinding(BindingInfo bi) {
        XMLBinding binding = new XMLBinding();
        
        binding.getInInterceptors().add(new DispatchInterceptor());
        binding.getInInterceptors().add(new URIParameterInterceptor());

        binding.getOutInterceptors().add(new StaxOutInterceptor());
        binding.getOutInterceptors().add(new ContentTypeOutInterceptor());
        binding.getOutInterceptors().add(new WrappedOutInterceptor());
        binding.getOutInterceptors().add(new XMLMessageOutInterceptor());
        
        return binding;
    }

}
