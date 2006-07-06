package org.objectweb.celtix.jca.celtix.handlers;


import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnection;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnectionFactory;

public abstract class AbstractInvocationHandlerTest 
    extends HandlerTestBase {
    
    public AbstractInvocationHandlerTest(String name) {
        super(name);
    }

    // seach for the setNext method
    public void testHandlerInvokesNext() throws Throwable {
        Object[] args = new Object[0];
                
        CeltixInvocationHandler handler = getHandler();
        handler.setNext(mockHandler); 
        
        handler.invoke(target, testMethod, args);        
             
        assertTrue("target object must not be called", !target.methodInvoked);
    }

    public void testTargetAttribute() {

        CeltixInvocationHandler handler = getHandler();
        handler.getData().setTarget(target);
        assertSame("target must be retrievable after set",
                   target, handler.getData().getTarget());
    }

    public void testBusAttribute() {

        CeltixInvocationHandler handler = getHandler();
        handler.getData().setBus(mockBus);
        assertSame("bus must be retrievable after set", mockBus, handler.getData().getBus());
    }

    public void testManagedConnectionAttribute() {

        CeltixInvocationHandler handler = getHandler();

        handler.getData().setManagedConnection((CeltixManagedConnection)mockManagedConnection);
        assertSame("bus must be retrievable after set", mockManagedConnection, handler.getData()
            .getManagedConnection());
    }

    protected CeltixInvocationHandler getNextHandler() {
        return (CeltixInvocationHandler)mockHandler;
    }

    protected abstract CeltixInvocationHandler getHandler();

    protected CeltixManagedConnectionFactory getTestManagedConnectionFactory() {
        return (CeltixManagedConnectionFactory)mcf;
    }

    
}
