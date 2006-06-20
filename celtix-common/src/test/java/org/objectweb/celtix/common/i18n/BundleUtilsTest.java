package org.objectweb.celtix.common.i18n;

import java.util.ResourceBundle;

import junit.framework.TestCase;


public class BundleUtilsTest extends TestCase {
    public void testGetBundleName() throws Exception {
        assertEquals("unexpected resource bundle name",
                     "org.objectweb.celtix.common.i18n.Messages",
                     BundleUtils.getBundleName(getClass()));
        assertEquals("unexpected resource bundle name",
                     "org.objectweb.celtix.common.i18n.Messages",
                     BundleUtils.getBundleName(getClass(), "Messages"));
    }

    public void testGetBundle() throws Exception {
        ResourceBundle bundle = BundleUtils.getBundle(getClass());
        assertNotNull("expected resource bundle", bundle);
        assertEquals("unexpected resource", 
                     "localized message",
                     bundle.getString("I18N_MSG"));
        ResourceBundle nonDefaultBundle = BundleUtils.getBundle(getClass(), "Messages");
        assertNotNull("expected resource bundle", nonDefaultBundle);
        assertEquals("unexpected resource", 
                     "localized message",
                     nonDefaultBundle.getString("I18N_MSG"));             
    }
}
