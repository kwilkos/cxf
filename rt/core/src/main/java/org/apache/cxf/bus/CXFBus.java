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

package org.apache.cxf.bus;


import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.extension.ExtensionManagerImpl;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.oldcfg.Configuration;
import org.apache.cxf.oldcfg.ConfigurationBuilder;
import org.apache.cxf.oldcfg.impl.ConfigurationBuilderImpl;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.PropertiesResolver;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.resource.SinglePropertyResolver;

public class CXFBus extends AbstractBasicInterceptorProvider implements Bus {
    
    public static final String BUS_PROPERTY_NAME = "bus";
    
    private static final String BUS_EXTENSION_RESOURCE = "META-INF/bus-extensions.xml";
    
    enum State { INITIAL, RUNNING, SHUTDOWN };

    private Map<Class, Object> extensions;
    private Configuration configuration;
    private String id;
    private State state;
    
    protected CXFBus() {
        this(new HashMap<Class, Object>());
    }

    protected CXFBus(Map<Class, Object> e) {
        this(e, null);
    }
    
    protected CXFBus(Map<Class, Object> e, Map<String, Object> properties) {
        
        extensions = e;
     
        BusConfigurationHelper helper = new BusConfigurationHelper();
        
        id = helper.getBusId(properties);
 
        ConfigurationBuilder builder = (ConfigurationBuilder)extensions.get(ConfigurationBuilder.class);
        if (null == builder) {
            builder = new ConfigurationBuilderImpl();
            extensions.put(ConfigurationBuilder.class, builder);
        }
        configuration = helper.getConfiguration(builder, id);
        
        ResourceManager resourceManager = new DefaultResourceManager();
        
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        
        properties.put(BusConfigurationHelper.BUS_ID_PROPERTY, BUS_PROPERTY_NAME);
        properties.put(BUS_PROPERTY_NAME, this);
        
        ResourceResolver propertiesResolver = new PropertiesResolver(properties);
        resourceManager.addResourceResolver(propertiesResolver);
        
        ResourceResolver busResolver = new SinglePropertyResolver(BUS_PROPERTY_NAME, this);
        resourceManager.addResourceResolver(busResolver);
   
        new ExtensionManagerImpl(BUS_EXTENSION_RESOURCE, 
                                                    Thread.currentThread().getContextClassLoader(),
                                                    extensions,
                                                    resourceManager);
        
        state = State.INITIAL;

    }

      
    public <T> T getExtension(Class<T> extensionType) {
        Object obj = extensions.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return null;
    }

    public <T> void setExtension(T extension, Class<T> extensionType) {
        extensions.put(extensionType, extension);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public void run() {
        synchronized (this) {
            if (state == State.RUNNING) {
                // REVISIT
                return;
            }
            state = State.RUNNING;

            while (state == State.RUNNING) {

                try {
                    wait();
                } catch (InterruptedException ex) {
                    // ignore;
                }
            }
        }
    }

    public void shutdown(boolean wait) {
        // TODO: invoke PreDestroy on all resources
        synchronized (this) {
            state = State.SHUTDOWN;
            notifyAll();
        }
    }
    
    protected State getState() {
        return state;
    }
}
