package org.objectweb.celtix.configuration.impl;

import java.net.URL;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.AbstractConfigurationImplTest;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.impl.TypeSchema.TypeSchemaErrorHandler;

public class TypeSchemaTest extends TestCase {
    
    private final TypeSchemaHelper tsh = new TypeSchemaHelper(true);
    
    public void setUp() {
        TypeSchemaHelper.clearCache();
    }
    
    public void testConstructor() {

        TypeSchema ts = null;
        
        // relative uri with relative path
        ts = tsh.get("http://celtix.objectweb.org/configuration/test/types",
                     null,
                     "org/objectweb/celtix/bus/configuration/resources/test-types.xsd");
        assertNotNull(ts);
        
        TypeSchema ts2 = tsh.get("http://celtix.objectweb.org/configuration/test/types",
                                 null,
                                 "org/objectweb/celtix/bus/configuration/resources/test-types.xsd");
        assertNotNull(ts2);
        assertTrue(ts == ts2);
        
        // relative uri with absolute path
        
        try {
            new TypeSchema("http://celtix.objectweb.org/configuration/test/types",
                           null,
                           "/org/objectweb/celtix/bus/configuration/resources/test-types.xsd",
                           true);
        } catch (ConfigurationException ex) {
            assertEquals("SCHEMA_LOCATION_ERROR_EXC", ex.getCode());
        }

        // file uri with relative path
        
        try {
            ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/types",
                                null,
                                "file://resources/test-types.xsd",
                                true);
        } catch (org.objectweb.celtix.configuration.ConfigurationException ex) {
            assertEquals("SCHEMA_LOCATION_ERROR_EXC", ex.getCode());
        }
        
        URL url = AbstractConfigurationImplTest.class.getResource("resources/test-types.xsd");
        
