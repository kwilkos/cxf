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
package org.apache.cxf.binding.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.xml.interceptor.XMLFaultInInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLFaultOutInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLMessageInInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLMessageOutInterceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;

public class XMLBindingFactory extends AbstractBindingFactory {

    private Map cachedBinding = new HashMap<BindingInfo, Binding>();

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

    public Binding createBinding(BindingInfo binding) {

        if (cachedBinding.get(binding) != null) {
            return (Binding) cachedBinding.get(binding);
        }

        XMLBinding xb = new XMLBinding();
        
        xb.getInInterceptors().add(new StaxInInterceptor());
        xb.getInInterceptors().add(new XMLMessageInInterceptor());
        
        xb.getInFaultInterceptors().add(new StaxInInterceptor());
        xb.getInFaultInterceptors().add(new XMLFaultInInterceptor());
        
        xb.getOutInterceptors().add(new StaxOutInterceptor());
        xb.getOutInterceptors().add(new XMLMessageOutInterceptor());
        
        xb.getOutFaultInterceptors().add(new StaxOutInterceptor());
        xb.getOutFaultInterceptors().add(new XMLFaultOutInterceptor());

        return xb;
    }

}
