package org.objectweb.celtix.bus.configuration.spring;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.LeafConfiguration;
import org.objectweb.celtix.bus.configuration.TopConfiguration;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.configuration.types.StringListType;

public class ConfigurationProviderImplTest extends TestCase {
    
    private String originalConfigDir;
    private String originalConfigFile;
    
    public void setUp() {
        originalConfigDir = System.getProperty(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME);
        originalConfigFile = System.getProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME);
    }

    public void tearDown() {
        if (null == originalConfigDir) {
            Properties properties = System.getProperties();
            properties.remove(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME);
            System.setProperties(properties);
        } else {
            System.setProperty(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME, originalConfigDir);
        }
        if (null == originalConfigFile) {
            Properties properties = System.getProperties();
            properties.remove(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME);
            System.setProperties(properties);
        } else {
            System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, originalConfigFile);
        }
    }
    
    public void testInvalidBeanDefinitionsResourceURL() {
        System.setProperty(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME, "c:\\resources\\sub");
        try {
            new TopConfiguration("top");
        } catch (ConfigurationException ex) {
            assertEquals("MALFORMED_URL_PROPERTY", ex.getCode());
        }
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, "resources\top1.xml");
        try {
            new TopConfiguration("top");
        } catch (ConfigurationException ex) {
            assertEquals("MALFORMED_URL_PROPERTY", ex.getCode());
        }
    }

    public void testNoBeanDefinitionsFile() {
        /*
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top-no-beans.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        */
        Configuration top = new TopConfiguration("top");
        ConfigurationProvider[] providers = top.getProviders();
        assertEquals(1, providers.length);
        assertTrue(providers[0] instanceof ConfigurationProviderImpl); 
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers[0];       
        assertNull(cpi.getBean());   
    }
    
    public void testInvalidBeanDefinitionFile() {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top-invalid.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        
        try {
            new TopConfiguration("top");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("BEAN_FACTORY_CREATION_EXC", ex.getCode());
        }
    }
    
    public void testBeanClassNotFound() {
        
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top-wrong-class.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        
        // The non-existing class is detected when the bean factory is initialised - not 
        // later when the bean is created.
        
        try {
            new TopConfiguration("top");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("BEAN_FACTORY_CREATION_EXC", ex.getCode());
        }
    }
    
    public void testNoSuchBean() {
        
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top1.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
          
        try {
            new TopConfiguration("top2");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("NO_SUCH_BEAN_EXC", ex.getCode());
        }
    }
    
    public void testDefaultBeanCreation() throws MalformedURLException {
        String surl = ConfigurationProviderImplTest.class.getResource("resources/top1.xml").toString();
        int index = surl.lastIndexOf("/top1.xml");
        URL url = new URL(surl.substring(0, index));
        System.setProperty(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME, url.toExternalForm());
            
        Configuration top = new TopConfiguration("top1");
        ConfigurationProvider[] providers = top.getProviders();
        assertEquals(1, providers.length);
        assertTrue(providers[0] instanceof ConfigurationProviderImpl); 
    }
    
    public void testBeanCreationUsingValueAsText() throws MalformedURLException {
        String surl = ConfigurationProviderImplTest.class.getResource("resources/top2.xml").toString();
        int index = surl.lastIndexOf("/top2.xml");
        URL url = new URL(surl.substring(0, index));
        System.setProperty(ConfigurationProviderImpl.CONFIG_DIR_PROPERTY_NAME, url.toExternalForm());
        Configuration top = null;
        
        try {
            top = new TopConfiguration("top2");
        } catch (Exception ex) {
            Throwable e = ex;
            while (null != e.getCause()) {
                e = e.getCause(); 
            }
            System.err.println("Original cause: ");
            e.printStackTrace();
            fail();
        }
        
        ConfigurationProvider[] providers = top.getProviders();
        assertEquals(1, providers.length);
        assertTrue(providers[0] instanceof ConfigurationProviderImpl); 
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers[0];
        Object o;
        
        o = cpi.getObject("booleanItem");
        assertTrue(o instanceof Boolean);
        assertTrue(!((Boolean)o).booleanValue());
        
        o = cpi.getObject("integerItem");
        assertTrue(o instanceof BigInteger);
        assertEquals(1997, ((BigInteger)o).intValue());
        
        o = cpi.getObject("longItem");
        assertNull(o);
        
        o = cpi.getObject("doubleItem");  
        assertTrue(o instanceof Double);
        assertTrue(Math.abs(((Double)o).doubleValue() - 9876.54321) < 0.5E-5);
 
        o = cpi.getObject("stringItem");
        assertTrue(o instanceof String);
        assertEquals("not the default", (String)o);
              
        try {
            cpi.getObject("noItem");
        } catch (ConfigurationException ex) {
            assertEquals("BEAN_INCOVATION_EXC", ex.getCode());
        }
    }
    
    public void testBeanCreationUsingValueAsElements() throws MalformedURLException {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top2.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        Configuration top = null;
        
        try {
            top = new TopConfiguration("top22");
        } catch (Exception ex) {
            Throwable e = ex;
            while (null != e.getCause()) {
                e = e.getCause(); 
            }
            System.err.println("Original cause: ");
            e.printStackTrace();
            fail();
        }
        
        ConfigurationProvider[] providers = top.getProviders();
        assertEquals(1, providers.length);
        assertTrue(providers[0] instanceof ConfigurationProviderImpl); 
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers[0];
        Object o;
        
        o = cpi.getObject("booleanItem");
        assertTrue(o instanceof Boolean);
        assertTrue(!((Boolean)o).booleanValue());
        
        o = cpi.getObject("integerItem");
        assertTrue(o instanceof BigInteger);
        assertEquals(1997, ((BigInteger)o).intValue());
        
        o = cpi.getObject("longItem");
        assertTrue(o instanceof Long);
        assertEquals(99, ((Long)o).longValue());
        
        o = cpi.getObject("doubleItem");  
        assertTrue(o instanceof Double);
        assertTrue(Math.abs(((Double)o).doubleValue() - 9876.54321) < 0.5E-5);
        
        o = cpi.getObject("stringItem");
        assertTrue(o instanceof String);
        assertEquals("not the default", (String)o);
        
        o = cpi.getObject("stringListItem");
        assertTrue(o instanceof StringListType);

        List<String> l = ((StringListType)o).getItem();
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals("something", l.get(0));
        assertEquals("else", l.get(1));
       
    }
    
    public void testBeanCreationSimpleHierarchy() {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top2.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        
        Configuration top = new TopConfiguration("top2");
        Configuration leaf = new LeafConfiguration(top, "leaf");
        
        ConfigurationProvider[] providers = leaf.getProviders();
        assertEquals(1, providers.length);
        
        
        Object o = providers[0].getObject("stringLeafItem");
        assertTrue(o instanceof String);
        assertEquals("Don't fear the reaper", (String)o);
        
        o = providers[0].getObject("longLeafItemNoDefault");
        assertEquals(99, ((Long)o).longValue()); 
    }
}
