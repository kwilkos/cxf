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

package org.apache.cxf.jaxws.spring;

import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.InitializingBean;

public class EndpointBean implements InitializingBean {
    private Object implementor;
    private Class implementorClass;
    private Map<String, Object> properties;
    private String address;
    private Bus bus;
    
    public void afterPropertiesSet() throws Exception {
        EndpointImpl ep = new EndpointImpl(bus, implementor);
        Map<String, Object> props = ep.getProperties();
        if (props != null) {
            props.putAll(properties);
        } else {
            ep.setProperties(properties);
        }
        
        ep.publish(address);
    }
    
    public Bus getBus() {
        return bus;
    }
    public void setBus(Bus bus) {
        this.bus = bus;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Object getImplementor() {
        return implementor;
    }
    public void setImplementor(Object implementor) {
        this.implementor = implementor;
    }
    public Class getImplementorClass() {
        return implementorClass;
    }
    public void setImplementorClass(Class implementorClass) {
        this.implementorClass = implementorClass;
    }
    public Map<String, Object> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    
}
