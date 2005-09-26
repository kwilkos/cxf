package org.objectweb.celtix.configuration;

import java.util.Collection;

public interface ConfigurationMetadata {
    ConfigurationItemMetadata getDefinition(String name);
    Collection<ConfigurationItemMetadata> getDefinitions(); 
}
