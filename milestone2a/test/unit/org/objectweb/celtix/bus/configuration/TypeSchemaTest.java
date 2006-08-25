package org.objectweb.celtix.bus.configuration;

import java.net.URL;
import java.util.Collection;

import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.TypeSchema.TypeSchemaErrorHandler;

public class TypeSchemaTest extends TestCase {
    
    private final TypeSchemaHelper tsh = new TypeSchemaHelper();
    
    public void testConstructor() {

        // relative uri with relative path
        
        TypeSchema ts1 = tsh.get("http://celtix.objectweb.org/configuration/test/types",
                            "resources/test-types.xsd");
        assertNotNull(ts1);
        
        TypeSchema ts2 = tsh.get("http://celtix.objectweb.org/configuration/test/types",
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
        TypeSchema ts = tsh.get("http://celtix.objectweb.org/configuration/test/types",
                                       "resources/test-types.xsd");
        assertNotNull(ts);
        
        assertEquals("org.objectweb.celtix.configuration.test.types", ts.getPackageName());
        assertEquals(6, ts.getTypes().size()); 
        
        assertTrue(ts.hasType("bool"));
        assertEquals("bool", ts.getDeclaredType("bool"));
        assertEquals("boolean", ts.getXMLSchemaBaseType("bool"));
        
        assertTrue(ts.hasType("int"));
        assertEquals("int", ts.getDeclaredType("int"));
        assertEquals("integer", ts.getXMLSchemaBaseType("int"));
        
        assertTrue(ts.hasType("long"));
        assertEquals("longType", ts.getDeclaredType("long"));
        assertEquals("long", ts.getXMLSchemaBaseType("long"));
        
        assertTrue(ts.hasType("string"));
        assertEquals("string", ts.getDeclaredType("string"));
        assertEquals("string", ts.getXMLSchemaBaseType("string"));
        
        assertTrue(ts.hasType("boolList"));        
        assertEquals("boolList", ts.getDeclaredType("boolList"));
        assertNull(ts.getXMLSchemaBaseType("boolList"));
        
        assertTrue(ts.hasType("address"));
        assertTrue(!ts.hasType("addressType"));
        assertEquals("addressType", ts.getDeclaredType("address"));
        assertNull(ts.getXMLSchemaBaseType("address"));
        
        assertNotNull(ts.getSchema());
        assertNotNull(ts.getValidator());
    }
    
    public void testAnnotatedPackageName() {
        TypeSchema ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/custom/pkg",
                                       "resources/test-types-annotations.xsd");   
        assertEquals("org.objectweb.celtix.test.custom", ts.getPackageName());
    }
    
    public void testErrorHandler() {
        TypeSchema ts = tsh.get("http://celtix.objectweb.org/configuration/test/types",
            "resources/test-types.xsd");
        TypeSchemaErrorHandler eh = ts.new TypeSchemaErrorHandler();
        SAXParseException spe = new SAXParseException(null, null, null, 0, 0);
        
        try {
            eh.error(spe);
            fail("Expected SAXParseException not thrown.");
        } catch (SAXParseException ex) {
            // ignore;
        }
        
        try {
            eh.warning(spe);
            fail("Expected SAXParseException not thrown.");
        } catch (SAXParseException ex) {
             // ignore;
        }
        
        try {
            eh.fatalError(spe);
            fail("Expected SAXParseException not thrown.");
        } catch (SAXParseException ex) {
             // ignore;
        }          
    }
    
    public void testTypeSchemaHelper() {
        TypeSchema ts = org.easymock.classextension.EasyMock.createMock(TypeSchema.class);
        String namespaceURI = "http://celtix.objectweb.org/helper/test/types";
        assertNull(tsh.get(namespaceURI));
        tsh.put(namespaceURI, ts);
        assertNotNull(tsh.get(namespaceURI));
        assertNotNull(tsh.get(namespaceURI, "/helper/test/types.xsd"));
        Collection<TypeSchema> c = tsh.getTypeSchemas();
        assertTrue(c.size() > 0);
        assertTrue(c.contains(ts));               
    }
}
