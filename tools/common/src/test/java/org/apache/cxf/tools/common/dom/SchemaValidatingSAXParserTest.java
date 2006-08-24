package org.apache.cxf.tools.common.dom;

import junit.framework.TestCase;

public class SchemaValidatingSAXParserTest extends TestCase {
    public void testMassMethod() {
        SchemaValidatingSAXParser parser = new SchemaValidatingSAXParser();
        parser.setValidating(true);
        assertTrue(parser.getSAXParser() != null);
    }
}
