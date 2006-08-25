package org.objectweb.celtix.bus.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata.LifecyclePolicy;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.types.StringListType;


public class ConfigurationMetadataImplTest extends TestCase {

    private static final String TYPES_NAMESPACE_URI = 
        "http://celtix.objectweb.org/configuration/types";
    

    public void testStandardTypes() {
             
        ConfigurationMetadata model = buildMetadata("meta1.xml"); 
        assertEquals("http://celtix.objectweb.org/configuration/test/meta1", 
                     model.getNamespaceURI());
        Collection<ConfigurationItemMetadata> definitions = model.getDefinitions();
        assertEquals(9, definitions.size());
        ConfigurationItemMetadata definition = null;
        
        definition = model.getDefinition("booleanItem");
        assertNotNull(definition);
        assertEquals("booleanItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean"),
                     definition.getType());
        assertEquals(LifecyclePolicy.STATIC, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("shortItem");
        assertNotNull(definition);
        assertEquals("shortItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "short"),
                     definition.getType());
        
        definition = model.getDefinition("intItem");
        assertNotNull(definition);
        assertEquals("intItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"),
                     definition.getType());
        
        definition = model.getDefinition("integerItem");
        assertNotNull(definition);
        assertEquals("integerItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "integer"),
                     definition.getType());
        // assertNull(definition.getDescription());
        assertEquals(LifecyclePolicy.PROCESS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("longItem");
        assertNotNull(definition);
        assertEquals("longItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "long"),
                     definition.getType());
        // assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.BUS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("floatItem");
        assertNotNull(definition);
        assertEquals("floatItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "float"),
                     definition.getType());
        
        definition = model.getDefinition("doubleItem");
        assertNotNull(definition);
        assertEquals("doubleItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "double"),
                     definition.getType());
        // assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.DYNAMIC, definition.getLifecyclePolicy()); 
        
        definition = model.getDefinition("stringItem");
        assertNotNull(definition);
        assertEquals("stringItem", definition.getName());
        assertEquals(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"),
                     definition.getType());
        
        definition = model.getDefinition("stringListItem");
        assertNotNull(definition);
        assertEquals("stringListItem", definition.getName());
        assertEquals(new QName(TYPES_NAMESPACE_URI, "stringListType"),
                     definition.getType()); 
        assertEquals(LifecyclePolicy.STATIC, definition.getLifecyclePolicy());
        assertNull(definition.getDefaultValue());
    }

    public void testIllegalQNameInType() {
        try {
            buildMetadata("meta2.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) { 
            assertEquals("ILLEGAL_QNAME_EXC", ex.getCode());
        }
    }

    public void testIllegalPrefixInType() {
        try {
            buildMetadata("meta3.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("ILLEGAL_PREFIX_EXC", ex.getCode());
        }
    }
    
    public void testUnknownType() {
        try {
            buildMetadata("meta4.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("TYPE_NOT_DEFINED_IN_NAMESPACE_EXC", ex.getCode()); 
        }
        assertNotNull(buildMetadata("meta4.xml", false));
    }

    public void testUniqueName() {
        PrintStream perr = System.err;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream newPerr = new PrintStream(bout); 
        System.setErr(newPerr);
        try {
            buildMetadata("meta5.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("METADATA_VALIDATION_ERROR_EXC", ex.getCode()); 
        } finally {
            System.setErr(perr); 
        }
        assertNotNull(buildMetadata("meta5.xml", false));
    }

    public void testInvalidTypeInDefaultValue() {       
        try {
            buildMetadata("meta6.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("INVALID_TYPE_FOR_DEFAULT_VALUE_EXC", ex.getCode()); 
        }
    }

    public void testIllegalDefaultValue() {

        try {
            buildMetadata("meta7.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            // assertEquals("INVALID_DEFAULT_VALUE_EXC", ex.getCode());   
            assertEquals("DEFAULT_VALUE_UNMARSHAL_ERROR_EXC", ex.getCode());
        }
        assertNotNull(buildMetadata("meta7.xml", false));
    }
    
    public void testDefaultValue() {        
        ConfigurationMetadata model = buildMetadata("meta8.xml"); 
        Collection<ConfigurationItemMetadata> definitions = model.getDefinitions();
        assertEquals(9, definitions.size());
        
        ConfigurationItemMetadata definition = null;  
        Object defaultValue = null;
       
        definition = model.getDefinition("booleanItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("java.lang.Boolean", defaultValue.getClass().getName());
        assertEquals(true, ((Boolean)defaultValue).booleanValue());
        
        definition = model.getDefinition("integerItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("java.math.BigInteger", defaultValue.getClass().getName());
        assertEquals(44959, ((BigInteger)defaultValue).intValue());
        
        definition = model.getDefinition("longItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("java.lang.Long", defaultValue.getClass().getName());
        assertEquals(-99, ((Long)defaultValue).longValue());
        
        definition = model.getDefinition("doubleItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("java.lang.Double", defaultValue.getClass().getName());
        assertTrue(Math.abs(1234.5678 - ((Double)defaultValue).doubleValue()) < 0.5E-5);
        
        definition = model.getDefinition("stringItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("java.lang.String", defaultValue.getClass().getName());
        assertEquals("\"Hello World!\"", (String)defaultValue);
        
        definition = model.getDefinition("stringListItem");
        defaultValue = definition.getDefaultValue();
        assertNotNull(defaultValue);

        assertEquals("org.objectweb.celtix.configuration.types.StringListType", 
                     defaultValue.getClass().getName());
        List<String> l = ((StringListType)defaultValue).getItem();
        assertNotNull(l);
        assertEquals(3, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }
   
    private ConfigurationMetadata buildMetadata(String filename) {
        return buildMetadata(filename, true);
    }
    
    private ConfigurationMetadata buildMetadata(String filename, boolean doValidate) {
        InputStream is = getClass().getResourceAsStream("resources/" + filename);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        builder.setValidation(doValidate);
        try {
            return builder.build(is); 
        } catch (IOException ex) {
            return null;
        }
    }
    

}
