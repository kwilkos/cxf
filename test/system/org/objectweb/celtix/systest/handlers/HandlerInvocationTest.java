package org.objectweb.celtix.systest.handlers;



import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.systest.basic.Server;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;

public class HandlerInvocationTest extends ClientServerTestBase {
    
    private static final String HANDLER_RESPONSE_VAL = "response from handler"; 

    private QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");    
    private QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");
    
    private URL wsdl;
    private SOAPService service;
    private Greeter greeter;
    
    public void setUp() {
        try { 
            super.setUp();
            
            wsdl = RegistrationTest.class.getResource("/hello_world.wsdl");
            service = new SOAPService(wsdl, serviceName);
            greeter = (Greeter) service.getPort(portName, Greeter.class);
            
            launchServer(Server.class);

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    
    public void testLogicalHandlerInvoked() { 
        
        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>();        
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext> ();
        
        addHandlersToChain((BindingProvider)greeter, handler1, handler2);
        
        // send message and receive response
        String resp = greeter.greetMe("test handler");
        // need to add support for setting the payload 
        // once that is done a 
        assertNotNull(resp);      
        
        assertTrue("handle message was not invoked", handler1.isHandleMessageInvoked());
        assertTrue("handle message was not invoked", handler2.isHandleMessageInvoked());
    }
    

    public void testLogicalHandlerStopProcessing() {

        TestHandler<LogicalMessageContext> handler1 = new TestHandler<LogicalMessageContext>() {
            public boolean handleMessage(LogicalMessageContext ctx) {
                super.handleMessage(ctx);
                
                try {
                    Boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY); 
                    if (!outbound) {
                        LogicalMessage msg = ctx.getMessage();
                        assertNotNull("logical message is null", msg);
                        JAXBContext jaxbCtx = JAXBContext.newInstance(GreetMe.class.getPackage().getName());

                        GreetMeResponse resp = new GreetMeResponse();
                        resp.setResponseType(HANDLER_RESPONSE_VAL); 
                        msg.setPayload(resp, jaxbCtx);
                    }

                } catch (Exception e) {
                    e.printStackTrace(); 
                    fail(e.toString());
                }

                return false;
            }
        };        
        TestHandler<LogicalMessageContext>  handler2 = new TestHandler<LogicalMessageContext> ();
        
        addHandlersToChain((BindingProvider)greeter, handler1, handler2);

        String resp = greeter.sayHi();
        assertEquals(HANDLER_RESPONSE_VAL, resp);      
        
        assertEquals("handler must be invoked for inbound and outbound message", 
                     2, handler1.getHandleMessageInvoked());
        assertTrue("second handler should not be invoked", !handler2.isHandleMessageInvoked());
    }
    
    private void addHandlersToChain(BindingProvider bp, Handler...handlers) { 
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        assertNotNull(handlerChain);        
        for (Handler h : handlers) {
            handlerChain.add(h);
        }
    }
}
