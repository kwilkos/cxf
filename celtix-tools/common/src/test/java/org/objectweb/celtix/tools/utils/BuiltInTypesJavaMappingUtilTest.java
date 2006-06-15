package org.objectweb.celtix.tools.utils;

import junit.framework.TestCase;

public class BuiltInTypesJavaMappingUtilTest extends TestCase {
    private final String xmlSchemaNS = "http://www.w3.org/2000/10/XMLSchema";
    public void testGetJType() {
        String jType = BuiltInTypesJavaMappingUtil.getJType(xmlSchemaNS, "string");
        assertEquals(jType, "java.lang.String");
    }
}
