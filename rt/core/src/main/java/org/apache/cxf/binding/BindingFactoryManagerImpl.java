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

package org.apache.cxf.binding;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.extension.ExtensionManager;

public final class BindingFactoryManagerImpl implements BindingFactoryManager {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(BindingFactoryManagerImpl.class);
    
    final Map<String, BindingFactory> bindingFactories;
    Properties factoryNamespaceMappings;
       
    ExtensionManager extensionManager;
    Bus bus;
     
    public BindingFactoryManagerImpl() throws BusException {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>();
    }
    
    @Resource
    public void setExtensionManager(ExtensionManager em) {
        extensionManager = em;
    }
    
    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    @PostConstruct
    public void register() {
        bus.setExtension(this, BindingFactoryManager.class);
    }
    
    BindingFactory loadBindingFactory(String className, String ...namespaceURIs) throws BusException {
        BindingFactory factory = null;
        try {
            Class<? extends BindingFactory> clazz = 
                Class.forName(className).asSubclass(BindingFactory.class);

            factory = clazz.newInstance();

            for (String namespace : namespaceURIs) {
                registerBindingFactory(namespace, factory);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new BusException(cnfe);
        } catch (InstantiationException ie) {
            throw new BusException(ie);
        } catch (IllegalAccessException iae) {
            throw new BusException(iae);
        }
        return factory;
    }
    
    public void registerBindingFactory(String name,
                                       BindingFactory factory) {
        bindingFactories.put(name, factory);
    }
    
    public void unregisterBindingFactory(String name) {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String namespace) throws BusException {
        BindingFactory factory = bindingFactories.get(namespace);
        if (null == factory) { 
            extensionManager.activateViaNS(namespace);            
            factory = bindingFactories.get(namespace);
        }
        if (null == factory) {
            throw new BusException(new Message("NO_BINDING_FACTORY_EXC", BUNDLE, namespace));
        }
        return factory;
    }
    
}
