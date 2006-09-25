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

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class BusApplicationContext extends AbstractXmlApplicationContext {
    
    private static final String DEFAULT_CXF_CFG_FILE = "META-INF/cxf/cxf.xml";
    private static final String DEFAULT_CXF_EXT_CFG_FILE = "META-INF/cxf/cxf-extension.xml";
    private static final Logger LOG = LogUtils.getL7dLogger(BusApplicationContext.class);
    
    private String cfgFile;
    private boolean includeDefaults;
    
    BusApplicationContext(String cf, boolean include) {
        cfgFile = cf;
        includeDefaults = include;
    }

    @Override
    protected Resource[] getConfigResources() {
  
        List<Resource> resources = new ArrayList<Resource>();
       
        if (includeDefaults) {
            try {
                Enumeration<URL> urls = Thread.currentThread().getContextClassLoader()
                    .getResources(DEFAULT_CXF_CFG_FILE);
                while (urls.hasMoreElements()) {
                    resources.add(new UrlResource(urls.nextElement()));
                }
                
                urls = Thread.currentThread().getContextClassLoader()
                    .getResources(DEFAULT_CXF_EXT_CFG_FILE);
                while (urls.hasMoreElements()) {
                    resources.add(new UrlResource(urls.nextElement()));
                }      
            } catch (IOException ex) {
                // ignore  
            }  
        }
        
        if (null == cfgFile) {
            cfgFile = System.getProperty(Configurer.USER_CFG_FILE_PROPERTY_NAME);
        }
        if (null == cfgFile) {
            cfgFile = Configurer.DEFAULT_USER_CFG_FILE;
        }
        ClassPathResource cpr = new ClassPathResource(cfgFile);
        if (cpr.exists()) {
            resources.add(cpr);
        } else {
            LOG.log(Level.INFO, new Message("USER_CFG_FILE_NOT_FOUND_MSG", LOG, cfgFile).toString());
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creating application context with resources: " + resources);
        }
        
        if (0 == resources.size()) {
            return null;
        }
        Resource[] res = new Resource[resources.size()];
        res = resources.toArray(res);
        return res;
    }
    
    

}
