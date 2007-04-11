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
package org.apache.cxf.binding.object;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class ObjectBindingFactory extends AbstractBindingFactory {
    public static final String BINDING_ID = "http://cxf.apache.org/binding/object";
    public static final String RUN_NON_LOGICAL  = "objectBinding.stopAfterLogical";
    
    private Collection<String> activationNamespaces;    
    private Bus bus;
    private boolean autoRegisterLocalEndpoint;
    private boolean initialized = true;
    private LocalServerListener listener;
    
    public Collection<String> getActivationNamespaces() {
        return activationNamespaces;
    }

    @Resource(name = "activationNamespaces")
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }
    
    @Resource(name = "bus")
    public void setBus(Bus bus) {
        this.bus = bus;
    }

    @PostConstruct
    public void initialize() {
        if (autoRegisterLocalEndpoint) {
            ServerLifeCycleManager manager = bus.getExtension(ServerLifeCycleManager.class);
            if (manager != null) {
                listener = new LocalServerListener(bus, this);
                manager.registerListener(listener);
            }
        }
        initialized = true;
    }
    
    public Binding createBinding(BindingInfo bi) {
        ObjectBinding binding = new ObjectBinding();
        binding.getOutInterceptors().add(new ObjectDispatchOutInterceptor());
        binding.getInInterceptors().add(new ObjectDispatchInInterceptor());
        
        return binding;
    }

    public BindingInfo createBindingInfo(ServiceInfo si, String bindingid, Object config) {
        BindingInfo info = super.createBindingInfo(si, bindingid, config);
        
        if (config instanceof ObjectBindingConfiguration) {
            ObjectBindingConfiguration c = (ObjectBindingConfiguration) config;
            
            info.setProperty(RUN_NON_LOGICAL, c.isNonLogicalPhasesEnabled());
        }
        
        info.setName(new QName(si.getName().getNamespaceURI(), 
                               si.getName().getLocalPart() + "ObjectBinding"));
        
        for (OperationInfo o : si.getInterface().getOperations()) {
            BindingOperationInfo bop = info.buildOperation(o.getName(), o.getInputName(), o.getOutputName());
            info.addOperation(bop);
        }
        return info;
    }

    public boolean isAutoRegisterLocalEndpoint() {
        return autoRegisterLocalEndpoint;
    }

    public void setAutoRegisterLocalEndpoint(boolean autoRegisterLocalEndpoint) {
        this.autoRegisterLocalEndpoint = autoRegisterLocalEndpoint;
        
        if (initialized && listener == null) {
            // register the lifecycle listener
            initialize();
        }
    }

}
