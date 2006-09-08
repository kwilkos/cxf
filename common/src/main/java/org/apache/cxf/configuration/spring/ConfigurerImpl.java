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

package org.apache.cxf.configuration.spring;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.Configurer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConfigurerImpl extends BeanConfigurerSupport implements Configurer {
    
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurerImpl.class);
    private static final String DEFAULT_USER_CFG_FILE = "cxf.xml";
    
    public ConfigurerImpl() {
        this(DEFAULT_USER_CFG_FILE);
    }
    
    public ConfigurerImpl(String cfgFile) {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(cfgFile);
        setApplicationContext(appContext);
    }
    
    public ConfigurerImpl(ApplicationContext appContext) {
        setApplicationContext(appContext);
    }
    
    public void configureBean(Configurable beanInstance) {

        final String beanName = beanInstance.getBeanName();
        
        setBeanWiringInfoResolver(new BeanWiringInfoResolver() {
            public BeanWiringInfo resolveWiringInfo(Object instance) {
                if (null != beanName && !"".equals(beanName)) {
                    return new BeanWiringInfo(beanName);
                }
                return null;
            }
        });
        
        try {
            super.configureBean(beanInstance);
            LOG.fine("Successfully performed injection.");
        } catch (NoSuchBeanDefinitionException ex) {
            // users often wonder why the settings in their configuration files seem
            // to have no effect - the most common cause is that they have been using
            // incorrect bean ids
            LOG.log(Level.INFO, "NO_MATCHING_BEAN", beanName);
        }
    }
    
    private void setApplicationContext(ApplicationContext appContext) {
        setBeanFactory(appContext.getAutowireCapableBeanFactory());
    }
}
