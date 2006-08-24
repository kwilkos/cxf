package org.apache.cxf.configuration.impl;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.cxf.configuration.CompoundName;
import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.configuration.ConfigurationBuilder;
import org.apache.cxf.configuration.ConfigurationException;
import org.apache.cxf.configuration.ConfigurationMetadata;
import org.apache.cxf.configuration.ConfigurationProvider;

import org.easymock.EasyMock;

public class ConfigurationBuilderImplTest extends TestCase {
    
    private static final String TEST_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/configuration/test/meta1";
    private static final String BUS_CONFIGURATION_URI = "http://celtix.objectweb.org/bus/bus-config";
    private static final String UNKNOWN_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/unknown/unknown-config";  
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME = 
        TestProvider.class.getName();
    
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY = 
        "org.apache.cxf.configuration.ConfigurationProviderClass";
    
    
    private String orgProviderClassname;
    
    public void setUp() {
        orgProviderClassname = System.getProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY);
        System.setProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY, 
                           DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME);      
    }
    
    public void tearDown() {
        if (null != orgProviderClassname) {
            System.setProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY, orgProviderClassname);
        } else {
            System.clearProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY);
        }
    }
    
    public void testGetModel() {
        ConfigurationBuilderImpl builder = new ConfigurationBuilderImpl();
        try {
            builder.getModel(UNKNOWN_CONFIGURATION_URI);
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
        
        ConfigurationMetadata unknownModel = EasyMock.createMock(ConfigurationMetadata.class);
        builder.addModel(UNKNOWN_CONFIGURATION_URI, unknownModel);
        
        ConfigurationMetadata model = builder.getModel(UNKNOWN_CONFIGURATION_URI);
        assertSame(unknownModel, model);
    }
    
    public void testGetConfigurationUnknownNamespace() {
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        CompoundName id = new CompoundName("celtix");
        try {
            builder.getConfiguration(UNKNOWN_CONFIGURATION_URI, id);            
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
    }
   
    public void testBuildConfiguration() throws Exception {
        URL url = getClass().getResource(getClass().getName() + ".class");        
        CompoundName id = new CompoundName("celtix");
        ConfigurationBuilderImpl builder = new ConfigurationBuilderImpl(url);
        ConfigurationMetadataImpl model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(BUS_CONFIGURATION_URI);
        builder.addModel(BUS_CONFIGURATION_URI, model);
        model = new ConfigurationMetadataImpl();
        
        Configuration c = builder.buildConfiguration(BUS_CONFIGURATION_URI, id);
        assertNotNull(c);
        List<ConfigurationProvider> providers = c.getProviders();
        assertEquals(1, providers.size());
        TestProvider tp = (TestProvider)providers.get(0);
        assertSame(url, tp.url);
        assertSame(c, tp.configuration);
        assertNull(tp.name);
    }
    
    public void testGetConfiguration() throws Exception {
        URL url = getClass().getResource(getClass().getName() + ".class");        
        CompoundName id = new CompoundName("celtix");
        ConfigurationBuilderImpl builder = new ConfigurationBuilderImpl(url);
        ConfigurationMetadataImpl model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(BUS_CONFIGURATION_URI);
        builder.addModel(BUS_CONFIGURATION_URI, model);
        model = new ConfigurationMetadataImpl();
        
        Configuration c = builder.getConfiguration(BUS_CONFIGURATION_URI, id);
        assertNotNull(c);
        List<ConfigurationProvider> providers = c.getProviders();
        assertEquals(1, providers.size());
        TestProvider tp = (TestProvider)providers.get(0);
        assertSame(url, tp.url);
        assertSame(c, tp.configuration);
        assertNull(tp.name);
    }    
    
    public void testGetResourceName() {
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        assertNull("Found metadata resource", builder.getModel(BUS_CONFIGURATION_URI));
        assertNotNull("Could not find metadata resource", builder.getModel(TEST_CONFIGURATION_URI));   
    }
   
}
