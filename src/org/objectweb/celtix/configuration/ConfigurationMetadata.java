package org.objectweb.celtix.configuration;

import java.util.Collection;

/**
 * Container for a component's runtime configuration metata.
 *
 */
public interface ConfigurationMetadata {
    
    /**
     * Gets the metadata for the specified configuration item name.
     * 
     * @param name the name of the configuration item.
     * @return the item's configuration metadata or null if no such item is 
     * defined.
     */
    ConfigurationItemMetadata getDefinition(String name);
    
    /**
     * Gets all configuration metadata items in this container.
     * 
     * @return the collection of configuration metadata items.
     */
    Collection<ConfigurationItemMetadata> getDefinitions(); 
}
