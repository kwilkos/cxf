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


import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSAddressPolicyType;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;
import org.apache.cxf.transports.jms.jms_conf.JMSClientConfig;
import org.apache.cxf.transports.jms.jms_conf.JMSServerConfig;
import org.apache.cxf.transports.jms.jms_conf.JMSSessionPoolConfigPolicy;
import org.apache.cxf.wsdl11.WSDLServiceFactory;


public class JMSConfigurationTest extends TestCase {
    private Bus bus;
    private EndpointInfo endpointInfo;
       
    public JMSConfigurationTest(String name) {
        super(name);
    }
    
    
    private void setupServiceInfo(String ns, String wsdl, String serviceName, String portName) {
        
        URL wsdlUrl = getClass().getResource(wsdl);
        assertNotNull(wsdlUrl);
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, wsdlUrl, new QName(ns, serviceName));

        Service service = factory.create();        
        endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, portName));
   
    }
    
    

    public void setUp() throws Exception {
        CXFBusFactory bf = new CXFBusFactory();
        bus = new CXFBusFactory().createBus();
        bf.setDefaultBus(bus);
        
    }

    public void tearDown() throws Exception {
        bus.shutdown(true);
        if (System.getProperty("cxf.config.file") != null) {
            System.clearProperty("cxf.config.file");
        }
    }
    
       
    public void testDefaultClientConfig() throws Exception {        
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HelloWorldService", 
                         "HelloWorldPort");
        JMSConduitConfiguration jmsCondConf = new JMSConduitConfiguration(bus, endpointInfo);

        checkDefaultAddressFields(jmsCondConf.getJmsAddressDetails());

        JMSClientBehaviorPolicyType clientPolicy = jmsCondConf.getJMSClientBehaviorPolicy();
        assertTrue("JMSClientBehaviourPolicy cannot be null ", null != clientPolicy);

        assertTrue("JMSClientPolicy messageType should be text ",
                   JMSConstants.TEXT_MESSAGE_TYPE.equals(clientPolicy.getMessageType().value()));
        
        JMSClientConfig cltConf = jmsCondConf.getClientConfiguration();
        assertTrue("clientConfig should not be null ...", cltConf  != null);
        assertTrue("Client receive timeout should be 0", cltConf.getClientReceiveTimeout() == 0);
        assertTrue("Client message time-to-live should be 0", cltConf.getMessageTimeToLive() == 0);
        
        JMSSessionPoolConfigPolicy sessConf = jmsCondConf.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 20...", sessConf.getLowWaterMark() == 20);
        assertTrue("JMS Session pool HighWaterMark should be 500...", sessConf.getHighWaterMark() == 500);
    }

    public void testDefaultServerConfig() throws Exception {        
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HelloWorldService", 
                         "HelloWorldPort");
        
        JMSDestinationConfiguration jmsDestConf = new JMSDestinationConfiguration(bus, endpointInfo);

        checkDefaultAddressFields(jmsDestConf.getJmsAddressDetails());

        JMSServerBehaviorPolicyType serverPolicy = jmsDestConf.getJMSServerBehaviorPolicy();

        assertTrue("JMSServerPolicy messageSelector should be null ",
                   serverPolicy.getMessageSelector() == null);
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be false ",
                   !serverPolicy.isUseMessageIDAsCorrelationID());
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be false ",
                   !serverPolicy.isTransactional());
        assertTrue("JMSServerPolicy durableSubscriberName should not be null ",
                   serverPolicy.getDurableSubscriberName() != null);
        
        JMSServerConfig  srvConf = jmsDestConf.getServerConfiguration();
        assertTrue("serverConfig should not be null ...", srvConf  != null);
        assertTrue("Server message time-to-live should be 0", srvConf.getMessageTimeToLive() == 0);
        assertTrue("durableSubscriptionClientId should be null", 
                   srvConf.getDurableSubscriptionClientId() == null);
        
        JMSSessionPoolConfigPolicy sessConf = jmsDestConf.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 20...", sessConf.getLowWaterMark() == 20);
        assertTrue("JMS Session pool HighWaterMark should be 500...", sessConf.getHighWaterMark() == 500);
        
    }
    
    public void untestClientConfig() throws Exception {


        setupServiceInfo("http://cxf.apache.org/jms_conf_test", 
                         "/wsdl/jms_test_no_addr.wsdl", 
                         "HelloWorldQueueBinMsgService", 
                         "HelloWorldQueueBinMsgPort");
        
        //TODO need configuration to get injected configuration file
        /*URL clientConfigFileUrl = getClass().getResource("/wsdl/jms_test_config.xml");
        System.setProperty("celtix.config.file", clientConfigFileUrl.toString());*/

      
      
        JMSConduitConfiguration jmsCondConf = new JMSConduitConfiguration(bus, endpointInfo);

        checkNonDefaultAddressFields(jmsCondConf.getJmsAddressDetails());

        JMSClientBehaviorPolicyType clientPolicy = jmsCondConf.getJMSClientBehaviorPolicy();
        assertTrue("JMSClientBehaviourPolicy cannot be null ", null != clientPolicy);
        assertTrue("JMSClientPolicy messageType should be text ",
                   JMSConstants.BINARY_MESSAGE_TYPE.equals(clientPolicy.getMessageType().value()));
        
        JMSClientConfig cltConf = jmsCondConf.getClientConfiguration();
        assertTrue("clientConfig should not be null ...", cltConf  != null);
        assertTrue("Client receive timeout should be 500", cltConf.getClientReceiveTimeout() == 500);
        assertTrue("Client message time-to-live should be 500", cltConf.getMessageTimeToLive() == 500);
        
        JMSSessionPoolConfigPolicy sessConf = jmsCondConf.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 10...", sessConf.getLowWaterMark() == 10);
        assertTrue("JMS Session pool HighWaterMark should be 5000...", sessConf.getHighWaterMark() == 5000);

        System.setProperty("celtix.config.file", "");
    }

    public void untestServerConfig() throws Exception {
                
        setupServiceInfo("http://cxf.apache.org/jms_conf_test", 
                         "/wsdl/jms_test_no_addr.wsdl", 
                         "HelloWorldQueueBinMsgService", 
                         "HelloWorldQueueBinMsgPort");

        JMSDestinationConfiguration jmsDestConf = new JMSDestinationConfiguration(bus, endpointInfo);

        checkNonDefaultAddressFields(jmsDestConf.getJmsAddressDetails());

        JMSServerBehaviorPolicyType serverPolicy = jmsDestConf.getJMSServerBehaviorPolicy();

        assertTrue("JMSServerPolicy messageSelector should be Celtix_message_selector ",
                   "Celtix_message_selector".equals(serverPolicy.getMessageSelector()));
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be true ",
                   serverPolicy.isUseMessageIDAsCorrelationID());
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be true ",
                   serverPolicy.isTransactional());
        assertTrue("JMSServerPolicy durableSubscriberName should be Celtix_subscriber ",
                   "Celtix_subscriber".equals(serverPolicy.getDurableSubscriberName()));
        
        JMSServerConfig  srvConf = jmsDestConf.getServerConfiguration();
        assertTrue("serverConfig should not be null ...", srvConf  != null);
        assertTrue("Server message time-to-live should be 500", srvConf.getMessageTimeToLive() == 500);
        assertTrue("durableSubscriptionClientId should not be null", 
                   srvConf.getDurableSubscriptionClientId() != null);
        
        JMSSessionPoolConfigPolicy sessConf = jmsDestConf.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 10...", sessConf.getLowWaterMark() == 10);
        assertTrue("JMS Session pool HighWaterMark should be 5000...", sessConf.getHighWaterMark() == 5000);
    }
    
    public void checkDefaultAddressFields(JMSAddressPolicyType addrPolicy) throws Exception {
        assertNotNull("JMSAddress cannot be null ", addrPolicy);

        assertNull("JMSAddress: connectionUserName should be null",
                   addrPolicy.getConnectionUserName());
        assertNull("JMSAddress: connectionPassword should be null",
                   addrPolicy.getConnectionPassword());        
        assertNull("JMSAddress: JndiReplyDestinationName should be null",
                   addrPolicy.getJndiReplyDestinationName());
        assertNotNull("JMSAddress: JndiDestinationName should not be null",
                   addrPolicy.getJndiDestinationName());
        assertNotNull("JMSAddress: jndiConnectionFactoryName should not be null",
                   addrPolicy.getJndiConnectionFactoryName());
        assertEquals(addrPolicy.getDestinationStyle().value(), JMSConstants.JMS_QUEUE);


        assertNotNull("JMSAddress:  jmsNaming should not be null ",
                   addrPolicy.getJMSNamingProperty());
    }

    public void checkNonDefaultAddressFields(JMSAddressPolicyType addrPolicy) throws Exception {

        assertNotNull("JMSAddress cannot be null ", addrPolicy);

        assertEquals("JMSAddress: connectionUserName should be testUser",
                   "testUser", addrPolicy.getConnectionUserName());
        assertEquals("JMSAddress: connectionPassword should be testPassword",
                   "testPassword", addrPolicy.getConnectionPassword());
        assertEquals("JMSAddress: JndiReplyDestinationName should be myOwnReplyDestination",
                   "myOwnReplyDestination", addrPolicy.getJndiReplyDestinationName());
        assertEquals("JMSAddress: JndiDestinationName should be myOwnDestination",
                   "myOwnDestination", addrPolicy.getJndiDestinationName());
        assertEquals("JMSAddress: jndiConnectionFactoryName should be MockConnectionFactory",
                   "MockConnectionFactory", addrPolicy.getJndiConnectionFactoryName());
        assertEquals("JMSAddress: destinationStyle should be queue",
                   addrPolicy.getDestinationStyle().value(), JMSConstants.JMS_QUEUE);

        assertNotNull("JMSAddress:  jmsNaming should not be null ",
                   addrPolicy.getJMSNamingProperty());
    }

}
