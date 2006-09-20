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

package org.apache.cxf.transport.jms;


import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSAddressPolicyType;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;


public class ServiceModelJMSConfigurationProvider implements ConfigurationProvider {

    private final EndpointInfo info;
    
    public ServiceModelJMSConfigurationProvider(EndpointInfo i) {
        info = i;       
    }
    
    public Object getObject(String name) {

        if (null == info) {
            return null;
        }

        if ("server".equals(name)) {
            return info.getExtensor(JMSServerBehaviorPolicyType.class);
        }
        
        if ("client".equals(name)) {
            return info.getExtensor(JMSClientBehaviorPolicyType.class);
        }
        
        if ("addressPolicy".equals(name)) {            
            return info.getExtensor(JMSAddressPolicyType.class);
        }

        return null;
    }

    
    public boolean setObject(String name, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean save() {
        // TODO Auto-generated method stub
        return false;
    }

}
