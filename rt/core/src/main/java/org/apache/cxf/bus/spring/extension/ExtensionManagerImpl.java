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


package org.apache.cxf.bus.spring.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.extension.ExtensionManager;
import org.apache.cxf.helpers.CastUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class ExtensionManagerImpl implements ExtensionManager, BeanFactoryPostProcessor {

    private static final Logger LOG = LogUtils.getL7dLogger(ExtensionManagerImpl.class);
    private static final String ACTIVATION_NAMESPACES_PROPERTY_NAME = "activationNamespaces";
    
    private Map<String, Collection<String>> deferred;
    private ConfigurableListableBeanFactory factory;
    
    ExtensionManagerImpl() {
        deferred = new HashMap<String, Collection<String>>();
    }
    
    public void activateViaNS(String namespace) {
        Collection<String> beanNames = deferred.get(namespace);
        if (null != beanNames) {
            for (String n : beanNames) {
                try {
                    factory.getBean(n);
                } catch (BeansException ex) {
                    LogUtils.log(LOG, Level.WARNING, "EXTENSION_ACTIVATION_EXC", ex, namespace);
                    throw ex;
                }
            }
            beanNames = null;
            deferred.remove(namespace);
        }
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory f) throws BeansException {
        factory = f;
        String[] names = factory.getBeanDefinitionNames();
        for (String n : names) {
            BeanDefinition bd = factory.getBeanDefinition(n);
            if (!bd.isLazyInit()) {
                continue;
            }
            MutablePropertyValues mpvs = bd.getPropertyValues();
            if (null == mpvs) {
                continue;
            }
            PropertyValue pv = mpvs.getPropertyValue(ACTIVATION_NAMESPACES_PROPERTY_NAME);
            if (null == pv) {
                continue;
            }
            Collection<String> activationNamespaces = null;
            try {     
                Collection<?> values = (Collection<?>)pv.getValue();
                activationNamespaces = CastUtils.cast(values);
            } catch (ClassCastException ex) {
                ex.printStackTrace();
                continue;
            }
            for (String ns : activationNamespaces) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("registering bean " + n + " for deferred activation in namespace " + ns);
                }
                addDeferred(ns, n);
            }  
        }
        
    }
    
    private void addDeferred(String namespace, String beanName) {
        Collection<String> beanNames = deferred.get(namespace);
        if (null == beanNames) {
            beanNames = new ArrayList<String>();   
            deferred.put(namespace, beanNames);
        }
        beanNames.add(beanName);
    }
}
