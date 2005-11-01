package org.objectweb.celtix.bus.handlers;


import java.util.HashMap;
import java.util.Map;

public class HandlerConfig {

    private String className;
    private String name;
    private Map<String, String> initParams = new HashMap<String, String>();

    public HandlerConfig() {
    }

    
    public final String getClassName() {
        return this.className;
    }

    public final void setClassName(final String argClassName) {
        this.className = argClassName;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(final String argName) {
        this.name = argName;
    }

    public void addInitParam(String n, String v) { 
        initParams.put(n, v); 
    } 

    public Map<String, String> getInitParams() { 
        return initParams;
    } 

    void addInitParam(Param p) { 
        addInitParam(p.getName(), p.getValue()); 
    }

    
    public String toString() {
        return "[" + name + ":" + className + ":" + initParams + "]";
    }

    static class Param {
        private String name; 
        private String value; 

        Param(String n, String v) { 
            name = n; 
            value = v;
        } 

        public final String getName() {
            return this.name;
        }

        public final void setName(final String argName) {
            this.name = argName;
        }

        public final String getValue() {
            return this.value;
        }

        public final void setValue(final String argValue) {
            this.value = argValue;
        }

        public String toString() {
            return "[" + name + ":" + value + "]";
        }
    }
}
