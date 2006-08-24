package org.apache.cxf.test;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.cxf.helpers.DOMUtils;

public class XPathAssertTest extends TestCase {
    public void testAssert() throws Exception {
        Document document = DOMUtils.readXml(getClass().getResourceAsStream("test.xml"));

        XPathAssert.assertValid("//a", document, null);
        XPathAssert.assertInvalid("//aasd", document, null);

        try {
            XPathAssert.assertInvalid("//a", document, null);
            fail("Expression is valid!");
        } catch (AssertionFailedError e) {
            // this is correct
        }

        try {
            XPathAssert.assertValid("//aa", document, null);
            fail("Expression is invalid!");
        } catch (AssertionFailedError e) {
            // this is correct
        }

        XPathAssert.assertXPathEquals("//b", "foo", document, null);
    }

    public void testAssertNamespace() throws Exception {
        Document document = DOMUtils.readXml(getClass().getResourceAsStream("test2.xml"));

        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("a", "urn:foo");
        namespaces.put("z", "urn:z");

        XPathAssert.assertValid("//a:a", document, namespaces);
        XPathAssert.assertValid("//z:b", document, namespaces);
    }
}
