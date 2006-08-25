package org.objectweb.celtix.bus.configuration.spring;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.LeafConfigurationBuilder;
import org.objectweb.celtix.bus.configuration.TopConfigurationBuilder;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.configuration.types.StringListType;

public class ConfigurationProviderImplTest extends TestCase {
    
    private String originalConfigFile;
    
    public void setUp() {
        originalConfigFile = System.getProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME);     
    }

    public void tearDown() {
        if (null == originalConfigFile) {
            Properties properties = System.getProperties();
            properties.remove(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME);
            System.setProperties(properties);
        } else {
            System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, originalConfigFile);
        }
        ConfigurationProviderImpl.getBeanFactories().clear();
    }
    
    public void testInvalidBeanDefinitionsResourceURL() {
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, "resources\top1.xml");
        try {
            new TopConfigurationBuilder().build("top");
        } catch (ConfigurationException ex) {
            assertEquals("MALFORMED_URL_PROPERTY", ex.getCode());
        }
    }

    public void testNoBeanDefinitionsFile() {
        Configuration top = new TopConfigurationBuilder().build("top");
        List<ConfigurationProvider> providers = top.getProviders();
        assertEquals(2, providers.size());
        assertTrue(providers.get(1) instanceof ConfigurationProviderImpl); 
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers.get(1);       
        assertNull(cpi.getBean());   
    }
    
    public void testInvalidBeanDefinitionFile() {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top-invalid.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        new TopConfigurationBuilder().build("top");
        assertNull(ConfigurationProviderImpl.getBeanFactories().get(url));  
    }
    
    public void testBeanClassNotFound() {
        
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top-wrong-class.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        
        // The non-existing class is detected when the bean factory is initialised - not 
        // later when the bean is created.
        
        new TopConfigurationBuilder().build("top");
        assertNull(ConfigurationProviderImpl.getBeanFactories().get(url)); 
    }
    
    public void testNoSuchBean() {

        URL url = ConfigurationProviderImplTest.class.getResource("resources/top1.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());

        Configuration top = new TopConfigurationBuilder().build("top2");

        List<ConfigurationProvider> providers = top.getProviders();
        assertEquals(2, providers.size());
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers.get(1);
        assertNull(cpi.getBean());
        

    }
    
    public void testDefaultBeanCreation() throws MalformedURLException {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top1.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
            
        Configuration top = new TopConfigurationBuilder().build("top1");
        List<ConfigurationProvider> providers = top.getProviders();
        assertEquals(2, providers.size());
        assertTrue(providers.get(1) instanceof ConfigurationProviderImpl); 
    }
    
    public void testBeanCreationUsingValueAsText() throws MalformedURLException {
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top2.xml");
        System.setProperty(ConfigurationProviderImpl.CONFIG_FILE_PROPERTY_NAME, url.toExternalForm());
        Configuration top = new TopConfigurationBuilder().build("top2");
        
        List<ConfigurationProvider> providers = top.getProviders();
        assertEquals(2, providers.size());
        assertTrue(providers.get(1) instanceof ConfigurationProviderImpl); 
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers.get(1);
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
        Configuration top = new TopConfigurationBuilder().build("top22");
        
        List<ConfigurationProvider> providers = top.getProviders();
        assertEquals(2, providers.size());
        assertTrue(providers.get(1) instanceof ConfigurationProviderImpl); 
        
        ConfigurationProviderImpl cpi = (ConfigurationProviderImpl)providers.get(1);
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
        
        Configuration top = new TopConfigurationBuilder().build("top2");
        Configuration leaf = new LeafConfigurationBuilder().build(top, "leaf");
        
        List<ConfigurationProvider> providers = leaf.getProviders();
        assertEquals(2, providers.size());
        
        
        Object o = providers.get(1).getObject("stringLeafItem");
        assertTrue(o instanceof String);
        assertEquals("Don't fear the reaper", (String)o);
        
        o = providers.get(1).getObject("longLeafItemNoDefault");
        assertEquals(99, ((Long)o).longValue()); 
    }
    
}
