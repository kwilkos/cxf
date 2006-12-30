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

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.BusException;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.handler_test.HandlerTest;
import org.apache.handler_test.HandlerTestService;
import org.apache.handler_test.PingException;
import org.apache.handler_test.types.PingOneWay;
import org.apache.handler_test.types.PingResponse;


public class HandlerInvocationTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://apache.org/handler_test",
                                          "HandlerTestService");
    private final QName portName = new QName("http://apache.org/handler_test", "SoapPort");

    private URL wsdl;
    private HandlerTestService service;
    private HandlerTest handlerTest;


    public static Test suite() throws Exception {        
        TestSuite suite = new TestSuite(HandlerInvocationTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }


    public void setUp() throws BusException {
        try {
            super.setUp();

            wsdl = HandlerInvocationTest.class.getResource("/wsdl/handler_test.wsdl");
            service = new HandlerTestService(wsdl, serviceName);
            handlerTest = service.getPort(portName, HandlerTest.class);
            if (!"testHandlersInvoked".equals(getName())) {
                //addHandlersToChain((BindingProvider)handlerTest, new TestStreamHandler(false));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    public void testLogicalHandlerOneWay() {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        handlerTest.pingOneWay();

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
    }
    
    public void testLogicalHandlerTwoWay() throws Exception {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        handlerTest.pingWithArgs("hello");

        assertEquals(2, handler1.getHandleMessageInvoked());
        assertEquals(2, handler2.getHandleMessageInvoked());
    }
 
    public void testLogicalHandlerStopProcessingClientSide() throws Exception {
        final String clientHandlerMessage = "handler2 client side";

        TestHandler<LogicalMessageContext>  handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                try {
                    Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                    if (outbound) {
                        LogicalMessage msg = ctx.getMessage();
                        assertNotNull("logical message is null", msg);
                        JAXBContext jaxbCtx = 
                            JAXBContext.newInstance(PackageUtils.getPackageName(PingOneWay.class));
                        PingResponse resp = new PingResponse();
                        resp.getHandlersInfo().add(clientHandlerMessage);

                        msg.setPayload(resp, jaxbCtx);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.toString());
                }
                return false;
            }
        };

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        List<String> resp = handlerTest.ping();
        assertEquals(clientHandlerMessage, resp.get(0));

        //TODO: Following are commented out due to CXF-332
        /*assertEquals("handler must be invoked for inbound & outbound message",
                     2, handler1.getHandleMessageInvoked());*/
        
        assertEquals("second handler must be invoked exactly once", 1, handler2.getHandleMessageInvoked());
        //assertTrue("close must be  called", handler1.isCloseInvoked());
        //assertTrue("close must be called", handler2.isCloseInvoked());
    }
    
    public void testLogicalHandlerStopProcessingServerSide() throws PingException {
        //TODO: Following are commented out due to CXF-332
        /*        
        String[] expectedHandlers = {"streamHandler5", "soapHandler4", "soapHandler3", "handler2",
                                     "soapHandler3", "soapHandler4"};

        List<String> resp = handlerTest.pingWithArgs("handler2 inbound stop");

        assertEquals(expectedHandlers.length, resp.size());

        int i = 0;
        for (String expected : expectedHandlers) {
            assertEquals(expected, resp.get(i++));
        }


        String[] expectedHandlers1 = {"streamHandler5", "soapHandler4", "soapHandler3", "soapHandler4"};
        resp = handlerTest.pingWithArgs("soapHandler3 inbound stop");
        assertEquals(expectedHandlers1.length, resp.size());
        i = 0;
        for (String expected : expectedHandlers1) {
            assertEquals(expected, resp.get(i++));
        }
        */
    }

    //TODO: commented out due to CXF-333
    public void xtestLogicalHandlerThrowsProtocolExceptionClientSide() throws Exception {

        final String clientHandlerMessage = "handler1 client side";

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                throw new ProtocolException(clientHandlerMessage);
            }
        };
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        try {
            handlerTest.ping();
            fail("did not get expected exception");
        } catch (ProtocolException e) {
            assertEquals(clientHandlerMessage, e.getMessage());
        }
        assertTrue(!handler2.isHandleFaultInvoked());
        assertTrue(handler1.isCloseInvoked());
        assertTrue(!handler2.isCloseInvoked());
    }

    //TODO: commented out due to CXF-333
    public void xtestLogicalHandlerThrowsProtocolExceptionServerSide() throws PingException {
        try {
            handlerTest.pingWithArgs("handler2 inbound throw javax.xml.ws.ProtocolException");
            fail("did not get expected exception");
        } catch (ProtocolException e) {
            // happy now
        }
    }

    public void xtestLogicalHandlerHandlerFault() {

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);
//        TestStreamHandler streamHandler = new TestStreamHandler(false);
//        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2, streamHandler);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);
        try {
            handlerTest.pingWithArgs("servant throw exception");
            fail("did not get expected PingException");
        } catch (PingException e) {
            assertTrue(e.getMessage().contains("from servant"));
        }

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
        //assertEquals(1, streamHandler.getHandleMessageInvoked());
        assertEquals(1, handler1.getHandleFaultInvoked());
        assertEquals(1, handler2.getHandleFaultInvoked());
        //assertEquals(1, streamHandler.getHandleFaultInvoked());
    }
       
    public void xtestHandlersInvoked() throws Exception {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);
        TestSOAPHandler soapHandler2 = new TestSOAPHandler(false);
