package org.objectweb.celtix.configuration;

public interface ConfigurationItem {

    ConfigurationItemMetadata getDefinition();
    Object getValue();
    ConfigurationProvider[] getProviders();
    void setProviders(ConfigurationProvider[] providers);
}
