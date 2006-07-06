package org.objectweb.celtix.jca.celtix.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;

public class InvocationHandlerFactoryTest extends HandlerTestBase {
    
    public InvocationHandlerFactoryTest(String name) {
        super(name);
    }
    
    public void testCreateHandlerChain() 
        throws ResourceAdapterInternalException {

        Subject testSubject = new Subject();

        InvocationHandlerFactory factory = 
            new InvocationHandlerFactory(
                 mockBus,
                 mci);

        CeltixInvocationHandler handler = factory.createHandlers(target, testSubject);
        CeltixInvocationHandler first = handler;
        CeltixInvocationHandler last = null;

        assertNotNull("handler must not be null", handler);
        int count = 0;
        Set<Class> allHandlerTypes = new HashSet<Class>();

        while (handler != null) {

            assertSame("managed connection must be set", mci, handler.getData().getManagedConnection());
            assertSame("bus must be set", mockBus, handler.getData().getBus());
            assertSame("subject must be set", testSubject, handler.getData().getSubject());
            assertSame("target must be set", target, handler.getData().getTarget());
            allHandlerTypes.add(handler.getClass());

            last = handler;
            handler = handler.getNext();

            count++;
        }
        assertNotNull(last);

        assertEquals("must create correct number of handlers", 3, count);

        assertTrue("first handler must a ProxyInvocationHandler", first instanceof ProxyInvocationHandler);
        assertTrue("last handler must be an InvokingInvocationHandler",
                   last instanceof InvokingInvocationHandler);

        Class[] types = {ProxyInvocationHandler.class, 
                         ObjectMethodInvocationHandler.class,
                         InvokingInvocationHandler.class};

        for (int i = 0; i < types.length; i++) {
            assertTrue("handler chain must contain type: " + types[i], allHandlerTypes.contains(types[i]));
        }
    }

    

    public static Test suite() {
        return new TestSuite(InvocationHandlerFactoryTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {InvocationHandlerFactoryTest.class.getName()});
    }
}