//        TestStreamHandler streamHandler = new TestStreamHandler(false);

//        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2,
//                           soapHandler1, soapHandler2,
//                           streamHandler);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2,
                           soapHandler1, soapHandler2);

        List<String> resp = handlerTest.ping();
        assertNotNull(resp);

        assertEquals("handle message was not invoked", 2, handler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, handler2.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler2.getHandleMessageInvoked());
        //assertEquals("handle message was not invoked", 2, streamHandler.getHandleMessageInvoked());
        assertTrue("close must be  called", handler1.isCloseInvoked());
        assertTrue("close must be  called", handler2.isCloseInvoked());
        assertTrue("close must be  called", soapHandler1.isCloseInvoked());
        assertTrue("close must be  called", soapHandler2.isCloseInvoked());
        //assertTrue("close must be  called", streamHandler.isCloseInvoked());

        // the server has encoded into the response the order in
        // which the handlers have been invoked, parse it and make
        // sure everything is ok

        // expected order for inbound interceptors
        //
        // note: the stream handler does not add itself to the list on
        // the way out of the server.  It compresses the message so
        // the fact that we are here indicates that it has
        // participated correctly in the message exchange.
        String[] handlerNames = {"streamHandler5", "soapHandler4", "soapHandler3", "handler2", "handler1",
                                 "servant",
                                 "handler1", "handler2", "soapHandler3", "soapHandler4"};

        assertEquals(handlerNames.length, resp.size());

        Iterator iter = resp.iterator();
        for (String expected : handlerNames) {
            assertEquals(expected, iter.next());
        }
    }
       
    
