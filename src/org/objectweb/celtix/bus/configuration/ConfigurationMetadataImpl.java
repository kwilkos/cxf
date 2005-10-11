package org.objectweb.celtix.bus.configuration;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;

public class ConfigurationMetadataImpl implements ConfigurationMetadata {

    private Map<String, ConfigurationItemMetadata> definitions;
    private Map<QName, QName> types;
    
    protected ConfigurationMetadataImpl() {
        definitions = new HashMap<String, ConfigurationItemMetadata>();
        types = new HashMap<QName, QName>();
    }
    
    protected void addItem(ConfigurationItemMetadata item) {
        definitions.put(item.getName(), item);
    }
    
    public ConfigurationItemMetadata getDefinition(String name) {      
        return definitions.get(name);
    }

    public Collection<ConfigurationItemMetadata> getDefinitions() {
        return definitions.values();
    }
    
    public Collection<QName> getTypes() {
        return types.values();
    }
    
    public QName getType(QName elementName) {
        return types.get(elementName);
    }
    
    public QName addType(QName elementName, QName type) {
        return types.put(elementName, type);
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("parsing metadata " + args[0]);
        URL url = new URL(args[0]);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        builder.build(url);        
    }   
}
