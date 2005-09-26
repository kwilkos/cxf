package org.objectweb.celtix.bus.configuration;

import javax.xml.namespace.QName;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;

public class ConfigurationItemMetadataImpl implements ConfigurationItemMetadata {
    
    private String name;
    private LifecyclePolicy lifecyclePolicy = LifecyclePolicy.STATIC;
    private QName type;
    private String description;
    private Object defaultValue;
    
    public String getName() {
        return name;
    }
    
    public QName getType() {
        return type;
    }
    
    
    
    public LifecyclePolicy getLifecyclePolicy() {
        return lifecyclePolicy;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    protected void setName(String n) {
        name = n;
    }
    
    protected void setType(QName t) {
        type = t;
    }
    
    public void setLifecyclePolicy(LifecyclePolicy policy) {
        lifecyclePolicy = policy;
    }
    
    void setDescription(String d) {
        description = d;
    }
    
    
    public void setDefaultValue(Object v) {
        defaultValue = v;
    }
}
