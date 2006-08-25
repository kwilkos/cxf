package org.objectweb.celtix.configuration.impl;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationProvider;

/**
 * Temporay class to accept changes to configuration. Should be obsoleted once other providers 
 * have implemented the setObject.
 */
public class InMemoryProvider implements ConfigurationProvider {

    private Map<String, Object> map;
    
    public InMemoryProvider() {
        map = new HashMap<String, Object>();
    }
    
    public void init(Configuration configuration) {
    }

    public Object getObject(String name) {
        return map.get(name);
    }

    public boolean setObject(String name, Object value) {
        map.put(name, value);
        return true;
    }
}
