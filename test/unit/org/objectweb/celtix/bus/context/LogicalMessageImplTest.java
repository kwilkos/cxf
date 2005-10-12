package org.objectweb.celtix.bus.context;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.ObjectMessageContextImpl;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;

public class LogicalMessageImplTest extends TestCase {

    private ObjectMessageContextImpl ctx = new ObjectMessageContextImpl();
    private LogicalMessageImpl lm = new LogicalMessageImpl(new LogicalMessageContextImpl(ctx)); 
    private JAXBContext jaxbCtx; 

    public void setUp() throws Exception { 

        ctx.setMethod(Greeter.class.getMethod("greetMe", String.class)); 
        jaxbCtx = JAXBContext.newInstance(GreetMe.class.getPackage().getName());
    } 


    public void testGetPayloadJAXBOutboundArguments() throws NoSuchMethodException, JAXBException { 
        
        Object[] args = {"test string parameter"};
        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);
        ctx.setMessageObjects(args);
        
        Object payload = lm.getPayload(jaxbCtx);
        assertNotNull(payload);
        assertTrue(payload instanceof GreetMe);
        assertEquals(args[0], ((GreetMe)payload).getRequestType());
    }


    public void testGetPayloadJAXBOutboundNoArguments() throws NoSuchMethodException, JAXBException { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);

        Object payload = lm.getPayload(jaxbCtx);
        assertNull(payload);
    } 


    public void testGetPayloadJAXBInboundReturn() { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);

        final String returnValue = "test return value";
        ctx.setReturn(returnValue);
        
        Object o = lm.getPayload(jaxbCtx); 
        assertNotNull(o); 
        assertTrue(o instanceof GreetMeResponse); 
        assertEquals(returnValue, ((GreetMeResponse)o).getResponseType());
    } 

    public void testSetPayloadJAXBOutboundArgs() { 

        ctx.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);

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
}
