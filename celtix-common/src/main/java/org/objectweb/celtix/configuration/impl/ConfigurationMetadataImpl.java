package org.objectweb.celtix.configuration.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class ConfigurationMetadataImpl implements ConfigurationMetadata {

    private final Map<String, ConfigurationItemMetadata> definitions;
    private String namespaceURI;
    
    public ConfigurationMetadataImpl() {
        definitions = new HashMap<String, ConfigurationItemMetadata>();
    }
    
    protected void addItem(ConfigurationItemMetadata item) {
        definitions.put(item.getName(), item);
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }

    public ConfigurationItemMetadata getDefinition(String name) {      
        return definitions.get(name);
    }

    public Collection<ConfigurationItemMetadata> getDefinitions() {
        return definitions.values();
    }
    
    protected void setNamespaceURI(String uri) {
        namespaceURI = uri;
    }
}
