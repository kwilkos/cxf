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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cxf.helpers.CastUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBeanMap<V> implements ApplicationContextAware, InitializingBean, Map<String, V> {
    private ApplicationContext context;
    private Class<?> type;
    private String idsProperty;
    private Map<String, String> idToBeanName = new ConcurrentHashMap<String, String>();
    private Map<String, V> putStore = new ConcurrentHashMap<String, V>();

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

    public void afterPropertiesSet() throws Exception {
        processBeans(context);
    }

    @SuppressWarnings("unchecked")
    private void processBeans(ApplicationContext beanFactory) {
        if (beanFactory == null) {
            return;
        }

        String[] beanNames = beanFactory.getBeanDefinitionNames();

        ConfigurableApplicationContext ctxt = (ConfigurableApplicationContext)beanFactory;

        // Take any bean name or alias that has a web service annotation
        for (int i = 0; i < beanNames.length; i++) {
            BeanDefinition def = ctxt.getBeanFactory().getBeanDefinition(beanNames[i]);

            if (!def.isSingleton() || def.isAbstract()) {
                continue;
            }

            if (!type.isAssignableFrom(ctxt.getType(beanNames[i]))) {
                continue;
            }

            try {
                Collection<String> ids = getIds(ctxt.getBean(beanNames[i]));
                if (ids == null) {
                    continue;
                }
                
                for (String id : ids) {
                    idToBeanName.put(id, beanNames[i]);
                }
            } catch (BeanIsAbstractException e) {
                // The bean is abstract, we won't be doing anything with it.
                continue;
            }
        }

        processBeans(ctxt.getParent());
    }

    private Collection<String> getIds(Object bean) {
        try {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), idsProperty);
            Method method = pd.getReadMethod();
            Collection<String> c = CastUtils.cast((Collection<?>)method.invoke(bean, new Object[0]));

            return c;
        } catch (IllegalArgumentException e) {
            throw new BeanInitializationException("Could not retrieve ids.", e);
        } catch (IllegalAccessException e) {
            throw new BeanInitializationException("Could not access id getter.", e);
        } catch (InvocationTargetException e) {
            throw new BeanInitializationException("Could not invoke id getter.", e);
        } catch (SecurityException e) {
            throw new BeanInitializationException("Could not invoke id getter.", e);
        }
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getIdsProperty() {
        return idsProperty;
    }

    public void setIdsProperty(String idsProperty) {
        this.idsProperty = idsProperty;
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return idToBeanName.containsKey(key) || putStore.containsKey(key);
    }

    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public Set<java.util.Map.Entry<String, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        String name = idToBeanName.get(key);
        if (name != null) {
            return (V)context.getBean(name);
        } else {
            return putStore.get(key);
        }
    }

    public boolean isEmpty() {
        return idToBeanName.isEmpty() && putStore.isEmpty();
    }

    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(idToBeanName.keySet());
        keys.addAll(putStore.keySet());
        return keys;
    }

    public V put(String key, V value) {
        // Make sure we don't take the key from Spring any more
        idToBeanName.remove(key);
        return putStore.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends V> m) {
        putStore.putAll(m);
    }

    public V remove(Object key) {
        V v = get(key);
        if (v != null) {
            idToBeanName.remove(key);
        } else {
            v = putStore.get(key);
        }

        return v;
    }

    public int size() {
        return idToBeanName.size() + putStore.size();
    }

    public Collection<V> values() {
        List<V> values = new ArrayList<V>();
        values.addAll(putStore.values());
        for (String id : idToBeanName.keySet()) {
            values.add(get(id));
        }
        return values;
    }
}
