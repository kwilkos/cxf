package org.objectweb.celtix.application;

import junit.framework.TestCase;

import org.objectweb.celtix.plugins.PluginException;
import org.objectweb.celtix.plugins.PluginManager;

public class ApplicationPluginManagerTest extends TestCase {

    public void testGetPluginClassNotFound() {
        ApplicationPluginManager apm = new ApplicationPluginManager();
        try {
            apm.getPlugin("org.objectweb.celtix.application.test.Greater");
            fail("Expected PluginException not thrown");
        } catch (PluginException ex) {
            assertEquals("LOAD_FAILED", ex.getCode());
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
    }

    public void testGetPluginIllegalAccess() {
        ApplicationPluginManager apm = new ApplicationPluginManager();
        try {
            apm.getPlugin("org.objectweb.celtix.application.test.ProtectedConstructorGreeter");
            fail("Expected PluginException not thrown");
        } catch (PluginException ex) {
            assertEquals("LOAD_FAILED", ex.getCode());
            assertTrue("Unexepcted cause: " + ex.getCause().getClass().getName(),
                       ex.getCause() instanceof IllegalAccessException);
        }
    }

    public void testGetPluginNoDefaultConstructor() {
        ApplicationPluginManager apm = new ApplicationPluginManager();
        try {
            apm.getPlugin("org.objectweb.celtix.application.test.PersonalGreeter");
            fail("Expected PluginException not thrown");
        } catch (PluginException ex) {
            assertEquals("LOAD_FAILED", ex.getCode());
            assertTrue(ex.getCause() instanceof InstantiationException);
        }
    }

    public void testGetPlugin() throws PluginException {
        ApplicationPluginManager apm = new ApplicationPluginManager();
        Object object = apm.getPlugin("org.objectweb.celtix.application.test.Greeter");
        Object otherObject = apm.createPlugin("org.objectweb.celtix.application.test.Greeter");
        assertTrue(object != otherObject);
        otherObject = apm.getPlugin("org.objectweb.celtix.application.test.Greeter");
        assertTrue(object == otherObject);
    }

    public void testConcurrentLoad() throws PluginException {
        String className = "org.objectweb.celtix.application.test.GreeterWithConstructorDelay";

        GetPluginThread t1 = new GetPluginThread(className);
        GetPluginThread t2 = new GetPluginThread(className);
        t1.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore
        }
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ex) {
            // ignore
        }

        assertTrue(t1.getPlugin() == t2.getPlugin());
    }

    class GetPluginThread extends Thread {

        String className;
        Object plugin;

        GetPluginThread(String cn) {
            className = cn;
        }

        public Object getPlugin() {
            return plugin;
        }

        public void run() {
            PluginManager pm = Application.getInstance().getPluginManager();
            try {
                plugin = pm.getPlugin(className);
            } catch (PluginException ex) {
                // ignore
            }
        }
    }

}
