package org.objectweb.celtix.bus.busimpl;

import java.util.Map;

import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;

public class BusConfiguration extends AbstractConfigurationImpl {
   
    BusConfiguration(String[] args, Map<String, Object> properties) {
        super(BusConfiguration.class.getResource("configuration.xml"));
        
        //ignore arguments  ...
    }  
    
    public Object getId() {
        // should really be the bus id
        return super.getId();
    }
}
