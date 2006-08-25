package org.objectweb.celtix.tools.common.dom;

import junit.framework.TestCase;

public class ExtendedDocumentBuilderTest extends TestCase {
    public void testMassMethod() throws Exception {
        ExtendedDocumentBuilder builder = new ExtendedDocumentBuilder();
        builder.setValidating(false);
        assertTrue(builder.getJavaResourceEntityResolver() != null);
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        assertTrue(builder.parse(getClass().getResourceAsStream(tsSource)) != null);

    }
}
