package org.objectweb.celtix.bus.configuration;

import java.net.URL;

import junit.framework.TestCase;

public class TypeSchemaTest extends TestCase {
    
    TypeSchemaInfo tsi1 = new TypeSchemaInfo("http://celtix.objectweb.org/configuration/test/types",
        "resources/test-types.xsd");
    TypeSchemaInfo tsi2 = new TypeSchemaInfo("http://celtix.objectweb.org/configuration/test/types",
        "/org/objectweb/celtix/bus/configuration/resources/test-types.xsd");
    TypeSchemaInfo tsi3 = new TypeSchemaInfo("http://celtix.objectweb.org/configuration/test/types",
        "file:resources/test-types.xsd");
    TypeSchemaInfo tsi4 = new TypeSchemaInfo("http://celtix.objectweb.org/configuration/types",
        "/org/objectweb/celtix/configuration/types.xsd"); 
    
    
    public void testHashCode() {
        assertTrue(tsi1.hashCode() == tsi2.hashCode());
        assertTrue(tsi1.hashCode() != tsi4.hashCode());     
    }
    
    public void testEquals() {
        assertTrue(tsi1.equals(tsi1));
        assertTrue(!tsi1.equals(tsi2));
        assertTrue(!tsi2.equals(tsi1));
        assertTrue(!tsi1.equals(this));
        assertTrue(!this.equals(tsi1));
    }
    
    public void testAttributes() {
        assertEquals("http://celtix.objectweb.org/configuration/test/types", tsi1.getNamespaceURI());
        assertEquals("resources/test-types.xsd", tsi1.getLocation());
    }

    public void testConstructor() {

        // relative uri with relative path

        TypeSchema ts1 = TypeSchema.get("http://celtix.objectweb.org/configuration/test/types",
                            "resources/test-types.xsd");
        assertNotNull(ts1);
        
        TypeSchema ts2 = TypeSchema.get("http://celtix.objectweb.org/configuration/test/types",
                                        "resources/test-types.xsd");
        assertNotNull(ts2);
        assertTrue(ts1 == ts2);
        
        // relative uri with absolute path
        
        TypeSchema ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/types",
                            "/org/objectweb/celtix/bus/configuration/resources/test-types.xsd");
        assertNotNull(ts);

        // absolute uri with relative path
        
        try {
            ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/types", 
                            "file:resources/test-types.xsd");
        } catch (org.objectweb.celtix.configuration.ConfigurationException ex) {
            assertEquals("FILE_OPEN_ERROR_EXC", ex.getCode());
        }
        
        URL url = TypeSchemaTest.class.getResource("resources/test-types.xsd");
        
        // absolute uri with absolute path
        
        ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/types", 
                            "file:" + url.getFile());
        assertNotNull(ts); 
    }
    
    public void testContent() {
        TypeSchema ts = TypeSchema.get("http://celtix.objectweb.org/configuration/test/types",
                                       "resources/test-types.xsd");
        assertNotNull(ts);
        
        assertEquals("org.objectweb.celtix.configuration.test.types", ts.getPackageName());
        assertEquals(5, ts.getTypes().size());      
        assertTrue(ts.hasType("boolList"));
        assertTrue(!ts.hasType("boolListType"));
        assertEquals("boolList", ts.getTypeType("boolList"));
        assertEquals("addressType", ts.getTypeType("address"));
        assertNotNull(ts.getSchema());
        assertNotNull(ts.getValidator());
    }
}
