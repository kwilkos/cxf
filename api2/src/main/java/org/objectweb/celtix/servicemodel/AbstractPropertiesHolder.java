package org.objectweb.celtix.servicemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.wsdl.extensions.ExtensibilityElement;

public abstract class AbstractPropertiesHolder {
    private AtomicReference<Map<String, Object>> propertyMap = new AtomicReference<Map<String, Object>>();
    private AtomicReference<Object[]> extensors = new AtomicReference<Object[]>();
    
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
            propertyMap.compareAndSet(null, new ConcurrentHashMap<String, Object>(4));
        }
        if (v == null) {
            propertyMap.get().remove(name);
        } else {
            propertyMap.get().put(name, v);
        }
    }
    
    
    public void addExtensor(Object el) {
        Object exts[] = extensors.get();
        Object exts2[];
        if (exts == null) {
            exts2 = new Object[1];
        } else {
            exts2 = new Object[exts.length + 1];
        }
        exts2[exts2.length - 1] = el;
        if (!extensors.compareAndSet(exts, exts2)) {
            //keep trying
            addExtensor(el);
        }
    }

    public <T> T getExtensor(Class<T> cls) {
        Object exts[] = extensors.get();
        if (exts == null) {
            return null;
        }
        for (int x = 0; x < exts.length; x++) {
            if (cls.isInstance(exts[x])) {
                return cls.cast(exts[x]);
            }
        }
        return null;
    }
    public <T> List<T> getExtensors(Class<T> cls) {
        Object exts[] = extensors.get();
        if (exts == null) {
            return null;
        }
        List<T> list = new ArrayList<T>(exts.length);
        for (int x = 0; x < exts.length; x++) {
            if (cls.isInstance(exts[x])) {
                list.add(cls.cast(exts[x]));
            }
        }
        return list;
    }

    public List<ExtensibilityElement> getWSDL11Extensors() {
        if (extensors.get() == null) {
            return null;
        }
        List<ExtensibilityElement> list
            = new ArrayList<ExtensibilityElement>(extensors.get().length);
        for (Object obj : extensors.get()) {
            if (obj instanceof ExtensibilityElement) {
                list.add((ExtensibilityElement)obj);
            }
        }
        return Collections.unmodifiableList(list);
    }
    /*
    //eventually for wsdl20 support
    public List<org.apache.woden.wsdl20.extensions.ExtensionElement> getWSDL12Extensors() {
        if (extensors.get() == null) {
            return null;
        }
        List<ExtensionElement> list
            = new ArrayList<ExtensionElement>(extensors.get().length);
        for (Object obj : extensors.get()) {
            if (obj instanceof ExtensionElement) {
                list.add((ExtensionElement)obj);
            }
        }
        return Collections.unmodifiableList(list);
    }
    */

}
