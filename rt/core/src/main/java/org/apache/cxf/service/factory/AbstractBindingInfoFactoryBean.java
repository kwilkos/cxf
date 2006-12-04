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
package org.apache.cxf.service.factory;

import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.ServiceInfo;

/**
 * An AbstractBindingFactory builds a binding for a Service.
 */
public abstract class AbstractBindingInfoFactoryBean {
    private AbstractServiceFactoryBean serviceFactory;
    
    public abstract BindingInfo create();
    
    public AbstractServiceFactoryBean getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(AbstractServiceFactoryBean serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    protected Service getService() {        
        return serviceFactory.getService();        
    }
    
    protected ServiceInfo getServiceInfo() {        
        return getService().getServiceInfo();        
    }
}
