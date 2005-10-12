package org.objectweb.celtix.common.bundle;

import junit.framework.TestCase;


public class BundleUtilsTest extends TestCase {
    public void testGetBundleName() throws Exception {
        assertEquals("unexpected resource bundle name",
                     "org.objectweb.celtix.common.bundle.Messages",
                     BundleUtils.getBundleName(getClass()));
    }
}
