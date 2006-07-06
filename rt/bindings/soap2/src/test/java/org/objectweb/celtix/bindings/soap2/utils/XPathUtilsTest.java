package org.objectweb.celtix.bindings.soap2.utils;

import java.io.*;
import org.w3c.dom.*;
import junit.framework.TestCase;
import org.objectweb.celtix.helpers.XMLUtils;

public class XPathUtilsTest extends TestCase {

    public void testIsExist() throws Exception {
        XMLUtils xmlUtils = new XMLUtils();
        Document doc = xmlUtils.parse(getTestStream("resources/amazon.xml"));

        XPathUtils xu = new XPathUtils();
        xu.addNamespace("a", "http://xml.amazon.com/AWSECommerceService/2004-08-01");

        assertTrue(xu.isExist(doc, "/a:ItemLookup"));
        assertTrue(xu.isExist(doc, "/a:ItemLookup/a:Request/a:IdType"));
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }
}
