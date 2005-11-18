package org.objectweb.celtix.tools.common;

import java.util.*;

public class ProcessorEnvironment {

    private Map<String, Object> paramMap;

    public void setParameters(Map<String, Object> map) {
        this.paramMap = map;
    }
    
    public boolean containsKey(String key) {
        return paramMap.containsKey(key);
    }

    public Object get(String key) {
        return paramMap.get(key);
    }

    public void put(String key, Object value) {
        paramMap.put(key, value);
    }

    public void remove(String key) {
        paramMap.remove(key);
    }
}
