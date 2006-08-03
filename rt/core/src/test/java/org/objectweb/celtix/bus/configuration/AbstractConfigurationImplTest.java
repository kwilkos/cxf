package org.objectweb.celtix.bus.configuration;


import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.Configurator;
import org.objectweb.celtix.configuration.impl.ConfigurationImpl;
import org.objectweb.celtix.configuration.impl.DefaultConfigurationProviderFactory;


public class AbstractConfigurationImplTest extends TestCase {

    private final Configuration top;

    public AbstractConfigurationImplTest(String name) {
        super(name);
        top = new TopConfigurationBuilder().build("top");
    }
    
    public void testDefaultConfigurationProviderFactory() throws NoSuchMethodException, IOException  {
        Method m = DefaultConfigurationProviderFactory.class
            .getDeclaredMethod("getDefaultProviderClassName");
        DefaultConfigurationProviderFactory factory = 
            EasyMock.createMock(DefaultConfigurationProviderFactory.class, new Method[] {m});
        factory.getDefaultProviderClassName();
        org.easymock.EasyMock.expectLastCall().andReturn("org.objectweb.celtix.some.Unknown");
        EasyMock.replay(factory);
        try {
            factory.createDefaultProvider();
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("DEFAULT_PROVIDER_INSTANTIATION_EXC", ex.getCode()); 
        }

        factory = DefaultConfigurationProviderFactory.getInstance();
        assertNotNull(factory.createDefaultProvider());
    }
    
    public void testConstruction() {
        assertNotNull(top);        
        ConfigurationMetadata model = top.getModel();
        assertNotNull(model);
        assertEquals(18, model.getDefinitions().size()); 
    }
    
    public void testConfigurators() {
        ConfigurationImpl topConfiguration = 
            (ConfigurationImpl)new TopConfigurationBuilder().build("TOP");
        Configurator topConfigurator = topConfiguration.getConfigurator();
        assertNotNull(topConfigurator);
        assertTrue(topConfiguration == topConfigurator.getConfiguration());
        assertNull(topConfigurator.getHook());
        Collection<Configurator> topClients = topConfigurator.getClients();
        assertEquals(0, topClients.size());    
        
        ConfigurationImpl leafConfiguration = 
            (ConfigurationImpl)new LeafConfigurationBuilder().build(topConfiguration, "LEAF");
        assertEquals(1, topClients.size());   
        Configurator leafConfigurator = leafConfiguration.getConfigurator();
        assertNotNull(leafConfigurator);
        assertTrue(leafConfiguration == leafConfigurator.getConfiguration());
        Configurator hook = leafConfigurator.getHook();
        assertNotNull(hook);
        assertTrue(hook == topConfigurator);
        Collection<Configurator> leafClients = leafConfigurator.getClients();
        assertEquals(0, leafClients.size());   
        
        Object cidTop = topConfiguration.getId();
        assertEquals("TOP", cidTop.toString());
        
        Object cidLeaf = leafConfiguration.getId();
        assertEquals("LEAF", cidLeaf.toString());
        
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
        assertNull(top.getObject("shortItemNoDefault"));
        try {
            top.getShort("shortItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("intItemNoDefault"));
        try {
            top.getInt("integerItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("longItemNoDefault"));
        try {
            top.getLong("longItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(top.getObject("floatItemNoDefault"));
        try {
            top.getFloat("floatItemNoDefault");
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
        Object value = null;
        assertNotNull(top.getObject("booleanItem"));
        assertTrue(top.getBoolean("booleanItem"));
        assertNotNull(top.getObject("shortItem"));
        assertEquals(3, top.getShort("shortItem"));
        assertNotNull(top.getObject("intItem"));
        assertEquals(44959, top.getInt("intItem"));
        value = top.getObject("integerItem");
        assertNotNull(value);
        assertEquals(44959, ((BigInteger)value).intValue());
        assertNotNull(top.getObject("longItem"));
        assertEquals(-99, top.getLong("longItem"));
        assertNotNull(top.getObject("floatItem"));
        assertTrue(Math.abs(1234.5678 - top.getFloat("floatItem")) < 0.5E-3);
        assertNotNull(top.getObject("doubleItem"));
        assertTrue(Math.abs(1234.5678 - top.getDouble("doubleItem")) < 0.5E-5);
        assertNotNull(top.getObject("stringItem"));
        assertEquals("\"Hello World!\"", top.getString("stringItem"));
        value = top.getObject("stringListItem");
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
            top.getBoolean("shortItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getShort("intItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.getInt("integerItem");
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

    public void testTypeMismatchWrite() {
        try {
            top.setBoolean("shortItem", true);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.setShort("intItem", (short)99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.setInt("integerItem", 99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.setLong("doubleItem", 99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.setDouble("stringItem", 99.9);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            top.setString("stringListItem", "testString");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
    }
    
}

