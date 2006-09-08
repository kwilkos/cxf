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

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.jms_conf.JMSClientConfig;



public class JMSConduitConfiguration extends JMSConfiguration {
    private JMSClientBehaviorPolicyType jmsClientPolicy;
    private JMSClientConfig jmsClientConfig;
    
    public JMSConduitConfiguration(Bus bus, EndpointInfo endpointInfo) { 
        super(bus, endpointInfo, false);       
        jmsClientPolicy = getJMSClientBehaviourPolicyType();
        jmsClientConfig = getClientConfig();
    }
     
    private JMSClientBehaviorPolicyType getJMSClientBehaviourPolicyType() {
        JMSClientBehaviorPolicyType pol = 
            configuration.getObject(JMSClientBehaviorPolicyType.class, "jmsClient");
        if (pol == null) {
            pol = new JMSClientBehaviorPolicyType();
        }
        return pol;
    }
    
    private JMSClientConfig getClientConfig() {
        JMSClientConfig clientConf = configuration.getObject(JMSClientConfig.class, "jmsClientConfig");
        if (clientConf == null) {
            clientConf = new JMSClientConfig();
        }
        
        return clientConf;
    }
    
    public JMSClientBehaviorPolicyType getJMSClientBehaviorPolicy() {
        return jmsClientPolicy;
    }
    
    public JMSClientConfig getClientConfiguration() {
        return jmsClientConfig;
    }
        
        
   
}
