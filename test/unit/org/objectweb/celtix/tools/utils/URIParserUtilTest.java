package org.objectweb.celtix.tools.utils;

import junit.framework.TestCase;

public class URIParserUtilTest extends TestCase {

    public void testGetPackageName() {
        String packageName = URIParserUtil.getPackageName("http://www.celtix.iona.com");
        this.assertEquals(packageName, "com.iona.celtix");
        packageName = URIParserUtil.getPackageName("urn://www.class.iona.com");
        this.assertEquals(packageName, "com.iona._class");
    }
}
