package org.objectweb.celtix.servicemodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractPropertiesHolder {
    private AtomicReference<Map<String, Object>> propertyMap = new AtomicReference<Map<String, Object>>();
    
    public Object getProperty(String name) {
        if (null == propertyMap.get()) {
            return null;
        }
        return propertyMap.get().get(name);
    }
    
    public <T> T getProperty(String name, Class<T> cls) {
        return cls.cast(getProperty(name));
    }
    
    public void setProperty(String name, Object v) {
        if (null == propertyMap.get()) {
            propertyMap.compareAndSet(null, new ConcurrentHashMap<String, Object>(8));
        }
        if (v == null) {
            propertyMap.get().remove(name);
        } else {
            propertyMap.get().put(name, v);
        }
    }

}