        // absolute uri with absolute path
        
        ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/types",
                            null,
                            "file://" + url.getFile(),
                            true);
        assertNotNull(ts); 
    }

    public void testTypesOnly() {
        TypeSchema ts = tsh.get("http://celtix.objectweb.org/configuration/test/types-types",
                                null,
                                "org/objectweb/celtix/bus/configuration/resources/test-types-types.xsd");
        
        assertEquals(7, ts.getTypes().size()); 
        assertEquals(0, ts.getElements().size());
        
        assertTrue(ts.hasType("bool"));
        assertEquals("boolean", ts.getXMLSchemaBaseType("bool"));
        
        assertTrue(ts.hasType("int"));
        assertEquals("integer", ts.getXMLSchemaBaseType("int"));
        
        assertTrue(ts.hasType("longType"));
        assertEquals("long", ts.getXMLSchemaBaseType("longType"));
        
        assertTrue(ts.hasType("longBaseType"));
        assertEquals("long", ts.getXMLSchemaBaseType("longBaseType"));
        
        assertTrue(ts.hasType("string"));
        assertEquals("string", ts.getXMLSchemaBaseType("string"));
        
        assertTrue(ts.hasType("boolList"));        
        assertNull(ts.getXMLSchemaBaseType("boolList"));
        
        assertTrue(ts.hasType("addressType"));
        assertNull(ts.getXMLSchemaBaseType("addressType"));  
        
        assertNotNull(ts.getSchema());
        assertNotNull(ts.getValidator());
    }

    public void testElementsOnly() {
        TypeSchema ts = tsh.get("http://celtix.objectweb.org/configuration/test/types-elements",
                                null,
                                "org/objectweb/celtix/bus/configuration/resources/test-types-elements.xsd");
        
        assertEquals(0, ts.getTypes().size());
        assertEquals(7, ts.getElements().size()); 
        
        String namespace = "http://celtix.objectweb.org/configuration/test/types-types";
        assertTrue(ts.hasElement("bool"));
        assertEquals(new QName(namespace, "bool"), ts.getDeclaredType("bool"));
        assertTrue(!ts.hasType("bool"));
        try {
            ts.getXMLSchemaBaseType("bool");
        } catch (ConfigurationException ex) {
            assertEquals("TYPE_NOT_DEFINED_IN_NAMESPACE_EXC", ex.getCode());
        }
        
        assertTrue(ts.hasElement("int"));
        assertEquals(new QName(namespace, "int"), ts.getDeclaredType("int"));
        
        assertTrue(ts.hasElement("long"));
        assertEquals(new QName(namespace, "longType"), ts.getDeclaredType("long"));
        
        assertTrue(ts.hasElement("string"));
        assertEquals(new QName(namespace, "string"), ts.getDeclaredType("string"));
        
        assertTrue(ts.hasElement("boolList"));        
        assertEquals(new QName(namespace, "boolList"), ts.getDeclaredType("boolList"));
        
        assertTrue(ts.hasElement("address"));
        assertEquals(new QName(namespace, "addressType"), ts.getDeclaredType("address"));
        
        assertTrue(ts.hasElement("floatValue"));
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "float"), 
                     ts.getDeclaredType("floatValue"));        
        
        assertNotNull(ts.getSchema());
        assertNotNull(ts.getValidator());
        
    }
    
    public void testElementsAndTypes() {
        String namespace = "http://celtix.objectweb.org/configuration/test/types";
        TypeSchema ts = tsh.get("http://celtix.objectweb.org/configuration/test/types",
                                null,
                                "org/objectweb/celtix/bus/configuration/resources/test-types.xsd");
        assertNotNull(ts);
        
        assertEquals("org.objectweb.celtix.configuration.test.types", ts.getPackageName());
        assertEquals(7, ts.getTypes().size());
        assertEquals(7, ts.getElements().size()); 
        
        assertTrue(ts.hasElement("bool"));        
        assertEquals(new QName(namespace, "bool"), ts.getDeclaredType("bool"));
        assertTrue(ts.hasType("bool"));
        assertEquals("boolean", ts.getXMLSchemaBaseType("bool"));
        
        assertTrue(ts.hasElement("int"));
        assertEquals(new QName(namespace, "int"), ts.getDeclaredType("int"));
        assertTrue(ts.hasType("int"));
        assertEquals("integer", ts.getXMLSchemaBaseType("int"));
        
        assertTrue(ts.hasElement("long"));
        assertEquals(new QName(namespace, "longType"), ts.getDeclaredType("long"));
        assertTrue(ts.hasType("longType"));
        assertEquals("long", ts.getXMLSchemaBaseType("longType"));
        assertTrue(ts.hasType("longBaseType"));
        assertEquals("long", ts.getXMLSchemaBaseType("longBaseType"));
        
        assertTrue(ts.hasElement("string"));
        assertEquals(new QName(namespace, "string"), ts.getDeclaredType("string"));
        assertTrue(ts.hasType("string"));
        assertEquals("string", ts.getXMLSchemaBaseType("string"));
        
        assertTrue(ts.hasElement("boolList"));        
        assertEquals(new QName(namespace, "boolList"), ts.getDeclaredType("boolList"));
        assertTrue(ts.hasElement("boolList"));
        assertNull(ts.getXMLSchemaBaseType("boolList"));
        
        assertTrue(ts.hasElement("address"));
        assertEquals(new QName(namespace, "addressType"), ts.getDeclaredType("address"));
        assertTrue(ts.hasType("addressType"));
        assertNull(ts.getXMLSchemaBaseType("addressType"));
        
        assertTrue(ts.hasElement("floatValue"));
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "float"), 
                     ts.getDeclaredType("floatValue"));
        assertTrue(!ts.hasType("float"));
        
        assertNotNull(ts.getSchema());
        assertNotNull(ts.getValidator());
    }
    
    public void testAnnotatedPackageName() {
        TypeSchema ts = new TypeSchema("http://celtix.objectweb.org/configuration/test/custom/pkg",
            null, "org/objectweb/celtix/bus/configuration/resources/test-types-annotations.xsd",
            true);   
        assertEquals("org.objectweb.celtix.test.custom", ts.getPackageName());
    }
    
    public void testErrorHandler() {
        TypeSchemaErrorHandler eh = new TypeSchema.TypeSchemaErrorHandler();
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
        assertNotNull(tsh.get(namespaceURI, null, "/helper/test/types.xsd"));
        Collection<TypeSchema> c = tsh.getTypeSchemas();
        assertTrue(c.size() > 0);
        assertTrue(c.contains(ts));               
    }
}
