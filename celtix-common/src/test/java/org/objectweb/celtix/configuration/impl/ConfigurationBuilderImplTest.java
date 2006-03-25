package org.objectweb.celtix.configuration.impl;

import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class ConfigurationBuilderImplTest extends TestCase {
    
    private static final String BUS_CONFIGURATION_URI = "http://celtix.objectweb.org/bus/bus-config";
    private static final String HTTP_LISTENER_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/transports/http/http-listener-config";
    private static final String HTTP_LISTENER_CONFIGURATION_ID = "http-listener.44959";
    private static final String UNKNOWN_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/unknown/unknown-config";    
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME =
        TestProvider.class.getName();
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY = 
        "org.objectweb.celtix.bus.configuration.ConfigurationProvider";
    
    
    private String orgProviderClassname;
    private String orgBuilderClassname;
    
    public void setUp() {
        orgProviderClassname = System.getProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY);
        System.setProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY, 
                           DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME);
        orgBuilderClassname = System.getProperty(ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY);
        System.setProperty(ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY, 
                           ConfigurationBuilderImpl.class.getName());
        
    }
    
    public void tearDown() {
        if (null != orgProviderClassname) {
            System.setProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY, orgProviderClassname);
        } else {
            Properties p = System.getProperties();
            p.remove(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY);
            System.setProperties(p);
        }
        
        if (null != orgBuilderClassname) {
            System.setProperty(ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY, 
                               orgBuilderClassname);
        } else {
            Properties p = System.getProperties();
            p.remove(ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY);
            System.setProperties(p);
        }
    }
    
    public void testGetBuilder() {
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder(null);
        assertNotNull(builder);
        assertTrue(builder instanceof ConfigurationBuilderImpl);
    }
    
    public void testGetConfigurationUnknownNamespace() {
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        try {
            builder.getConfiguration(UNKNOWN_CONFIGURATION_URI, "celtix");            
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
        Configuration parent = EasyMock.createMock(Configuration.class);
        try {
            builder.getConfiguration(UNKNOWN_CONFIGURATION_URI, "celtix", parent);            
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
    }
    
    public void testGetAddModel() {
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder(null);
        try {
            builder.getModel(UNKNOWN_CONFIGURATION_URI);
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
        
        ConfigurationMetadata unknownModel = EasyMock.createMock(ConfigurationMetadata.class);
        unknownModel.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(UNKNOWN_CONFIGURATION_URI);
        EasyMock.replay(unknownModel);
        builder.addModel(unknownModel);
        assertSame(unknownModel, builder.getModel(UNKNOWN_CONFIGURATION_URI));
        EasyMock.verify(unknownModel); 
    }
    
    public void testAddModel() throws Exception {
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder(null);
        try {
            builder.getModel("a.wsdl");
        } catch (ConfigurationException ex) {
            assertEquals("METADATA_RESOURCE_EXC", ex.getCode());
        }
    }
    
    public void testGetConfiguration() {
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        ConfigurationMetadata model = EasyMock.createMock(ConfigurationMetadata.class);
        model.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(BUS_CONFIGURATION_URI);
        EasyMock.replay(model);
        builder.addModel(model);
        assertNull(builder.getConfiguration(BUS_CONFIGURATION_URI, "celtix"));        
        EasyMock.verify(model);
        
        model = EasyMock.createMock(ConfigurationMetadata.class);
        model.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(HTTP_LISTENER_CONFIGURATION_URI);
        EasyMock.replay(model);
        builder.addModel(model);
        Configuration parent = EasyMock.createMock(Configuration.class);
        assertNull(builder.getConfiguration(HTTP_LISTENER_CONFIGURATION_URI, 
                                            HTTP_LISTENER_CONFIGURATION_ID, parent));
    }

    public void testInvalidParentConfiguration() {
        String id = "celtix";
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        ConfigurationMetadataImpl model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(BUS_CONFIGURATION_URI);
        model.setParentNamespaceURI(null);
        builder.addModel(model);
        model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(HTTP_LISTENER_CONFIGURATION_URI);
        model.setParentNamespaceURI(BUS_CONFIGURATION_URI);
        builder.addModel(model);
        
        Configuration parent = builder.buildConfiguration(BUS_CONFIGURATION_URI, id, null);
        assertNotNull(parent);

        try {
            builder.buildConfiguration(HTTP_LISTENER_CONFIGURATION_URI, 
                                       HTTP_LISTENER_CONFIGURATION_ID, null);
            fail("Did not throw expected exception");
        } catch (ConfigurationException e) {
            String expectedErrorMsg = "Configuration " + HTTP_LISTENER_CONFIGURATION_URI
                + " is not a valid top configuration.";
            assertEquals("Unexpected exception message", expectedErrorMsg, e.getMessage());
        } catch (Exception e) {
            fail("Caught unexpected exception");
        }
    }

    /*    
    public void testInvalidChildConfiguration() {
        String id = "celtix";
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        ConfigurationMetadataImpl model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(BUS_CONFIGURATION_URI);
        model.setParentNamespaceURI(null);
        builder.addModel(model);
        model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(HTTP_LISTENER_CONFIGURATION_URI);
        model.setParentNamespaceURI(BUS_CONFIGURATION_URI);
        builder.addModel(model);
        
        Configuration parent = builder.buildConfiguration(BUS_CONFIGURATION_URI, id, null);
        assertNotNull(parent);

        //build a http configuration that is the child of bus config
        Configuration wrongParent = builder.buildConfiguration(HTTP_LISTENER_CONFIGURATION_URI, 
                                       HTTP_LISTENER_CONFIGURATION_ID, parent);

        assertNotNull(parent);

        try {
            builder.buildConfiguration(HTTP_LISTENER_CONFIGURATION_URI, 
                                       HTTP_LISTENER_CONFIGURATION_ID, wrongParent);
            fail("Did not throw expected exception");
        } catch (ConfigurationException e) {
            String expectedErrorMsg = "Configuration " + HTTP_LISTENER_CONFIGURATION_URI
                + " is not a valid child configuration of " + HTTP_LISTENER_CONFIGURATION_URI + ".";
            assertEquals("Unexpected exception message", expectedErrorMsg, e.getMessage());
        } catch (Exception e) {
            fail("Caught unexpected exception");
        }
    }
    */

    public void testBuildConfiguration() throws Exception {
                                                       
        String id = "celtix";
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        ConfigurationMetadataImpl model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(BUS_CONFIGURATION_URI);
        builder.addModel(model);
        model = new ConfigurationMetadataImpl();
        model.setNamespaceURI(HTTP_LISTENER_CONFIGURATION_URI);
        builder.addModel(model);
        Configuration parent = builder.buildConfiguration(BUS_CONFIGURATION_URI, id);
        assertNotNull(parent);
        Configuration child = builder.buildConfiguration(HTTP_LISTENER_CONFIGURATION_URI, 
                                                         HTTP_LISTENER_CONFIGURATION_ID);
        assertNotNull(child);
    }
}
