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

package org.apache.cxf.systest.handlers;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;


import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.handlers.AddNumbersService;
import org.apache.handlers.types.AddNumbersResponse;
import org.apache.handlers.types.ObjectFactory;
import org.junit.BeforeClass;
import org.junit.Test;


public class DispatchHandlerInvocationTest extends AbstractBusClientServerTestBase {

    static QName serviceName = new QName("http://apache.org/handlers", "AddNumbersService");
    static QName portName = new QName("http://apache.org/handlers", "AddNumbersPort");
   
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(HandlerServer.class));
    }
    
    @Test
    public void testInvokeWithJAXBPayloadMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        JAXBContext jc = JAXBContext.newInstance("org.apache.handlers.types");
        Dispatch<Object> disp = service.createDispatch(portName, jc, Service.Mode.PAYLOAD);
 
        TestHandler handler = new TestHandler();
        TestSOAPHandler soapHandler = new TestSOAPHandler();
        addHandlersProgrammatically(disp, handler, soapHandler);
      
        org.apache.handlers.types.AddNumbers req = new org.apache.handlers.types.AddNumbers();        
        req.setArg0(10);
        req.setArg1(20);        
        ObjectFactory factory = new ObjectFactory();        
        JAXBElement e = factory.createAddNumbers(req);        

        JAXBElement response = (JAXBElement)disp.invoke(e);
        assertNotNull(response);
        AddNumbersResponse value = (AddNumbersResponse)response.getValue();
        assertEquals(200, value.getReturn());
    }

    @Test
    public void testInvokeWithDOMSourcMessageMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Mode.MESSAGE);
 
        TestHandler handler = new TestHandler();
        TestSOAPHandler soapHandler = new TestSOAPHandler();
        addHandlersProgrammatically(disp, handler, soapHandler);
      
        InputStream is2 =  this.getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapReq = factory.createMessage(null, is2);
        DOMSource domReqMessage = new DOMSource(soapReq.getSOAPPart());   

        //XMLUtils.writeTo(domReqMessage, System.out);
        DOMSource response = disp.invoke(domReqMessage);
        assertNotNull(response);
    }

    @Test
    public void testInvokeWithDOMSourcPayloadMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Mode.PAYLOAD);
 
        TestHandler handler = new TestHandler();
        TestSOAPHandler soapHandler = new TestSOAPHandler();
        addHandlersProgrammatically(disp, handler, soapHandler);
      
        InputStream is2 =  this.getClass().getResourceAsStream("resources/GreetMeDocLiteralReqPayload.xml");
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapReq = factory.createMessage(null, is2);
        DOMSource domReqMessage = new DOMSource(soapReq.getSOAPPart());   

        //XMLUtils.writeTo(domReqMessage, System.out);
        DOMSource response = disp.invoke(domReqMessage);
        assertNotNull(response);
    }

    @Test
    public void testInvokeWithSOAPMessageMessageMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        Dispatch<SOAPMessage> disp = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
 
        TestHandler handler = new TestHandler();
        TestSOAPHandler soapHandler = new TestSOAPHandler();
        addHandlersProgrammatically(disp, handler, soapHandler);
      
        InputStream is2 =  this.getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapReq = factory.createMessage(null, is2);
  
        SOAPMessage response = disp.invoke(soapReq);
        assertNotNull(response);
        //response.writeTo(System.out);
    }
    
    @Test
    public void testInvokeWithSOAPMessagePayloadMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        Dispatch<SOAPMessage> disp = service.createDispatch(portName, SOAPMessage.class, Mode.PAYLOAD);
 
        TestHandler handler = new TestHandler();
        TestSOAPHandler soapHandler = new TestSOAPHandler();
        addHandlersProgrammatically(disp, handler, soapHandler);
      
        InputStream is2 =  this.getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapReq = factory.createMessage(null, is2);
  
        try {
            disp.invoke(soapReq);
            fail("Did not get expected exception");
        } catch (SOAPFaultException e) {
            assertTrue("Did not get expected exception message",  e.getMessage()
                       .indexOf("is not valid in PAYLOAD mode with SOAP/HTTP binding") > -1);
        }
    }
     
    public void addHandlersProgrammatically(BindingProvider bp, Handler...handlers) {
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        assertNotNull(handlerChain);
        for (Handler h : handlers) {
            handlerChain.add(h);
        }    
    }
    
    class TestHandler implements LogicalHandler<LogicalMessageContext> {
        public boolean handleMessage(LogicalMessageContext ctx) {
            try {
                Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                if (outbound) {
                    LogicalMessage msg = ctx.getMessage();                        
                    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                    Object payload = ((JAXBElement)msg.getPayload(jaxbContext)).getValue();
                    org.apache.handlers.types.AddNumbers req = 
                        (org.apache.handlers.types.AddNumbers)payload;

                    assertEquals(10, req.getArg0());
                    assertEquals(20, req.getArg1());
                } else {
                    LogicalMessage msg = ctx.getMessage();
                    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                    Object payload = ((JAXBElement)msg.getPayload(jaxbContext)).getValue();
                    org.apache.handlers.types.AddNumbersResponse res = 
                        (org.apache.handlers.types.AddNumbersResponse)payload;

                    assertEquals(200, res.getReturn());
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
            return true;
        }        
        public boolean handleFault(LogicalMessageContext ctx) {
            return true;
        }        
        public void close(MessageContext arg0) {
        }
    }
    
    class TestSOAPHandler implements SOAPHandler<SOAPMessageContext> {
        public boolean handleMessage(SOAPMessageContext ctx) {
            try {
                SOAPMessage msg = ctx.getMessage();
                //msg.writeTo(System.out);
                assertNotNull(msg);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }

            return true;
        }
        public final Set<QName> getHeaders() {
            return null;
        }
        public boolean handleFault(SOAPMessageContext ctx) {
            return true;
        }        
        public void close(MessageContext arg0) {
        }     
    }   
}
