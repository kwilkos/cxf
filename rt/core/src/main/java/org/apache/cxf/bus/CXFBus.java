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
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.extension.ExtensionManagerImpl;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.PropertiesResolver;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.resource.SinglePropertyResolver;

public class CXFBus extends AbstractBasicInterceptorProvider implements Bus {
    
    public static final String BUS_PROPERTY_NAME = "bus";
    private static final String BUS_ID_PROPERTY_NAME = "org.apache.cxf.bus.id";
    private static final String DEFAULT_BUS_ID = "cxf";
    
    private static final String BUS_EXTENSION_RESOURCE = "META-INF/bus-extensions.xml";
    
    enum State { INITIAL, RUNNING, SHUTDOWN };

    private Map<Class, Object> extensions;
    private BusLifeCycleManager lifeCycleManager;
    private String id;
    private State state;
    
    protected CXFBus() {
        this(new HashMap<Class, Object>());
    }

    protected CXFBus(Map<Class, Object> e) {
        this(e, new HashMap<String, Object>());
    }
    
    protected CXFBus(Map<Class, Object> e, Map<String, Object> properties) {
        
        extensions = e;
        
        Configurer configurer = (Configurer)extensions.get(Configurer.class);
        if (null == configurer) {
            String cfgFile = (String)properties.get(Configurer.USER_CFG_FILE_PROPERTY_NAME);
            configurer = new ConfigurerImpl(cfgFile);
            extensions.put(Configurer.class, configurer);
        }
 
        id = getBusId(properties);
        
        ResourceManager resourceManager = new DefaultResourceManager();
        
        properties.put(BUS_ID_PROPERTY_NAME, BUS_PROPERTY_NAME);
        properties.put(BUS_PROPERTY_NAME, this);
        
        ResourceResolver propertiesResolver = new PropertiesResolver(properties);
        resourceManager.addResourceResolver(propertiesResolver);
        
        ResourceResolver busResolver = new SinglePropertyResolver(BUS_PROPERTY_NAME, this);
        resourceManager.addResourceResolver(busResolver);
        
        extensions.put(ResourceManager.class, resourceManager);

        new ExtensionManagerImpl(BUS_EXTENSION_RESOURCE, 
                                 Thread.currentThread().getContextClassLoader(),
                                 extensions,
                                 resourceManager);
        
        state = State.INITIAL;
        
        lifeCycleManager = this.getExtension(BusLifeCycleManager.class);
        if (null != lifeCycleManager) {
            lifeCycleManager.initComplete();
        }

    }

      
    public final <T> T getExtension(Class<T> extensionType) {
        Object obj = extensions.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return null;
    }

    public <T> void setExtension(T extension, Class<T> extensionType) {
        extensions.put(extensionType, extension);
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
        lifeCycleManager = this.getExtension(BusLifeCycleManager.class);
        if (null != lifeCycleManager) {
            lifeCycleManager.preShutdown();
        }
        synchronized (this) {
            state = State.SHUTDOWN;
            notifyAll();
        }
        if (null != lifeCycleManager) {
            lifeCycleManager.postShutdown();
        }
    }
    
    protected State getState() {
        return state;
    }
    
    private String getBusId(Map<String, Object> properties) {

        String busId = null;

        // first check properties
        if (null != properties) {
            busId = (String)properties.get(BUS_ID_PROPERTY_NAME);
            if (null != busId && !"".equals(busId)) {
                return busId;
            }
        }

        // next check system properties
        busId = System.getProperty(BUS_ID_PROPERTY_NAME);
        if (null != busId && !"".equals(busId)) {
            return busId;
        }

        // otherwise use default
        return DEFAULT_BUS_ID;
    }
}
