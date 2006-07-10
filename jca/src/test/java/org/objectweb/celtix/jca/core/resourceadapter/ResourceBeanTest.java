package org.objectweb.celtix.jca.core.resourceadapter;

import java.net.MalformedURLException;
import java.util.Properties;

import javax.resource.ResourceException;

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

    public void testValidatePropertiesThrowExceptionForBadCeltixCeUrl() throws Exception {
        try {
            Properties p = new Properties();
            ResourceBean resBean = new ResourceBean(p);
            resBean.setCeltixInstallDir("/vob/aspen");
            p.setProperty(ResourceBean.CELTIX_CE_URL, "bad:/bad_Celtix_ce.xml");
            resBean.validateProperties();
            fail("expect an exception due to bad CeltixCEURL configuration.");
        } catch (ResourceException re) {
            assertTrue("Cause is MalformedURLException, cause: " + re.getCause(),
                       re.getCause() instanceof MalformedURLException);
            assertTrue("Error message should contains bad", re.getMessage().indexOf("bad") != -1);
        }
    }

    public void testValidatePropertiesThrowExceptionForCeltixCeUrlNotSet() throws Exception {
        try {
            ResourceBean resBean = new ResourceBean();
            resBean.setCeltixInstallDir("/vob/aspen");
            resBean.validateProperties();
            fail("Exception expected due to Celtix CE URL property not set");
        } catch (ResourceException re) {
            assertNull("no cause for this exception : " + re.getCause(), re.getCause());
            assertTrue("Error message should contains CeltixCEURL",
                       re.getMessage().indexOf("CeltixCEURL") != -1);
        }
    }

}
