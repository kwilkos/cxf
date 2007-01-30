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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transports.jms.context.JMSMessageHeadersType;


public class JMSConduitTest extends AbstractJMSTester {
     
    
    public JMSConduitTest(String name) {
        super(name);
    }   
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JMSConduitTest.class);
        return  new JMSBrokerSetup(suite, "tcp://localhost:61500");        
    }
    
    public void testGetConfiguration() throws Exception {
        // setup the new bus to get the configuration file
        SpringBusFactory bf = new SpringBusFactory();
        bf.setDefaultBus(null);
        bus = bf.createBus("/wsdl/jms_test_config.xml");
        bf.setDefaultBus(bus);
        setupServiceInfo("http://cxf.apache.org/jms_conf_test",
                         "/wsdl/jms_test_no_addr.wsdl",
                         "HelloWorldQueueBinMsgService",
                         "HelloWorldQueueBinMsgPort");
        JMSConduit conduit = setupJMSConduit(false, false);
        assertNotNull(conduit.config);
        assertEquals("Can't get the right ClientReceiveTimeout",
                     500,
                     conduit.config.getClientConfig().getClientReceiveTimeout());
        assertEquals("Can't get the right SessionPoolConfig's LowWaterMark",
                     10,
                     conduit.base.getSessionPoolConfig().getLowWaterMark());
        assertEquals("Can't get the right AddressPolicy's ConnectionPassword",
                     "testPassword",
                     conduit.base.getAddressPolicy().getConnectionPassword());
        bf.setDefaultBus(null);
        
    }
    
    public void testPrepareSend() throws Exception {
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HelloWorldService", 
                         "HelloWorldPort");

        JMSConduit conduit = setupJMSConduit(false, false);
        Message message = new MessageImpl();
        try {
            conduit.send(message);
        } catch (Exception ex) {
            ex.printStackTrace();            
        }
        verifySentMessage(false, message);        
    }
    
    public void verifySentMessage(boolean send, Message message) {
        PooledSession pooledSession = (PooledSession)message.get(JMSConstants.JMS_POOLEDSESSION);
        OutputStream os = message.getContent(OutputStream.class);
        assertTrue("pooled Session should not be null ", pooledSession != null);
        assertTrue("OutputStream should not be null", os != null);
        
    }
    
    public void testSendOut() throws Exception {
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HelloWorldServiceLoop", 
                         "HelloWorldPortLoop");

        JMSConduit conduit = setupJMSConduit(true, false); 
        Message message = new MessageImpl();
        // set the isOneWay to false
        sendoutMessage(conduit, message, false);        
        verifyReceivedMessage(message);        
    }
    
    public void verifyReceivedMessage(Message message)  {
        ByteArrayInputStream bis = 
            (ByteArrayInputStream) inMessage.getContent(InputStream.class);
        byte bytes[] = new byte[bis.available()];
        try {
            bis.read(bytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String reponse = new String(bytes);
        assertEquals("The reponse date should be equals", reponse, "HelloWorld");
                
        JMSMessageHeadersType inHeader =
            (JMSMessageHeadersType)inMessage.get(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS); 
        
        assertTrue("The inMessage JMS Header should not be null", inHeader != null);
        
               
    }
    
    
   

}
