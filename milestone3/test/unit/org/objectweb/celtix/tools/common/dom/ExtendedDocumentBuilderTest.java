package org.objectweb.celtix.tools.common.dom;

import org.w3c.dom.Document;

import junit.framework.TestCase;

public class ExtendedDocumentBuilderTest extends TestCase {
    public void testMassMethod() throws Exception {
        ExtendedDocumentBuilder builder = new ExtendedDocumentBuilder();
        builder.setValidating(false);
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        assertTrue(builder.parse(getClass().getResourceAsStream(tsSource)) != null);
    }

    public void testParse() throws Exception {
        ExtendedDocumentBuilder builder = new ExtendedDocumentBuilder();
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool1.xml";
        Document doc = builder.parse(getClass().getResourceAsStream(tsSource));
        assertEquals(doc.getXmlVersion(), "1.0");
    }
}