/*
    public void testDescription() throws PingException { 
        TestHandler<LogicalMessageContext> handler = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                assertTrue("wsdl description not found or invalid",
                           isValidWsdlDescription(ctx.get(MessageContext.WSDL_DESCRIPTION)));
                return true;
            }
        };
        TestSOAPHandler soapHandler = new TestSOAPHandler<SOAPMessageContext>(false) {
            public boolean handleMessage(SOAPMessageContext ctx) {
                super.handleMessage(ctx);
                assertTrue("wsdl description not found or invalid",
                           isValidWsdlDescription(ctx.get(MessageContext.WSDL_DESCRIPTION)));
                return true;
            }
        };           
        TestStreamHandler streamHandler = new TestStreamHandler(false) {
            public boolean handleMessage(StreamMessageContext ctx) {
                super.handleMessage(ctx);
                assertTrue("wsdl description not found or invalid",
                           isValidWsdlDescription(ctx.get(MessageContext.WSDL_DESCRIPTION)));
                return false;
            }
        };           

        addHandlersToChain((BindingProvider)handlerTest, handler, soapHandler, streamHandler);

        List<String> resp = handlerTest.ping();
        assertNotNull(resp); 
        
        assertEquals("handler was not invoked", 2, handler.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler.getHandleMessageInvoked());
        assertEquals("handler was not invoked", 1, streamHandler.getHandleMessageInvoked());
        assertTrue("close must be  called", handler.isCloseInvoked());
        assertTrue("close must be  called", soapHandler.isCloseInvoked());
        assertTrue("close must be called", streamHandler.isCloseInvoked());
    }
    
    public void testHandlersInvokedForDispatch() throws Exception {

        Dispatch<SOAPMessage> disp = service.createDispatch(portName,
                                                            SOAPMessage.class,
                                                            Service.Mode.MESSAGE);
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);
        TestSOAPHandler soapHandler2 = new TestSOAPHandler(false);
        TestStreamHandler streamHandler = new TestStreamHandler(false);

        addHandlersToChain((BindingProvider)disp, handler1, handler2,
                           soapHandler1, soapHandler2,
                           streamHandler);

        InputStream is =  getClass().getResourceAsStream("PingReq.xml");
        SOAPMessage outMsg = MessageFactory.newInstance().createMessage(null, is);

        SOAPMessage inMsg = disp.invoke(outMsg);
        assertNotNull(inMsg);

        assertEquals("handle message was not invoked", 2, handler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, handler2.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler2.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, streamHandler.getHandleMessageInvoked());
        assertTrue("close must be  called", handler1.isCloseInvoked());
        assertTrue("close must be  called", handler2.isCloseInvoked());
        assertTrue("close must be  called", soapHandler1.isCloseInvoked());
        assertTrue("close must be  called", soapHandler2.isCloseInvoked());
        assertTrue("close must be  called", streamHandler.isCloseInvoked());

        // the server has encoded into the response the order in
        // which the handlers have been invoked, parse it and make
        // sure everything is ok

        // expected order for inbound interceptors
        //
        // note: the stream handler does not add itself to the list on
        // the way out of the server.  It compresses the message so
        // the fact that we are here indicates that it has
        // participated correctly in the message exchange.
        String[] handlerNames = {"streamHandler5", "soapHandler4", "soapHandler3", "handler2", "handler1",
                                 "servant",
                                 "handler1", "handler2", "soapHandler3", "soapHandler4"};

        List<String> resp = getHandlerNames(inMsg.getSOAPBody().getChildNodes());
        assertEquals(handlerNames.length, resp.size());

        Iterator iter = resp.iterator();
        for (String expected : handlerNames) {
            assertEquals(expected, iter.next());
        }
    }


 */


    void addHandlersToChain(BindingProvider bp, Handler...handlers) {
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        assertNotNull(handlerChain);
        for (Handler h : handlers) {
            handlerChain.add(h);
        }
    }

    List<String> getHandlerNames(NodeList nodes) throws Exception {
        List<String> stringList = null;
        Node elNode = null;
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Node n = nodes.item(idx);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elNode = n;
                break;
            }
        }

        JAXBContext jaxbCtx = JAXBContext.newInstance(PingResponse.class);
        Unmarshaller um = jaxbCtx.createUnmarshaller();
        Object obj = um.unmarshal(elNode);

        if (obj instanceof PingResponse) {
            PingResponse pr = PingResponse.class.cast(obj);
            stringList = pr.getHandlersInfo();
        }
        return stringList;
    }
    
    /*
    private boolean isValidWsdlDescription(Object wsdlDescription) {
        return (wsdlDescription != null) 
            && ((wsdlDescription instanceof java.net.URI)
                || (wsdlDescription instanceof java.net.URL));
    }
    */
}
