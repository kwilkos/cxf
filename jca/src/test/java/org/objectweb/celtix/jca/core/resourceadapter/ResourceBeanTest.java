package org.objectweb.celtix.jca.core.resourceadapter;


import java.util.Properties;
import junit.framework.TestCase;

public class ResourceBeanTest extends TestCase {

    public ResourceBeanTest(String name) {
        super(name);
    }

    public void testSetCeltixInstallDir() throws Exception {
        final String dudDir = "A_DUD_INSTALL_DIR";
        Properties p = new Properties();
        ResourceBean rb = new ResourceBean(p);

        rb.setCeltixInstallDir(dudDir);
        assertTrue(p.containsValue(dudDir));
        assertEquals(dudDir, rb.getCeltixInstallDir());
    }

   

    public void testDisableConsoleLogging() throws Exception {
        ResourceBean rb = new ResourceBean();
        rb.setDisableConsoleLogging(true);
    }
    

}
