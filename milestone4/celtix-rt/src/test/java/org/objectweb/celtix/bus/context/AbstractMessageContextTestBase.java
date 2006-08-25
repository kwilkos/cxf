package org.objectweb.celtix.bus.context;

import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

public abstract class AbstractMessageContextTestBase extends TestCase {

    private MessageContext ctx; 
    
    protected abstract MessageContext getMessageContext(); 
    
    public void setUp() {
        ctx = getMessageContext();
    }
    
    public void testGetDefaultScope() { 
        
        final String name = "foo.bar";        
        ctx.put(name, "prop-value");        
        assertTrue(ctx.containsKey(name));
        assertEquals(MessageContext.Scope.HANDLER, ctx.getScope(name));
    }

    public void testGetSetScope() { 

        final String name = "foo.bar";        
        ctx.put(name, "prop-value");        
        ctx.setScope(name, MessageContext.Scope.APPLICATION);
        assertEquals(MessageContext.Scope.APPLICATION, ctx.getScope(name));        
    }
    
    
    public void testGetScopeBadScope() {
        
        try {
            ctx.getScope("foo.bar.wibble.bad.property");
            fail("did not get expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
    }
    
    public void testSetScopeBadScope() { 
        
        try {
            ctx.setScope("foo.bar.wibble.bad.property", MessageContext.Scope.APPLICATION);
            fail("did not get expected IllegalArgumentException");                        
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }
}
