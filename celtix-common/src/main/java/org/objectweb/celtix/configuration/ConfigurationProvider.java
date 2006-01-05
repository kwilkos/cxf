package org.objectweb.celtix.configuration;


public interface ConfigurationProvider {
    
    void init(Configuration configuration);

    Object getObject(String name);
}
