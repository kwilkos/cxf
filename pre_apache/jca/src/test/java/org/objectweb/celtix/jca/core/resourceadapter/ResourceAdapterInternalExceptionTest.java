package org.objectweb.celtix.jca.core.resourceadapter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ResourceAdapterInternalExceptionTest extends TestCase {

    public ResourceAdapterInternalExceptionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ResourceAdapterInternalExceptionTest.class);
    }
    
    public void testMessage() {
        final String msg = "msg1";
        msg.intern();

        Exception e = new ResourceAdapterInternalException(msg);
        assertTrue(e.toString().indexOf(msg) != -1);
        assertTrue(e.toString().indexOf("reason") == -1);
        assertEquals(e.getMessage(), msg);
    }

    public void testMessageWithNullTx() {
        final String msg = "msg1";
        msg.intern();

        javax.resource.spi.ResourceAdapterInternalException e = new ResourceAdapterInternalException(msg,
                                                                                                     null);
        assertTrue(e.toString().indexOf(msg) != -1);
        assertTrue(e.toString().indexOf("reason") == -1);
        assertEquals(e.getMessage(), msg);

        assertNull(e.getCause());
        assertNull(e.getLinkedException());
    }

    public void testMessageWithEx() throws Exception {
        final String msg = "msg";
        final String causeMsg = "cause";

        Exception cause = new RuntimeException(causeMsg);
        javax.resource.spi.ResourceAdapterInternalException e = new ResourceAdapterInternalException(msg,
                                                                                                     cause);
        assertTrue(e.toString().indexOf(msg) != -1);
        assertTrue(e.toString().indexOf("reason") != -1);
        assertTrue(e.toString().indexOf(causeMsg) != -1);

        assertEquals(e.getCause(), cause);
    }

    public void testMessageWithThrowable() throws Exception {
        final String msg = "msg";
        final String causeMsg = "cause";

        Throwable cause = new Throwable(causeMsg);
        javax.resource.spi.ResourceAdapterInternalException e = new ResourceAdapterInternalException(msg,
                                                                                                     cause);
        assertTrue(e.toString().indexOf(msg) != -1);
        assertTrue(e.toString().indexOf("reason") != -1);
        assertTrue(e.toString().indexOf(causeMsg) != -1);

        assertEquals(e.getCause(), cause);
        assertNull(e.getLinkedException());

    }

    public void testMessageWithIteEx() throws Exception {
        final String msg = "msg";
        final String causeMsg = "cause";

        Exception cause = new RuntimeException(causeMsg);
        javax.resource.spi.ResourceAdapterInternalException re = 
            new ResourceAdapterInternalException(
                msg, 
                new java.lang.reflect.InvocationTargetException(cause));
        
        assertTrue(re.toString().indexOf(msg) != -1);
        assertTrue(re.toString().indexOf("reason") != -1);
        assertTrue(re.toString().indexOf(causeMsg) != -1);

        assertEquals(re.getCause(), cause);
    }

    public void testMessageWithIteErroriNotThrow() throws Exception {
        final String msg = "msg";
        final String causeMsg = "cause";

        java.lang.Throwable cause = new java.lang.UnknownError(causeMsg);
        ResourceAdapterInternalException re = 
            new ResourceAdapterInternalException(
                msg,
                new java.lang.reflect.InvocationTargetException(cause));
        assertEquals(re.getCause(), cause);
    }

    public void testGetLinkedExceptionReturnNullIfNoCause() throws Exception {
        ResourceAdapterInternalException re = new ResourceAdapterInternalException("ex");
        assertNull("getLinkedException return null", re.getLinkedException());
    }

    public void testGetLinkedExceptionReturnNullIfCauseIsError() throws Exception {
        java.lang.Throwable cause = new java.lang.UnknownError("error");
        ResourceAdapterInternalException re = new ResourceAdapterInternalException("ex", cause);
        assertNull("getLinkedException return null", re.getLinkedException());
    }

    public void testGetLinkedExceptionReturnNotNullIfCauseIsException() throws Exception {
        java.lang.Throwable cause = new RuntimeException("runtime exception");
        ResourceAdapterInternalException re = new ResourceAdapterInternalException("ex", cause);
        assertEquals("get same exception", cause, re.getLinkedException());
    }
}
