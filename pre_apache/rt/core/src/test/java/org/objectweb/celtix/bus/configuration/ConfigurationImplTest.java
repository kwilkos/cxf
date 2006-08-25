package org.objectweb.celtix.bus.configuration;


import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.DefaultConfigurationProviderFactory;


public class ConfigurationImplTest extends TestCase {

    private final Configuration cfg;

    public ConfigurationImplTest(String name) {
        super(name);
        
        cfg = new TestConfigurationBuilder().build("top");
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
        assertNotNull(cfg);        
        ConfigurationMetadata model = cfg.getModel();
        assertNotNull(model);
        assertEquals(18, model.getDefinitions().size()); 
    }
    
    public void testUndefined() {
        try {
            cfg.getObject("undefinedStringItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NOT_DEFINED_EXC", ex.getCode());
        }
    }
    
    
    public void testNoDefaults() {
        
        assertNull(cfg.getObject("booleanItemNoDefault"));
        try {
            cfg.getBoolean("booleanItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("shortItemNoDefault"));
        try {
            cfg.getShort("shortItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("intItemNoDefault"));
        try {
            cfg.getInt("integerItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("longItemNoDefault"));
        try {
            cfg.getLong("longItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("floatItemNoDefault"));
        try {
            cfg.getFloat("floatItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("doubleItemNoDefault"));
        try {
            cfg.getDouble("doubleItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("stringItemNoDefault"));
        try {
            cfg.getObject("stringItemNoDefault");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_NO_VALUE_EXC", ex.getCode());
        }
        assertNull(cfg.getObject("stringListItemNoDefault"));
    }
    
    public void testDefaults() {
        Object value = null;
        assertNotNull(cfg.getObject("booleanItem"));
        assertTrue(cfg.getBoolean("booleanItem"));
        assertNotNull(cfg.getObject("shortItem"));
        assertEquals(3, cfg.getShort("shortItem"));
        assertNotNull(cfg.getObject("intItem"));
        assertEquals(44959, cfg.getInt("intItem"));
        value = cfg.getObject("integerItem");
        assertNotNull(value);
        assertEquals(44959, ((BigInteger)value).intValue());
        assertNotNull(cfg.getObject("longItem"));
        assertEquals(-99, cfg.getLong("longItem"));
        assertNotNull(cfg.getObject("floatItem"));
        assertTrue(Math.abs(1234.5678 - cfg.getFloat("floatItem")) < 0.5E-3);
        assertNotNull(cfg.getObject("doubleItem"));
        assertTrue(Math.abs(1234.5678 - cfg.getDouble("doubleItem")) < 0.5E-5);
        assertNotNull(cfg.getObject("stringItem"));
        assertEquals("\"Hello World!\"", cfg.getString("stringItem"));
        value = cfg.getObject("stringListItem");
        assertNotNull(value);
        List<String> l = cfg.getStringList("stringListItem");
        assertNotNull(l);
        assertEquals(3, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }
    
    public void testTypeMismatch() {
        try {
            cfg.getStringList("booleanItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getBoolean("shortItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getShort("intItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getInt("integerItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getLong("doubleItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getDouble("stringItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.getString("stringListItem");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
    }

    public void testTypeMismatchWrite() {
        try {
            cfg.setBoolean("shortItem", true);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.setShort("intItem", (short)99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.setInt("integerItem", 99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.setLong("doubleItem", 99);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.setDouble("stringItem", 99.9);
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
        try {
            cfg.setString("stringListItem", "testString");
        } catch (ConfigurationException ex) {
            assertEquals("ITEM_TYPE_MISMATCH_EXC", ex.getCode());
        }
    }
    
}

