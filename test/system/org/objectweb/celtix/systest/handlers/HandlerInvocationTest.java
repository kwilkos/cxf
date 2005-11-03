package org.objectweb.celtix.systest.handlers;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.handler_test.HandlerTest;
import org.objectweb.handler_test.HandlerTestService;
import org.objectweb.handler_test.PingException;
import org.objectweb.handler_test.types.PingResponse;
import org.objectweb.hello_world_soap_http.types.GreetMe;


public class HandlerInvocationTest extends ClientServerTestBase {
    
    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", 
                                          "HandlerTestService");    
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");
    
    private URL wsdl;
    private HandlerTestService service;
    private HandlerTest handlerTest;

    protected void onetimeSetUp() { 
        launchServer(Server.class);
    } 


    public void setUp() throws BusException {
        try { 
            super.setUp();
            
            wsdl = HandlerInvocationTest.class.getResource("/handler_test.wsdl");
            service = new HandlerTestService(wsdl, serviceName);
            handlerTest = (HandlerTest) service.getPort(portName, HandlerTest.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }


    public void testHandlersInvoked() throws PingException { 
        
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);        
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext> (false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false); 
        TestSOAPHandler soapHandler2 = new TestSOAPHandler(false); 

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2, soapHandler1, soapHandler2);
        
        List<String> resp = handlerTest.ping();
        assertNotNull(resp);      

        assertTrue("handle message was not invoked", handler1.isHandleMessageInvoked());
        assertTrue("handle message was not invoked", handler2.isHandleMessageInvoked());
        assertTrue("handle message was not invoked", soapHandler1.isHandleMessageInvoked());
        assertTrue("handle message was not invoked", soapHandler2.isHandleMessageInvoked());
        assertTrue("close must be  called", handler1.isCloseInvoked());
        assertTrue("close must be  called", handler2.isCloseInvoked());
        assertTrue("close must be  called", soapHandler1.isCloseInvoked());
        assertTrue("close must be  called", soapHandler2.isCloseInvoked());

        // the server has encoded into the response the order in
        // which the handlers have been invoked, parse it and make
        // sure everything is ok
        
        // expected order for inbound interceptors
        //
        
        String[] handlerNames = {"soapHandler4", "soapHandler3", "handler2", "handler1", 
                                 "servant", 
                                 "handler1", "handler2", "soapHandler3", "soapHandler4"}; 

        assertEquals(handlerNames.length, resp.size()); 

        Iterator iter = resp.iterator();
        for (String expected : handlerNames) {
            assertEquals(expected, iter.next());
        }
    }
    

    public void testLogicalHandlerStopProcessing() throws PingException {

        final String clientHandlerMessage = "handler2 client side"; 

        TestHandler<LogicalMessageContext>  handler1 = new TestHandler<LogicalMessageContext> (false);
        TestHandler<LogicalMessageContext> handler2 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                try {
                    Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY); 
                    if (outbound) {
                        LogicalMessage msg = ctx.getMessage();
                        assertNotNull("logical message is null", msg);
                        JAXBContext jaxbCtx = JAXBContext.newInstance(GreetMe.class.getPackage().getName());
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
        
        assertEquals("handler must be invoked for inbound & outbound message", 
                     2, handler1.getHandleMessageInvoked());
        assertEquals("second handler must be invoked exactly once", 1, handler2.getHandleMessageInvoked());
        assertTrue("close must be  called", handler1.isCloseInvoked());
        assertTrue("close must be called", handler2.isCloseInvoked());
    }


    public void testLogicalHandlerStopProcessingServerSide() throws PingException {

        String[] expectedHandlers = {"soapHandler4", "soapHandler3", "handler2", 
                                     "soapHandler3", "soapHandler4"};

        List<String> resp = handlerTest.pingWithArgs("handler2 inbound stop");

        assertEquals(expectedHandlers.length, resp.size());
       
        int i = 0;
        for (String expected : expectedHandlers) {
            assertEquals(expected, resp.get(i++));
        }


        String[] expectedHandlers1 = {"soapHandler4", "soapHandler3", "soapHandler4"};
        resp = handlerTest.pingWithArgs("soapHandler3 inbound stop");
        assertEquals(expectedHandlers1.length, resp.size());
        i = 0;
        for (String expected : expectedHandlers1) {
            assertEquals(expected, resp.get(i++));
        }
    }


    public void testLogicalHandlerThrowsProtocolException() throws Exception { 

        final String clientHandlerMessage = "handler1 client side"; 

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false) {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                throw new ProtocolException(clientHandlerMessage); 
            }
        };        
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext> (false);
        
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



    public void testLogicalHandlerThrowsProtocolExceptionServerSide() throws PingException {
        try {
            handlerTest.pingWithArgs("handler2 inbound throw javax.xml.ws.ProtocolException");
            fail("did not get expected exception");
        } catch (ProtocolException e) {
            // happy now
        }
    }


    public void testLogicalHandlerHandlerFault() { 

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);
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


    public void testLogicalHandlerOneWay() {
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>(false);
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext>(false);
        TestSOAPHandler soapHandler1 = new TestSOAPHandler(false);  

        addHandlersToChain((BindingProvider)handlerTest, handler1, handler2, soapHandler1);

        handlerTest.pingOneWay(); 

        assertEquals(1, handler1.getHandleMessageInvoked());
        assertEquals(1, handler2.getHandleMessageInvoked());
        assertEquals(1, soapHandler1.getHandleMessageInvoked());
    }

    void addHandlersToChain(BindingProvider bp, Handler...handlers) { 
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        assertNotNull(handlerChain);        
        for (Handler h : handlers) {
            handlerChain.add(h);
        }
    }
}
