package org.objectweb.celtix.bus.configuration;

import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata.LifecyclePolicy;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.StringListType;


public class ConfigurationMetadataTest extends TestCase {

    public void testStandardTypes() throws ConfigurationException {
             
        ConfigurationMetadata model = buildMetadata("meta1.xml"); 
        Collection<ConfigurationItemMetadata> definitions = model.getDefinitions();
        assertEquals(12, definitions.size());
        ConfigurationItemMetadata definition = model.getDefinition("stringListItem");
        assertNotNull(definition);
        assertEquals("stringListItem", definition.getName());
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "stringList"),
                     definition.getType());
        assertNull(definition.getDescription());
        assertEquals(LifecyclePolicy.STATIC, definition.getLifecyclePolicy());
        assertNull(definition.getDefaultValue());
        
       
        definition = model.getDefinition("otherBooleanItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "boolean"),
                     definition.getType());
        assertNull(definition.getDescription());
        // assertEquals("", definition.getDescription());
        
        definition = model.getDefinition("otherIntegerItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "integer"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(" ", definition.getDescription());
        assertEquals(LifecyclePolicy.PROCESS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("otherLongItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "long"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.BUS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("otherDoubleItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "double"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.DYNAMIC, definition.getLifecyclePolicy()); 
        
        definition = model.getDefinition("otherStringItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "string"),
                     definition.getType());
    }

    public void testIllegalQNameInType() {
        try {
            buildMetadata("meta2.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) { 
            assertEquals("ILLEGAL_QNAME", ex.getCode());
        }
    }

    public void testIllegalPrefixInType() {
        try {
            buildMetadata("meta3.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("ILLEGAL_PREFIX", ex.getCode());
        }
    }
    
    public void testUnknownType() {
        try {
            buildMetadata("meta4.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_TYPE", ex.getCode()); 
        }
    }

    public void testUniqueName() {
        try {
            buildMetadata("meta5.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("METADATA_VALIDATION_ERROR", ex.getCode()); 
        }
    }

    
    public void testInvalidTypeInDefaultValue() {       
        try {
            buildMetadata("meta6.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("INVALID_TYPE_FOR_DEFAULT_VALUE", ex.getCode()); 
        }
    }

    public void testIllegalDefaultValue() {

        try {
            buildMetadata("meta7.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("INVALID_DEFAULT_VALUE", ex.getCode());          
        }
    }

    
    public void testDefaultValue() throws ConfigurationException {        
        ConfigurationMetadata model = buildMetadata("meta8.xml"); 
        Collection<ConfigurationItemMetadata> definitions = model.getDefinitions();
        assertEquals(6, definitions.size());
        
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
        assertEquals("org.objectweb.celtix.configuration.StringListType", defaultValue.getClass().getName());
        List<String> l = ((StringListType)defaultValue).getItem();
        assertNotNull(l);
        assertEquals(3, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }

    private ConfigurationMetadata buildMetadata(String filename) throws ConfigurationException {
        URL url = getClass().getResource("resources/" + filename);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        return builder.build(url);     
    }
    

}
