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

package org.apache.cxf.bus.spring;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class SpringBusFactory implements BusFactory {
    
    private static final String DEFAULT_CXF_CFG_FILE = "META-INF/cxf/cxf.xml";
    private static final String DEFAULT_CXF_EXT_CFG_FILE = "META-INF/cxf/cxf-extension.xml";
    private static final String DEFAULT_BUS_ID = "cxf";
    
    private static final Logger LOG = LogUtils.getL7dLogger(SpringBusImpl.class);
    
    private static Bus defaultBus;

    public synchronized Bus getDefaultBus() {
        if (null == defaultBus) {
            defaultBus = new SpringBusImpl();
        }
        return defaultBus;
    }

    public void setDefaultBus(Bus bus) {
        defaultBus = bus;
    }
    
    public Bus createBus() {
        return createBus(null);
    }
    
    public Bus createBus(String cfgFile) {
        return createBus(cfgFile, true);
    }
    
    public Bus createBus(String cfgFile, boolean includeDefaults) {
        
        List<String> cfgFileLocations = new ArrayList<String>();
        
        if (null == cfgFile) {
            cfgFile = System.getProperty(Configurer.USER_CFG_FILE_PROPERTY_NAME);
        }
        if (null == cfgFile) {
            cfgFile = Configurer.DEFAULT_USER_CFG_FILE;
        }
        ClassPathResource cpr = new ClassPathResource(cfgFile);
        if (cpr.exists()) {
            cfgFileLocations.add(cfgFile);
        } else {
            LOG.log(Level.INFO, new Message("USER_CFG_FILE_NOT_FOUND_MSG", LOG, cfgFile).toString());
        }
        
        if (includeDefaults) {
            try {
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                    .getResources(DEFAULT_CXF_CFG_FILE);                
                if (resources.hasMoreElements()) {
                    cfgFileLocations.add(DEFAULT_CXF_CFG_FILE);
                }
                resources = Thread.currentThread().getContextClassLoader()
                    .getResources(DEFAULT_CXF_EXT_CFG_FILE);
                if (resources.hasMoreElements()) {
                    cfgFileLocations.add(DEFAULT_CXF_EXT_CFG_FILE);
                }      
            } catch (IOException ex) {
                // ignore  
            }  
        }
        
        
        
        String[] locations = new String[cfgFileLocations.size()];
        locations = cfgFileLocations.toArray(locations);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creating application context with cfg files: " + locations);
        }
        
        ApplicationContext ac = null;
        try {      
            ac = new ClassPathXmlApplicationContext(locations);           
        } catch (BeansException ex) {
            LogUtils.log(LOG, Level.WARNING, "APP_CONTEXT_CREATION_FAILED_MSG", ex, (Object[])null);
        }
        
        Bus bus = (Bus)ac.getBean(DEFAULT_BUS_ID);
        
        Configurer configurer = new ConfigurerImpl(ac);
        bus.setExtension(configurer, Configurer.class);

        return bus;
    }
    
}
