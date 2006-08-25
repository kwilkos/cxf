package org.objectweb.celtix.tools.common;

import java.util.*;

public class ProcessorEnvironment {

    private Map<String, Object> paramMap;

    public void setParameters(Map<String, Object> map) {
        this.paramMap = map;
    }
    
    public boolean containsKey(String key) {
        return (paramMap == null) ? false : paramMap.containsKey(key);
    }

    public Object get(String key) {
        return (paramMap == null) ? null : paramMap.get(key);
    }

    public void put(String key, Object value) {
        if (paramMap == null) {
            paramMap = new HashMap<String, Object>();
        }
        paramMap.put(key, value);
    }

    public void remove(String key) {
        if (paramMap == null) {
            return;
        }
        paramMap.remove(key);
    }

    public boolean optionSet(String key) {
        return (get(key) == null) ? false : true;
    }

    public boolean isVerbose() {
        if (get(ToolConstants.CFG_VERBOSE) == null) {
            return false;
        } else {
            return (Boolean) get(ToolConstants.CFG_VERBOSE);
        }
    }
}
