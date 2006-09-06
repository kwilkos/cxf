/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.oldcfg.impl;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.cxf.oldcfg.CompoundName;
import org.apache.cxf.oldcfg.Configuration;
import org.apache.cxf.oldcfg.ConfigurationBuilder;
import org.apache.cxf.oldcfg.ConfigurationException;
import org.apache.cxf.oldcfg.ConfigurationMetadata;
import org.apache.cxf.oldcfg.ConfigurationProvider;

import org.easymock.EasyMock;

public class ConfigurationBuilderImplTest extends TestCase {
    
    private static final String TEST_CONFIGURATION_URI = 
        "http://cxf.apache.org/configuration/test/meta1";
    private static final String BUS_CONFIGURATION_URI = "http://cxf.apache.org/bus/bus-config";
    private static final String UNKNOWN_CONFIGURATION_URI = 
        "http://cxf.apache.org/unknown/unknown-config";  
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
        CompoundName id = new CompoundName("cxf");
        try {
            builder.getConfiguration(UNKNOWN_CONFIGURATION_URI, id);            
        } catch (ConfigurationException ex) {
            assertEquals("UNKNOWN_NAMESPACE_EXC", ex.getCode());
        }
    }
   
    public void testBuildConfiguration() throws Exception {
        URL url = getClass().getResource(getClass().getName() + ".class");        
        CompoundName id = new CompoundName("cxf");
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
        CompoundName id = new CompoundName("cxf");
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
