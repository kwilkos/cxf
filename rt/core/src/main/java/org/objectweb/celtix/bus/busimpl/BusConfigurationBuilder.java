package org.objectweb.celtix.bus.busimpl;

import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.CommandLineOption;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;

public class BusConfigurationBuilder  {
    
    public static final String BUS_ID_PROPERTY = "org.objectweb.celtix.BusId";
    public static final String BUS_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/bus-config";
    private static final CommandLineOption BUS_ID_OPT;    
    private static final String DEFAULT_BUS_ID = "celtix";
    

    static {
        BUS_ID_OPT = new CommandLineOption("-BUSid");
    }
    
    Configuration build(ConfigurationBuilder builder, String[] args, Map<String, Object> properties) {
        String id = getBusId(args, properties);
        return builder.getConfiguration(BUS_CONFIGURATION_URI, new CompoundName(id));
    }

    private static String getBusId(String[] args, Map<String, Object> properties) {

        String busId = null;

        // first check command line arguments
        BUS_ID_OPT.initialize(args);
        busId = (String)BUS_ID_OPT.getValue();
        if (null != busId && !"".equals(busId)) {
            return busId;
        }

        // next check properties
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
