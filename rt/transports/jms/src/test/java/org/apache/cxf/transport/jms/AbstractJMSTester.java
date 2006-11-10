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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.namespace.QName;


import junit.framework.TestCase;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.easymock.classextension.EasyMock;

public class AbstractJMSTester extends TestCase {
    protected Bus bus;
    protected EndpointInfo endpointInfo;
    protected EndpointReferenceType target;
    protected MessageObserver observer;
    protected Message inMessage;
    
    public AbstractJMSTester(String name) {
        super(name);
    }
    
    public void setUp() {
        BusFactory bf = BusFactoryHelper.newInstance();
        bus = bf.createBus();
        bf.setDefaultBus(bus);
    }
    
    public void tearDown() {
        bus.shutdown(true);
        if (System.getProperty("cxf.config.file") != null) {
            System.clearProperty("cxf.config.file");
        }
    }
    
    protected void setupServiceInfo(String ns, String wsdl, String serviceName, String portName) {        
        URL wsdlUrl = getClass().getResource(wsdl);
        assertNotNull(wsdlUrl);
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, wsdlUrl, new QName(ns, serviceName));

        Service service = factory.create();        
        endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, portName));
   
    }
    
    protected void sendoutMessage(Conduit conduit, Message message, Boolean isOneWay) throws IOException {
        
        Exchange exchange = new ExchangeImpl();
        exchange.setOneWay(isOneWay);
        message.setExchange(exchange);
        exchange.setInMessage(message);
        try {
            conduit.send(message);
        } catch (IOException ex) {
            assertFalse("JMSConduit can't perpare to send out message", false);
            ex.printStackTrace();            
        }            
        OutputStream os = message.getContent(OutputStream.class);
        assertTrue("The OutputStream should not be null ", os != null);
        os.write("HelloWorld".getBytes());
        os.close();            
    }
    
    protected JMSConduit setupJMSConduit(boolean send, boolean decoupled) {
        if (decoupled) {
            // setup the reference type
        } else {
            target = EasyMock.createMock(EndpointReferenceType.class);
        }    
        
        JMSConduit jmsConduit = new JMSConduit(bus, endpointInfo, target);
        
        if (send) {
            // setMessageObserver
            observer = new MessageObserver() {
                public void onMessage(Message m) {                    
                    inMessage = m;
                }
            };
            jmsConduit.setMessageObserver(observer);
        }
        
        return jmsConduit;        
    }
    
       
   
   
}
