package org.objectweb.celtix.bus.configuration;

import java.net.URL;
import java.util.Collection;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata.LifecyclePolicy;
import org.objectweb.celtix.configuration.ConfigurationMetadata;


public class ConfigurationMetadataTest extends TestCase {

    public void testStandardTypes() throws ConfigurationException {
        ConfigurationMetadata model = buildMetadata("meta1.xml"); 
        Collection<ConfigurationItemMetadata> definitions = model.getDefinitions();
        assertEquals(12, definitions.size());
        ConfigurationItemMetadata definition = model.getDefinition("stringListItem");
        assertNotNull(definition);
        assertEquals("stringListItem", definition.getName());
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "stringListType"),
                     definition.getType());
        assertNull(definition.getDescription());
        assertEquals(LifecyclePolicy.STATIC, definition.getLifecyclePolicy());
        assertNull(definition.getDefaultValue());
        
       
        definition = model.getDefinition("otherBooleanItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "booleanType"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        // assertEquals("", definition.getDescription());
        
        definition = model.getDefinition("otherIntegerItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "integerType"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(" ", definition.getDescription());
        assertEquals(LifecyclePolicy.PROCESS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("otherLongItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "longType"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.BUS, definition.getLifecyclePolicy());
        
        definition = model.getDefinition("otherDoubleItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "doubleType"),
                     definition.getType());
        assertNotNull(definition.getDescription());
        assertEquals(definition.getName() + " description", definition.getDescription());
        assertEquals(LifecyclePolicy.DYNAMIC, definition.getLifecyclePolicy()); 
        
        definition = model.getDefinition("otherStringItem");
        assertEquals(new QName("http://celtix.objectweb.org/config-types", "stringType"),
                     definition.getType());
    }
    
    public void testIllegalQNameInType() {
        try {
            buildMetadata("meta2.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
          // ignore  
        }
    }

    public void testIllegalPrefixInType() {
        try {
            buildMetadata("meta3.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
          // ignore  
        }
    }
    
    public void testUnknownType() {
        try {
            buildMetadata("meta4.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
          // ignore  
        }
    }

    public void testUniqueName() {
        try {
            buildMetadata("meta5.xml");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
          // ignore  
        }
    }

    private ConfigurationMetadata buildMetadata(String filename) throws ConfigurationException {
        URL url = getClass().getResource("resources/" + filename);
        return new ConfigurationMetadataImpl(url);     
    }

}
