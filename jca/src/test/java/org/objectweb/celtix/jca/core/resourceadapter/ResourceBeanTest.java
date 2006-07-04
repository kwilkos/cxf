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

    public void testValidatePropertiesThrowExceptionForBadArtixCeUrl() throws Exception {
        try {
            Properties p = new Properties();
            ResourceBean resBean = new ResourceBean(p);
            resBean.setCeltixInstallDir("/vob/aspen");
            p.setProperty(ResourceBean.CELTIX_CE_URL, "bad:/bad_artix_ce.xml");
            resBean.validateProperties();
            fail("expect an exception due to bad ArtixCEURL configuration.");
        } catch (ResourceException re) {
            assertTrue("Cause is MalformedURLException, cause: " + re.getCause(),
                       re.getCause() instanceof MalformedURLException);
            assertTrue("Error message should contains bad", re.getMessage().indexOf("bad") != -1);
        }
    }

    public void testValidatePropertiesThrowExceptionForArtixCeUrlNotSet() throws Exception {
        try {
            ResourceBean resBean = new ResourceBean();
            resBean.setCeltixInstallDir("/vob/aspen");
            resBean.validateProperties();
            fail("Exception expected due to Artix CE URL property not set");
        } catch (ResourceException re) {
            assertNull("no cause for this exception : " + re.getCause(), re.getCause());
            assertTrue("Error message should contains ArtixCEURL",
                       re.getMessage().indexOf("ArtixCEURL") != -1);
        }
    }

}
