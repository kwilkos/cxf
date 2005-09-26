package org.objectweb.celtix.configuration;

import javax.xml.namespace.QName;

public interface ConfigurationItemMetadata {
    
    public enum LifecyclePolicy {
        STATIC,
        PROCESS,
        BUS,
        DYNAMIC
    };
    
    String getName();
    QName getType();
    LifecyclePolicy getLifecyclePolicy();
    String getDescription();
    Object getDefaultValue();   
}
