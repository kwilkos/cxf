package org.objectweb.celtix.bus.configuration;


import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.Configurator;


public class AbstractConfigurationImplTest extends TestCase {

    private Configuration top;

    public AbstractConfigurationImplTest(String name) {
        super(name);
        top = new TopConfiguration("top");
    }
    
    public void testConstruction() {
        assertNotNull(top);        
        ConfigurationMetadata model = top.getModel();
        assertNotNull(model);
        assertEquals(12, model.getDefinitions().size()); 
    }
    
    public void testConfigurators() {
        Configuration topConfiguration = new TopConfiguration("TOP");
        Configurator topConfigurator = topConfiguration.getConfigurator();
        assertNotNull(topConfigurator);
        assertTrue(topConfiguration == topConfigurator.getConfiguration());
        assertNull(topConfigurator.getHook());
        Collection<Configurator> topClients = topConfigurator.getClients();
        assertEquals(0, topClients.size());    
        
        Configuration leafConfiguration = new LeafConfiguration(topConfiguration, "LEAF");
        assertEquals(1, topClients.size());   
        Configurator leafConfigurator = leafConfiguration.getConfigurator();
        assertNotNull(leafConfigurator);
        assertTrue(leafConfiguration == leafConfigurator.getConfiguration());
        Configurator hook = leafConfigurator.getHook();
        assertNotNull(hook);
        assertTrue(hook == topConfigurator);
        Collection<Configurator> leafClients = leafConfigurator.getClients();
        assertEquals(0, leafClients.size());   
        
        QName cidTop = topConfiguration.getName();
        assertEquals("http://celtix.objectweb.org/configuration/test/top", cidTop.getNamespaceURI());
        assertEquals("TOP", cidTop.getLocalPart());
        
        QName cidLeaf = leafConfiguration.getName();
        assertEquals("http://celtix.objectweb.org/configuration/test/leaf", cidLeaf.getNamespaceURI());
        assertEquals("LEAF", cidLeaf.getLocalPart());
        
        assertTrue(cidTop.equals(cidTop));
        assertTrue(!cidTop.equals(cidLeaf));
        assertTrue(!cidTop.equals(this));
        
        assertTrue(!cidTop.toString().equals(cidLeaf.toString()));
        assertTrue(cidTop.hashCode() != cidLeaf.hashCode());
     
        
        topConfigurator.unregisterClient(leafConfigurator);
        assertEquals(0, topClients.size());
        assertNotNull(leafConfigurator.getHook());
        
    }
     
    public void testUndefined() {
        try {
            top.getObject("undefinedStringItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NOT_DEFINED_EXC", ex.getCode());
        }
    }
    
    
    public void testNoDefaults() {
        
        assertNull(top.getObject("booleanItemNoDefault"));
        try {
            top.getBoolean("booleanItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("integerItemNoDefault"));
        try {
            top.getInteger("integerItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("longItemNoDefault"));
        try {
            top.getLong("longItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("doubleItemNoDefault"));
        try {
            top.getDouble("doubleItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("stringItemNoDefault"));
        try {
            top.getObject("stringItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("stringListItemNoDefault"));
    }
    
    public void testDefaults() {
        assertNotNull(top.getObject("booleanItem"));
        assertTrue(top.getBoolean("booleanItem"));
        assertNotNull(top.getObject("integerItem"));
        assertEquals(44959, top.getInteger("integerItem"));
        assertNotNull(top.getObject("longItem"));
        assertEquals(-99, top.getLong("longItem"));
        assertNotNull(top.getObject("doubleItem"));
        assertTrue(Math.abs(1234.5678 - top.getDouble("doubleItem")) < 0.5E-5);
        assertNotNull(top.getObject("stringItem"));
        assertEquals("\"Hello World!\"", top.getString("stringItem"));
        Object value = top.getObject("stringListItem");
        assertNotNull(value);
        List<String> l = top.getStringList("stringListItem");
        assertNotNull(l);
        assertEquals(3, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }
    
    public void testTypeMismatch() {
        try {
            top.getStringList("booleanItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getBoolean("integerItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getInteger("longItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getLong("doubleItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getDouble("stringItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getString("stringListItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
    }
    
}

