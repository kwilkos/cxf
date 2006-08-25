package org.objectweb.celtix.tools.common.dom;

import junit.framework.TestCase;

public class SchemaValidatingSAXParserTest extends TestCase {
    public void testMassMethod() {
        SchemaValidatingSAXParser parser = new SchemaValidatingSAXParser();
        parser.setValidating(true);
        parser.mapNamespaceToSchemaResource("testpublicId", "testSystemID",
                                            "/org/objectweb/celtix/tools/common/toolspec/parse"
                                                + "r/resources/testtool.xml");
        assertTrue(parser.getSAXParser() != null);
    }
}
