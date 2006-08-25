package org.objectweb.celtix.tools.common.dom;

import junit.framework.TestCase;

public class SchemaValidatingSAXParserTest extends TestCase {
    public void testMassMethod() {
        SchemaValidatingSAXParser parser = new SchemaValidatingSAXParser();
        parser.setValidating(true);
        assertTrue(parser.getSAXParser() != null);
    }
}
