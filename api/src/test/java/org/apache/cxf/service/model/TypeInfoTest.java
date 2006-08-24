package org.apache.cxf.service.model;

import junit.framework.TestCase;

public class TypeInfoTest extends TestCase {
    
    private TypeInfo typeInfo;
    
    public void setUp() throws Exception {
        typeInfo = new TypeInfo(null);
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testConstructor() throws Exception {
        assertNull(typeInfo.getService());
    }
 
    public void testSchema() throws Exception {
        assertEquals(typeInfo.getSchemas().size(), 0);
        typeInfo.addSchema("http://schema1");
        assertEquals(typeInfo.getSchemas().size(), 1);
        SchemaInfo schemaInfo = typeInfo.getSchema("dummySchema");
        assertNull(schemaInfo);
        schemaInfo = typeInfo.getSchema("http://schema1");
        assertNotNull(schemaInfo);
        assertEquals(schemaInfo.getNamespaceURI(), "http://schema1");
        assertTrue(schemaInfo.getTypeInfo() == typeInfo);
        schemaInfo = new SchemaInfo(typeInfo, "http://schema2");
        typeInfo.addSchema(schemaInfo);
        assertEquals(typeInfo.getSchemas().size(), 2);
        assertEquals(typeInfo.getSchema("http://schema2").getNamespaceURI(), "http://schema2");
    }
    
    public void testSchemaTnsNull() throws Exception {
        boolean isNull = false;
        try {
            String tns = null;
            typeInfo.addSchema(tns);
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "Namespace URI cannot be null.");
            isNull = true;
        }
        if (!isNull) {
            fail("should get NullPointerException");
        }
        
        boolean duplicatedTns = false;
        try {
            String tns = "http://schema";
            typeInfo.addSchema(tns);
            typeInfo.addSchema(tns);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), 
                "An schema with namespaceURI [http://schema] already exists in this service");
            duplicatedTns = true;
        }
        
        if (!duplicatedTns) {
            fail("should get IllegalArgumentException");
        }
    }
}
