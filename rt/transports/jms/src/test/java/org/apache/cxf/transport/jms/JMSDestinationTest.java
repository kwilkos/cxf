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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.MessageObserver;
import org.easymock.classextension.EasyMock;

public class JMSDestinationTest extends AbstractJMSTester {
    private Message destMessage;
    
    public JMSDestinationTest(String name) {
        super(name);        
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JMSDestinationTest.class);
        return  new JMSBrokerSetup(suite, "tcp://localhost:61500");        
    }
    
    private void waitForReceiveInMessage() {       
        int waitTime = 0;
        while (inMessage == null && waitTime < 3000) {
            try {
                Thread.sleep(1000);                
            } catch (InterruptedException e) {
                // do nothing here
            }
            waitTime = waitTime + 1000;
        }
        assertTrue("Can't receive the Conduit Message in 3 seconds", inMessage != null);
    }
    
    private void waitForReceiveDestMessage() {       
        int waitTime = 0;
        while (destMessage == null && waitTime < 3000) {
            try {
                Thread.sleep(1000);                
            } catch (InterruptedException e) {
                // do nothing here
            }
            waitTime = waitTime + 1000;
        }
        assertTrue("Can't receive the Destination message in 3 seconds", destMessage != null);
    }
    
    
    
    public JMSDestination setupJMSDestination(boolean send) throws IOException {
        ConduitInitiator conduitInitiator = EasyMock.createMock(ConduitInitiator.class);
        JMSDestination jmsDestination = new JMSDestination(bus, conduitInitiator, endpointInfo);
        if (send) {
            // setMessageObserver
            observer = new MessageObserver() {
                public void onMessage(Message m) {                    
                    destMessage = m;                                    
                }
            };
            jmsDestination.setMessageObserver(observer);
        }
        return jmsDestination;
    }
    
    public void testOneWayDestination() throws Exception {
        destMessage = null;
        inMessage = null;
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HWStaticReplyQBinMsgService", 
                         "HWStaticReplyQBinMsgPort");
        JMSConduit conduit = setupJMSConduit(true, false);
        Message outMessage = new MessageImpl();
        JMSDestination destination = null;
        try {
            destination = setupJMSDestination(true);        
            //destination.activate();
        } catch (IOException e) {
            assertFalse("The JMSDestination activate should not through exception ", false);            
            e.printStackTrace();
        }        
        sendoutMessage(conduit, outMessage, true);  
        // wait for the message to be get from the destination
        waitForReceiveDestMessage();
        // just verify the Destination inMessage
        assertTrue("The destiantion should have got the message ", destMessage != null);
        verifyDestinationReceivedMessage(destMessage);
        destination.shutdown();
    }
    
    private void verifyDestinationReceivedMessage(Message inMessage) {
        ByteArrayInputStream bis = 
            (ByteArrayInputStream) inMessage.getContent(InputStream.class);
        byte bytes[] = new byte[bis.available()];
        try {
            bis.read(bytes);
        } catch (IOException ex) {
            assertFalse("Read the Destination recieved Message error ", false);
            ex.printStackTrace();
        }
        String reponse = new String(bytes);
        assertEquals("The reponse date should be equals", reponse, "HelloWorld");
        
        //REVISIT we should check the message header in message context leavel
        /*  
         JMSMessageHeadersType outHeader =
            (JMSMessageHeadersType)outMessage.get(JMSConstants.JMS_CLIENT_REQUEST_HEADERS);
        
         JMSMessageHeadersType inHeader =
            (JMSMessageHeadersType)inMessage.get(JMSConstants.JMS_SERVER_HEADERS); 
        
        System.out.println("outHeader" + outHeader);
        System.out.println("inHeader" + inHeader);
        
        assertEquals("The inMessage and outMessage JMS Header's CorrelationID should be equals", 
                     outHeader.getJMSCorrelationID(), inHeader.getJMSCorrelationID());
        assertEquals("The inMessage and outMessage JMS Header's JMSExpiration should be equals", 
                     outHeader.getJMSExpiration(), inHeader.getJMSExpiration());
        assertEquals("The inMessage and outMessage JMS Header's JMSMessageID should be equals", 
                     outHeader.getJMSMessageID(), inHeader.getJMSMessageID());
        assertEquals("The inMessage and outMessage JMS Header's JMSTimeStamp should be equals", 
                     outHeader.getJMSTimeStamp(), inHeader.getJMSTimeStamp());
        assertEquals("The inMessage and outMessage JMS Header's JMSType should be equals", 
                     outHeader.getJMSType(), inHeader.getJMSType());*/
        
    }
    
    public void testRoundTripDestination() throws Exception {
       
        inMessage = null;
        setupServiceInfo("http://cxf.apache.org/hello_world_jms", 
                         "/wsdl/jms_test.wsdl", 
                         "HelloWorldService", 
                         "HelloWorldPort");
        //set up the conduit send to be true 
        JMSConduit conduit = setupJMSConduit(true, false);
        Message outMessage = new MessageImpl();
        
        final JMSDestination destination = setupJMSDestination(true);
        
        //set up MessageObserver for handlering the conduit message
        MessageObserver observer = new MessageObserver() {
            public void onMessage(Message m) {                    
                verifyDestinationReceivedMessage(m);
                //setup the message for 
                Conduit backConduit;
                try {
                    backConduit = destination.getBackChannel(m, null, null);                 
                    //wait for the message to be got from the conduit
                    Message replyMessage = new MessageImpl();
                    sendoutMessage(backConduit, replyMessage, true);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        destination.setMessageObserver(observer);  
        //set is oneway false for get response from destination
        sendoutMessage(conduit, outMessage, false);        
        //wait for the message to be got from the destination, 
        // create the thread to handler the Destination incomming message
               
        waitForReceiveInMessage();
        verifyDestinationReceivedMessage(inMessage);        
        destination.shutdown();
    }
    

}
