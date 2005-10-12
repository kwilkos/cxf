package org.objectweb.celtix.bus.configuration;

import javax.xml.namespace.QName;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;

public class ConfigurationItemMetadataImpl implements ConfigurationItemMetadata {
    
    private String name;
    private LifecyclePolicy lifecyclePolicy = LifecyclePolicy.STATIC;
    private QName type;
    private Object defaultValue;
    private String packageName;
    
    public String getName() {
        return name;
    }
    
    public QName getType() {
        return type;
    }
        
    public LifecyclePolicy getLifecyclePolicy() {
        return lifecyclePolicy;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public String getTypePackageName() {
        return packageName;
    }
    
    protected void setName(String n) {
        name = n;
    }
    
    protected void setType(QName t) {
        type = t;
    }
    
    protected void setLifecyclePolicy(LifecyclePolicy policy) {
        lifecyclePolicy = policy;
    }
        
    protected void setDefaultValue(Object v) {
        defaultValue = v;
    }
    
    protected void setTypePackageName(String p) {
        packageName = p;
    }
}
