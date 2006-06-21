package org.objectweb.celtix.bus.context;


import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.GenericMessageContext;

public class LogicalMessageContextTest extends AbstractMessageContextTestBase {

    GenericMessageContext wrapped = new GenericMessageContext(); 
    
    LogicalMessageContextImpl ctx = new LogicalMessageContextImpl(wrapped);
    
    @Override
    protected MessageContext getMessageContext() {
        return ctx;
    }

    public void testGetMessage() { 
        
        LogicalMessage msg = ctx.getMessage(); 
        assertNotNull(msg);
        assertTrue(LogicalMessageImpl.class.isInstance(msg));
    }
}
