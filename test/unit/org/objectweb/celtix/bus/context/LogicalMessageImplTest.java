package org.objectweb.celtix.bus.context;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.MessageContext;
import junit.framework.TestCase;

import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.handler_test.HandlerTest;
import org.objectweb.handler_test.types.PingResponse;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;

public class LogicalMessageImplTest extends TestCase {

    private final ObjectMessageContextImpl ctx = new ObjectMessageContextImpl();
    private final LogicalMessageImpl lm = new LogicalMessageImpl(new LogicalMessageContextImpl(ctx)); 
    private JAXBContext jaxbCtx; 

    public void setUp() throws Exception { 

        ctx.setMethod(Greeter.class.getMethod("greetMe", String.class)); 
        jaxbCtx = JAXBContext.newInstance(GreetMe.class.getPackage().getName());
    } 


    public void testGetPayloadJAXBOutboundArguments() throws NoSuchMethodException, JAXBException { 
        
        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); 

        doGetArgumentsFromPayloadTest();
    }

    private void doGetArgumentsFromPayloadTest() { 
        Object[] args = {"test string parameter"};
        ctx.setMessageObjects(args);
        
        Object payload = lm.getPayload(jaxbCtx);
        assertNotNull(payload);
        assertTrue(payload instanceof GreetMe);
        assertEquals(args[0], ((GreetMe)payload).getRequestType());
    } 


    public void testGetPayloadJAXBOutboundNoArguments() throws NoSuchMethodException, JAXBException { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); 

        Object payload = lm.getPayload(jaxbCtx);
        assertNull(payload);
    } 


    public void testGetPayloadJAXBInboundReturn() { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false); 

        final String returnValue = "test return value";
        ctx.setReturn(returnValue);
        
        Object o = lm.getPayload(jaxbCtx); 
        assertNotNull(o); 
        assertTrue(o instanceof GreetMeResponse); 
        assertEquals(returnValue, ((GreetMeResponse)o).getResponseType());
    } 

    public void testSetPayloadJAXBOutboundArgs() { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); 

        final String testArg = "test argument set";
        GreetMe wrapper = new GreetMe(); 
        wrapper.setRequestType(testArg);
        lm.setPayload(wrapper, jaxbCtx); 
        
        GreetMe payload = (GreetMe)lm.getPayload(jaxbCtx);
        assertEquals(wrapper.getRequestType(), payload.getRequestType());
        
        Object[] args = ctx.getMessageObjects();

        assertNotNull(args);
        assertEquals(1, args.length);
        assertTrue(args[0] instanceof String);
        assertEquals(testArg, args[0]); 
    }


    public void testSetPayloadJAXInboundReturn() { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false); 

        final String testResponse = "test replaced response";
        GreetMeResponse wrapper = new GreetMeResponse(); 
        wrapper.setResponseType(testResponse); 
        lm.setPayload(wrapper, jaxbCtx); 
        
        GreetMeResponse payload = (GreetMeResponse)lm.getPayload(jaxbCtx);
        assertEquals(wrapper.getResponseType(), payload.getResponseType());

        Object ret = ctx.getReturn();
            
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals(testResponse, ret); 

    } 


    // server side tests 

    public void testSetPayloadJAXOutboundReturnListProperty() throws Exception { 
        
        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); 
        ctx.setMethod(HandlerTest.class.getMethod("ping")); 

        List<String> l = new ArrayList<String>(); 
        l.add("foo");
        l.add("bar");
        l.add("baz");
        ctx.setReturn(l);

        Object o = lm.getPayload(jaxbCtx);

        assertNotNull(o);
        assertEquals(PingResponse.class, o.getClass());
        assertEquals(l, ((PingResponse)o).getHandlersInfo());
    }
    
    public void testGetPayloadJAXBInbountArgs() {
        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);
    }
}
