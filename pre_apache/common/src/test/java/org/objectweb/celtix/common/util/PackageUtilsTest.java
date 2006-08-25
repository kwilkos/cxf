package org.objectweb.celtix.common.util;

import junit.framework.TestCase;

public class PackageUtilsTest extends TestCase {
    public void testgetPackageName() throws Exception {
        String packageName = PackageUtils.getPackageName(this.getClass());       
        assertEquals("Should get same packageName", this.getClass().getPackage().getName(), packageName);
    }
    
    public void testGetPackageName() throws Exception {
        String className = "HelloWorld";
        assertEquals("Should return empty string", "", PackageUtils.getPackageName(className));
    }
}
