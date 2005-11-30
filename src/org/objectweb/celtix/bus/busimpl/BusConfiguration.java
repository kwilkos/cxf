package org.objectweb.celtix.bus.busimpl;

import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;
import org.objectweb.celtix.configuration.CommandLineOption;

public class BusConfiguration extends AbstractConfigurationImpl {
    
    public static final String BUS_ID_PROPERTY = "org.objectweb.celtix.BusId";
    private static final CommandLineOption BUS_ID_OPT;    
    private static final String DEFAULT_BUS_ID = "celtix";
    
    private final String id;

    static {
        BUS_ID_OPT = new CommandLineOption("-BUSid");
    }
    
    
    BusConfiguration(String[] args, Map<String, Object> properties) {
        super(BusConfiguration.class, "config-metadata/bus-config.xml", "celtix");  
        
        // get the bus id from the command line arguments        
        id = getBusId(args, properties);       
    }

    @Override
    public Object getId() {
        return id;
    } 
    
    private String getBusId(String[] args, Map<String, Object> properties) {

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
