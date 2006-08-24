package org.apache.cxf.tools.util;

import junit.framework.TestCase;

public class URIParserUtilTest extends TestCase {

    public void testGetPackageName() {
        String packageName = URIParserUtil.getPackageName("http://www.cxf.iona.com");
        assertEquals(packageName, "com.iona.cxf");
        packageName = URIParserUtil.getPackageName("urn://www.class.iona.com");
        assertEquals(packageName, "com.iona._class");
    }
}
