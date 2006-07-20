package org.objectweb.celtix.bus;

import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;

public class BusConfigurationBuilder  {
    
    public static final String BUS_ID_PROPERTY = "org.objectweb.celtix.bus.id";
    public static final String BUS_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/bus-config";    
    private static final String DEFAULT_BUS_ID = "celtix";
   
    
    Configuration build(Map<String, Object> properties) {
        String id = getBusId(properties);
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder(null);
        Configuration c = builder.getConfiguration(BUS_CONFIGURATION_URI, id);
        if (null == c) {
            c = builder.buildConfiguration(BUS_CONFIGURATION_URI, id);
        }
        return c;  
    }

    private static String getBusId(Map<String, Object> properties) {

        String busId = null;

        // first check properties
        busId = (String)properties.get(BUS_ID_PROPERTY);
        if (null != busId && !"".equals(busId)) {
            return busId;
        }

        // next check system properties
        busId = System.getProperty(Bus.BUS_CLASS_PROPERTY);
        if (null != busId && !"".equals(busId)) {
            return busId;
        }

        // otherwise use default  
        return DEFAULT_BUS_ID;
    } 
}
