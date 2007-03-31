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

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class ObjectBindingFactory extends AbstractBindingFactory {
    public static final String BINDING_ID = "http://cxf.apache.org/binding/object";
    private Collection<String> activationNamespaces;    

    public Collection<String> getActivationNamespaces() {
        return activationNamespaces;
    }

    @Resource(name = "activationNamespaces")
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }

    public Binding createBinding(BindingInfo bi) {
        ObjectBinding binding = new ObjectBinding();
        binding.getOutInterceptors().add(new ObjectDispatchOutInterceptor());
        binding.getInInterceptors().add(new ObjectDispatchInInterceptor());
        
        return binding;
    }

    public BindingInfo createBindingInfo(ServiceInfo si, String bindingid, Object config) {
        BindingInfo info = super.createBindingInfo(si, bindingid, config);
        
        info.setName(new QName(si.getName().getNamespaceURI(), 
                               si.getName().getLocalPart() + "ObjectBinding"));
        
        for (OperationInfo o : si.getInterface().getOperations()) {
            BindingOperationInfo bop = info.buildOperation(o.getName(), o.getInputName(), o.getOutputName());
            info.addOperation(bop);
        }
        return info;
    }

}
