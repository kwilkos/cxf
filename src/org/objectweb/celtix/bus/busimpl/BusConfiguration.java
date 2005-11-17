package org.objectweb.celtix.bus.busimpl;

import java.util.Map;

import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;

public class BusConfiguration extends AbstractConfigurationImpl {
   
    BusConfiguration(String[] args, Map<String, Object> properties) {
        super(BusConfiguration.class, "config-metadata/bus-config.xml", "celtix");      
        //ignore arguments  ...
    }  
}
