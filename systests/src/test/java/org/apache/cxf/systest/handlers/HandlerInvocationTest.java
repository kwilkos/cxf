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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.BusException;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.handler_test.HandlerTest;
import org.apache.handler_test.HandlerTestService;
import org.apache.handler_test.PingException;
import org.apache.handler_test.types.PingOneWay;
import org.apache.handler_test.types.PingResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class HandlerInvocationTest extends AbstractBusClientServerTestBase {

    private final QName serviceName = new QName("http://apache.org/handler_test", "HandlerTestService");
    private final QName portName = new QName("http://apache.org/handler_test", "SoapPort");

    private URL wsdl;
    private HandlerTestService service;
    private HandlerTest handlerTest;

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    @Before
    public void setUp() throws BusException {
        try {
            super.createBus();

            wsdl = HandlerInvocationTest.class.getResource("/wsdl/handler_test.wsdl");
            service = new HandlerTestService(wsdl, serviceName);
            handlerTest = service.getPort(portName, HandlerTest.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    @Test
    public void testAddHandlerThroughHandlerResolverClientSide() {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);

        MyHandlerResolver myHandlerResolver = new MyHandlerResolver(handler1, handler2);

        service.setHandlerResolver(myHandlerResolver);

        HandlerTest handlerTestNew = service.getPort(portName, HandlerTest.class);

        handlerTestNew.pingOneWay();

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
    }

    @Test
    public void testLogicalHandlerOneWay() {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        handlerTest.pingOneWay();

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
    }
    
    @Test
    @Ignore
    public void testLogicalHandlerTwoWay() throws Exception {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        handlerTest.pingWithArgs("hello");

        assertEquals(2, handler1.getHandleMessageInvoked());
        assertEquals(2, handler2.getHandleMessageInvoked());
    }

    @Test
    public void testSOAPHandlerHandleMessageReturnTrueClientSide() throws Exception {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);
        TestSOAPHandler soapHandler2 = new TestSOAPHandler(false);

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2, soapHandler1, soapHandler2);

        List<String> resp = handlerTest.ping();
        assertNotNull(resp);

        assertEquals("handle message was not invoked", 2, handler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, handler2.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler2.getHandleMessageInvoked());
        assertTrue("close must be  called", handler1.isCloseInvoked());
        assertTrue("close must be  called", handler2.isCloseInvoked());
        assertTrue("close must be  called", soapHandler1.isCloseInvoked());
        assertTrue("close must be  called", soapHandler2.isCloseInvoked());

        // the server has encoded into the response the order in
        // which the handlers have been invoked, parse it and make
        // sure everything is ok
        // expected order for inbound interceptors
        String[] handlerNames = {"soapHandler4", "soapHandler3", "handler2", "handler1", "servant",
                                 "handler1", "handler2", "soapHandler3", "soapHandler4"};

        assertEquals(handlerNames.length, resp.size());

        Iterator iter = resp.iterator();
        for (String expected : handlerNames) {
            assertEquals(expected, iter.next());
        }
    }
    
    @Test
    public void testLogicalHandlerHandleMessageReturnFalseClientSide() throws Exception {
        final String clientHandlerMessage = "handler2 client side";

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                try {
                    Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                    if (outbound) {
                        LogicalMessage msg = ctx.getMessage();
                        assertNotNull("logical message is null", msg);
                        JAXBContext jaxbCtx = JAXBContext.newInstance(PackageUtils
                            .getPackageName(PingOneWay.class));
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

        assertEquals("handler must be invoked for inbound & outbound message", 2, handler1
            .getHandleMessageInvoked());

        assertEquals("the second handler must be invoked once", 1, handler2.getHandleMessageInvoked());

        /*
         * assertTrue("close must be called", handler1.isCloseInvoked());
         * assertTrue("close must be called", handler2.isCloseInvoked());
         */
    }

    @Test
    public void testLogicalHandlerHandleMessageReturnsFalseServerSide() throws PingException {
        //FIXME: the actual invoking sequence are ("soapHandler4", "soapHandler3", "handler2", 
        //"soapHandler3", "soapHandler4", "soapHandler3", "soapHandler4"). The 4th and 5th handlers
        //are called from invokeReversedHandlerMessage. This needs to be fixed.
        String[] expectedHandlers = {"soapHandler4", "soapHandler3", "handler2", "soapHandler3",
                                     "soapHandler4"};

        List<String> resp = handlerTest.pingWithArgs("handler2 inbound stop");

        assertEquals(expectedHandlers.length, resp.size());

        int i = 0;
        for (String expected : expectedHandlers) {
            assertEquals(expected, resp.get(i++));
        }
    }
    
    @Test
    public void testSOAPHandlerHandleMessageReturnsFalseServerSide() throws PingException {
        //FIXME: the actual invoking sequence are ("soapHandler4", "soapHandler3", "soapHandler4", 
        //"soapHandler3", "soapHandler4"). The 3rd was called by invokeReversedHandlerMessage. 
        //the 4th and 5th were called when sending out outbound message. We should fix this by removing
        //the 4th and 5th calls. 
        String[] expectedHandlers = {"soapHandler4", "soapHandler3", "soapHandler4", "soapHandler3",
                                     "soapHandler4"};
        List<String> resp = handlerTest.pingWithArgs("soapHandler3 inbound stop");
        assertEquals(expectedHandlers.length, resp.size());
        int i = 0;
        for (String expected : expectedHandlers) {
            assertEquals(expected, resp.get(i++));
        }
    }

    @Test
    public void testLogicalHandlerHandleMessageThrowsProtocolExceptionClientSide() throws Exception {

        final String clientHandlerMessage = "handler1 client side";

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                throw new ProtocolException(clientHandlerMessage);
            }
        };

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        try {
            handlerTest.ping();
            fail("did not get expected exception");
        } catch (ProtocolException e) {
            assertEquals(clientHandlerMessage, e.getMessage());
        }

        assertEquals(0, handler2.getHandleFaultInvoked());
        assertEquals(1, handler1.getHandleFaultInvoked());

        assertEquals(1, handler1.getCloseInvoked());
        assertEquals(1, handler2.getCloseInvoked());
    }

    // TODO: commented out due to CXF-333
    @Test
    @Ignore
    public void testLogicalHandlerThrowsProtocolExceptionServerSide() throws PingException {
        try {
            handlerTest.pingWithArgs("handler2 inbound throw javax.xml.ws.ProtocolException");
            fail("did not get expected exception");
        } catch (ProtocolException e) {
            // happy now
        }
    }

    @Test
    public void testLogicalHandlerHandleMessageThrowsRuntimeExceptionClientSide() throws Exception {
        final String clientHandlerMessage = "handler1 client side";

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                throw new RuntimeException(clientHandlerMessage);
            }
        };

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        try {
            handlerTest.ping();
            fail("did not get expected exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(clientHandlerMessage));
        }

        assertEquals(0, handler2.getHandleFaultInvoked());
        assertEquals(0, handler1.getHandleFaultInvoked());

        assertEquals(1, handler1.getCloseInvoked());
        assertEquals(1, handler2.getCloseInvoked());
    }

    @Test
    public void testSOAPHandlerHandleMessageReturnFalseClientSide() throws Exception {
        final String clientHandlerMessage = "client side";
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                try {
                    Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                    if (outbound) {
                        LogicalMessage msg = ctx.getMessage();
                        assertNotNull("logical message is null", msg);
                        JAXBContext jaxbCtx = JAXBContext.newInstance(PackageUtils
                            .getPackageName(PingOneWay.class));
                        PingResponse resp = new PingResponse();
                        resp.getHandlersInfo().add(clientHandlerMessage);

                        msg.setPayload(resp, jaxbCtx);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.toString());
                }
                return true;
            }
        };
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);
        TestSOAPHandler soapHandler2 = new TestSOAPHandler<SOAPMessageContext>(false) {
            public boolean handleMessage(SOAPMessageContext ctx) {
                super.handleMessage(ctx);

                return false;
            }
        };
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2, soapHandler1, soapHandler2);

        List<String> resp = handlerTest.ping();
        assertEquals(clientHandlerMessage, resp.get(0));

        assertEquals(3, handler1.getHandleMessageInvoked());
        assertEquals(3, handler2.getHandleMessageInvoked());
        assertEquals(2, soapHandler1.getHandleMessageInvoked());
        assertEquals(1, soapHandler2.getHandleMessageInvoked());
    }

    @Test
    @Ignore
    public void testLogicalHandlerHandlerFaultServerSide() {

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2);

        try {
            handlerTest.pingWithArgs("servant throw exception");
            fail("did not get expected PingException");
        } catch (PingException e) {
            assertTrue(e.getMessage().contains("from servant"));
        }

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
        assertEquals(1, handler1.getHandleFaultInvoked());
        assertEquals(1, handler2.getHandleFaultInvoked());
    }

    @Test
    @Ignore
    public void testDescription() throws PingException {
        TestHandler<LogicalMessageContext> handler = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                assertTrue("wsdl description not found or invalid", isValidWsdlDescription(ctx
                    .get(MessageContext.WSDL_DESCRIPTION)));
                return true;
            }
        };
        TestSOAPHandler soapHandler = new TestSOAPHandler<SOAPMessageContext>(false) {
            public boolean handleMessage(SOAPMessageContext ctx) {
                super.handleMessage(ctx);
                assertTrue("wsdl description not found or invalid", isValidWsdlDescription(ctx
                    .get(MessageContext.WSDL_DESCRIPTION)));
                return true;
            }
        };

        addHandlersToChain((BindingProvider)handlerTest, handler, soapHandler);

        List<String> resp = handlerTest.ping();
        assertNotNull(resp);

        assertEquals("handler was not invoked", 2, handler.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler.getHandleMessageInvoked());
        assertTrue("close must be  called", handler.isCloseInvoked());
        assertTrue("close must be  called", soapHandler.isCloseInvoked());
    }

    @Test
    public void testHandlersInvokedForDispatch() throws Exception {

        Dispatch<SOAPMessage> disp = service
            .createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);
        TestSOAPHandler soapHandler2 = new TestSOAPHandler(false);

        addHandlersToChain((BindingProvider)disp, handler1, handler2, soapHandler1, soapHandler2);

        InputStream is = getClass().getResourceAsStream("PingReq.xml");
        SOAPMessage outMsg = MessageFactory.newInstance().createMessage(null, is);

        SOAPMessage inMsg = disp.invoke(outMsg);
        assertNotNull(inMsg);

        assertEquals("handle message was not invoked", 2, handler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, handler2.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler1.getHandleMessageInvoked());
        assertEquals("handle message was not invoked", 2, soapHandler2.getHandleMessageInvoked());
        // TODO: commented out, need to fix
        // assertTrue("close must be called", handler1.isCloseInvoked());
        // assertTrue("close must be called", handler2.isCloseInvoked());
        // assertTrue("close must be called", soapHandler1.isCloseInvoked());
        // assertTrue("close must be called", soapHandler2.isCloseInvoked());

        // the server has encoded into the response the order in
        // which the handlers have been invoked, parse it and make
        // sure everything is ok

        // expected order for inbound interceptors
        String[] handlerNames = {"soapHandler4", "soapHandler3", "handler2", "handler1", "servant",
                                 "handler1", "handler2", "soapHandler3", "soapHandler4"};

        List<String> resp = getHandlerNames(inMsg.getSOAPBody().getChildNodes());
        assertEquals(handlerNames.length, resp.size());

        Iterator iter = resp.iterator();
        for (String expected : handlerNames) {
            assertEquals(expected, iter.next());
        }
    }

    void addHandlersToChain(BindingProvider bp, Handler... handlers) {
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

    public class MyHandlerResolver implements HandlerResolver {
        List<Handler> chain = new ArrayList<Handler>();

        public MyHandlerResolver(Handler... handlers) {
            for (Handler h : handlers) {
                chain.add(h);
            }
        }

        public List<Handler> getHandlerChain(PortInfo portInfo) {
            return chain;
        }

    }

    private boolean isValidWsdlDescription(Object wsdlDescription) {
        return (wsdlDescription != null)
               && ((wsdlDescription instanceof java.net.URI) || (wsdlDescription instanceof java.net.URL));
    }
}
