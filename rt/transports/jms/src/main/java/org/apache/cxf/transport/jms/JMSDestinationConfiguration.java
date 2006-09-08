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
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;
import org.apache.cxf.transports.jms.jms_conf.JMSServerConfig;

public class JMSDestinationConfiguration extends JMSConfiguration {
    private JMSServerBehaviorPolicyType jmsServerPolicy;
    private JMSServerConfig jmsServerConfig;
    
    public JMSDestinationConfiguration(Bus bus, EndpointInfo endpointInfo) {
        super(bus, endpointInfo, true);
        jmsServerPolicy = getJMSServerBehaviorPolicyType();
        jmsServerConfig = getServerConfig();
    }
    
    private JMSServerBehaviorPolicyType getJMSServerBehaviorPolicyType() {
        JMSServerBehaviorPolicyType pol = 
            configuration.getObject(JMSServerBehaviorPolicyType.class, 
                                    "jmsServer");
        if (pol == null) {
            pol = new JMSServerBehaviorPolicyType();
        }
        return pol;
    }
          
    private JMSServerConfig getServerConfig() {
        JMSServerConfig serverConf = 
            configuration.getObject(JMSServerConfig.class, "jmsServerConfig");
        if (serverConf == null) {
            serverConf = new JMSServerConfig();
        }
        return serverConf;
    }
    
    public JMSServerBehaviorPolicyType getJMSServerBehaviorPolicy() {
        return jmsServerPolicy;
    }
    
    public JMSServerConfig getServerConfiguration() {
        return jmsServerConfig;
    }
    
    
}
