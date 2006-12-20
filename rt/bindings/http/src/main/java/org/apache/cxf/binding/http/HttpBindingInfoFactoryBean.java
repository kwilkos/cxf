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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.http.strategy.ConventionStrategy;
import org.apache.cxf.binding.http.strategy.JRAStrategy;
import org.apache.cxf.binding.http.strategy.ResourceStrategy;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;

public class HttpBindingInfoFactoryBean extends AbstractBindingInfoFactoryBean {
    private List<ResourceStrategy> strategies = new ArrayList<ResourceStrategy>();
    
    public HttpBindingInfoFactoryBean() {
        super();
        
        strategies.add(new JRAStrategy());
        strategies.add(new ConventionStrategy());
    }

    @Override
    public BindingInfo create() {
        URIMapper mapper = new URIMapper();
        
        BindingInfo info = new BindingInfo(getServiceInfo(), 
                                           HttpBindingFactory.HTTP_BINDING_ID);
        info.setName(new QName(getServiceInfo().getName().getNamespaceURI(), 
                               getServiceInfo().getName().getLocalPart() + "HttpBinding"));
        
        ReflectionServiceFactoryBean sf = (ReflectionServiceFactoryBean) getServiceFactory();
        Service service = sf.getService();
        MethodDispatcher md = (MethodDispatcher) service.get(MethodDispatcher.class.getName()); 

        for (OperationInfo o : getServiceInfo().getInterface().getOperations()) {
            BindingOperationInfo bop = info.buildOperation(o.getName(), o.getInputName(), o.getOutputName());

            info.addOperation(bop);
            
            Method m = md.getMethod(bop);
            
            // attempt to map the method to a resource using different strategies
            for (ResourceStrategy s : strategies) {
                // Try different ones until we find one that succeeds
                if (s.map(bop, m, mapper)) {
                    break;
                }
            }
        }
        
        service.put(URIMapper.class.getName(), mapper);
        return info;
    }

    public List<ResourceStrategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<ResourceStrategy> strategies) {
        this.strategies = strategies;
    }

    
}
