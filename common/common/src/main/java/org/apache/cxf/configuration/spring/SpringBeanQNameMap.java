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

import javax.xml.namespace.QName;

import org.apache.cxf.helpers.CastUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.Mergeable;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBeanQNameMap<V> implements ApplicationContextAware, InitializingBean, Map<QName, V> {
    private ApplicationContext context;
    private Class<?> type;
    private String idsProperty;
    private Map<QName, String> idToBeanName = new ConcurrentHashMap<QName, String>();
    private Map<QName, V> putStore = new ConcurrentHashMap<QName, V>();

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

    public void afterPropertiesSet() throws Exception {
        processBeans(context);
    }

    private void processBeans(ApplicationContext beanFactory) {
        if (beanFactory == null) {
            return;
        }

        String[] beanNames = beanFactory.getBeanNamesForType(type);

        ConfigurableApplicationContext ctxt = (ConfigurableApplicationContext)beanFactory;

        // Take any bean name or alias that has a web service annotation
        for (int i = 0; i < beanNames.length; i++) {

            BeanDefinition def = ctxt.getBeanFactory().getBeanDefinition(beanNames[i]);

            if (!def.isSingleton() || def.isAbstract()) {
                continue;
            }

            try {
                Collection<?> ids = null;
                PropertyValue pv = def.getPropertyValues().getPropertyValue(idsProperty);
                
                if (pv != null) {
                    Object value = pv.getValue();
                    if (!(value instanceof Collection)) {
                        throw new RuntimeException("The property " + idsProperty + " must be a collection!");
                    }

                    if (value instanceof Mergeable) {
                        if (!((Mergeable)value).isMergeEnabled()) {
                            ids = (Collection<?>)value;
                        }
                    } else {
                        ids = (Collection<?>)value;
                    }
                } 
              
                // if values are not legal keys (for lazy-init bean definitions id values may be
                // BeanDefinitionHolders), load the bean and get its id values instead
                // for BeanReference type values, simply resolve reference
                //  
                if (null != ids) {
                    Collection<Object> checked = new ArrayList<Object>(ids.size());
                    for (Object id : ids) {
                        if (id instanceof QName) {
                            checked.add(id);
                        } else if (id instanceof BeanReference) {
                            BeanReference br = (BeanReference)id;
                            Object refId = context.getBean(br.getBeanName());
                            checked.add(refId);
                        } else {
                            break;
                        }
                    }
                    if (checked.size() < ids.size()) {
                        ids = null;
                    } else {
                        ids = checked;
                    }
                } 
                if (ids == null) {
                    ids = getIds(ctxt.getBean(beanNames[i]));
                    if (ids == null) {
                        continue;
                    }
                }
                
                for (Object id : ids) {
                    QName key = (QName)id;
                    idToBeanName.put(key, beanNames[i]);
                }
            } catch (BeanIsAbstractException e) {
                // The bean is abstract, we won't be doing anything with it.
                continue;
            }
        }

        processBeans(ctxt.getParent());
    }

    private Collection<QName> getIds(Object bean) {
        try {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), idsProperty);
            Method method = pd.getReadMethod();
            Collection<QName> c = CastUtils.cast((Collection<?>)method.invoke(bean, new Object[0]),
                QName.class);

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

    public Set<java.util.Map.Entry<QName, V>> entrySet() {
        Set<Map.Entry<QName, V>> entries = new HashSet<Map.Entry<QName, V>>();
        for (QName k : keySet()) {
            entries.add(new Entry<V>(this, k));
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        String name = idToBeanName.get(key);
        if (name != null) {
            return (V)(context.getBean(name));
        } else {
            return putStore.get(key);
        }
    }

    public boolean isEmpty() {
        return idToBeanName.isEmpty() && putStore.isEmpty();
    }

    public Set<QName> keySet() {
        Set<QName> keys = new HashSet<QName>();
        keys.addAll(idToBeanName.keySet());
        keys.addAll(putStore.keySet());
        return keys;
    }

    public V put(QName key, V value) {
        // Make sure we don't take the key from Spring any more
        idToBeanName.remove(key);
        return putStore.put(key, value);
    }

    public void putAll(Map<? extends QName, ? extends V> m) {
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
        for (QName id : idToBeanName.keySet()) {
            values.add(get(id));
        }
        return values;
    }
    
    public static class Entry<V> implements Map.Entry<QName, V> {
        private SpringBeanQNameMap<V> map;
        private QName key;

        public Entry(SpringBeanQNameMap<V> map, QName key) {
            this.map = map;
            this.key = key;
        }
        
        public QName getKey() {
            return key;
        }

        public V getValue() {
            return map.get(key);
        }

        public V setValue(V value) {
            return map.put(key, value);
        }
    }
}
