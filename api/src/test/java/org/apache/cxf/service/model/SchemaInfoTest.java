package org.apache.cxf.service.model;

import junit.framework.TestCase;

public class SchemaInfoTest extends TestCase {
    
    private SchemaInfo schemaInfo;
    
    public void setUp() throws Exception {
        schemaInfo = new SchemaInfo(null, "http://apache.org/hello_world_soap_http/types");
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testConstructor() throws Exception {
        assertNull(schemaInfo.getTypeInfo());
        assertNull(schemaInfo.getElement());
        assertEquals(schemaInfo.getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http/types");
    }
    
    public void testNamespaceURI() throws Exception {
        schemaInfo.setNamespaceURI("http://apache.org/hello_world_soap_http/types1");
        assertEquals(schemaInfo.getNamespaceURI(), "http://apache.org/hello_world_soap_http/types1");
    }
    
}
