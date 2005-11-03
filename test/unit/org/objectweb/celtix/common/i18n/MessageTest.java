package org.objectweb.celtix.common.i18n;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.objectweb.celtix.common.logging.LogUtils;


public class MessageTest extends TestCase {
    private static final Logger LOG = LogUtils.getL7dLogger(MessageTest.class);
    
    public void testMessageWithLoggerBundle() throws Exception {
        Message msg = new Message("SUB1_EXC", LOG, new Object[] {1});
        assertSame("unexpected resource bundle",
                   LOG.getResourceBundle(),
                   msg.bundle);
        assertEquals("unexpected message string", 
                     "subbed in 1 only", 
                     msg.toString()); 
    }

    public void testMessageWithExplicitBundle() throws Exception {
        ResourceBundle bundle = BundleUtils.getBundle(getClass());
        Message msg = new Message("SUB2_EXC", bundle, new Object[] {3, 4});
        assertSame("unexpected resource bundle", bundle, msg.bundle);
        assertEquals("unexpected message string", 
                     "subbed in 4 & 3",
                     msg.toString()); 
    }
}
