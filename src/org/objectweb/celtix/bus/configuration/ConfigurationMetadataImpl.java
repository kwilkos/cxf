package org.objectweb.celtix.bus.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class ConfigurationMetadataImpl implements ConfigurationMetadata {

    private Map<String, ConfigurationItemMetadata> definitions;
    private Map<String, TypeSchema> types;
    private String namespaceURI;
    
    protected ConfigurationMetadataImpl() {
        definitions = new HashMap<String, ConfigurationItemMetadata>();
        types = new HashMap<String, TypeSchema>();
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
    
    public Collection<TypeSchema> getTypeSchemas() {
        return types.values();
    }
    
    public TypeSchema getTypeSchema(QName typeName) {
        return types.get(typeName.getNamespaceURI());
    }
    
    public TypeSchema getTypeSchema(String namespace) {
        return types.get(namespace);
    }
    
    public void addTypeSchema(String namespace, TypeSchema ts) {
        types.put(namespace, ts);
    }
    
    protected void setNamespaceURI(String uri) {
        namespaceURI = uri;
    }
}
