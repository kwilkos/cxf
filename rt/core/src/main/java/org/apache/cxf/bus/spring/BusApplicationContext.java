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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.JaxbClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BusApplicationContext extends JaxbClassPathXmlApplicationContext {
    
    private static final String DEFAULT_CXF_CFG_FILE = "META-INF/cxf/cxf.xml";
    private static final String DEFAULT_CXF_EXT_CFG_FILE = "classpath*:META-INF/cxf/cxf-extension-*.xml";
    private static final String CXF_PROPERTY_EDITORS_CFG_FILE = 
        "classpath*:META-INF/cxf/cxf-property-editors.xml";
    private static final Logger LOG = LogUtils.getL7dLogger(BusApplicationContext.class);
    
    private boolean includeDefaults;
    private String cfgFile;
    
    BusApplicationContext(String cf, boolean include) {
        this(cf, include, null);
    }

    BusApplicationContext(String cf, boolean include, ApplicationContext parent) {
        super((String[])null, parent);
        cfgFile = cf;
        includeDefaults = include;
    }
    
    @Override
    protected Resource[] getConfigResources() {
  
        List<Resource> resources = new ArrayList<Resource>();
       
        if (includeDefaults) {
            try {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread
                    .currentThread().getContextClassLoader());
                
                Collections.addAll(resources, resolver.getResources(DEFAULT_CXF_CFG_FILE));
                Collections.addAll(resources, resolver.getResources(DEFAULT_CXF_EXT_CFG_FILE));
                Collections.addAll(resources, resolver.getResources(CXF_PROPERTY_EDITORS_CFG_FILE));
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
        
        String cfgFileUrl = System.getProperty(Configurer.USER_CFG_FILE_PROPERTY_URL);
        if (null != cfgFileUrl) {
            try {
                UrlResource ur = new UrlResource(cfgFileUrl);
                if (ur.exists()) {
                    resources.add(ur);
                } else {
                    LOG.log(Level.INFO, 
                            new Message("USER_CFG_FILE_URL_NOT_FOUND_MSG", LOG, cfgFileUrl).toString());
                }            
            } catch (MalformedURLException e) {            
                LOG.log(Level.WARNING, 
                        new Message("USER_CFG_FILE_URL_ERROR_MSG", LOG, cfgFileUrl).toString());
            }
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
