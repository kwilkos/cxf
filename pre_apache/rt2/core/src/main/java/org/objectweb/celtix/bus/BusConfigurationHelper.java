package org.objectweb.celtix.bus;

import java.util.Map;

import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;

public class BusConfigurationHelper {

    public static final String BUS_ID_PROPERTY = "org.objectweb.celtix.bus.id";
    public static final String BUS_CONFIGURATION_URI = "http://celtix.objectweb.org/configuration/bus";
    public static final String DEFAULT_BUS_ID = "celtix";

    Configuration getConfiguration(ConfigurationBuilder builder, String id) {
        return builder.getConfiguration(BUS_CONFIGURATION_URI, new CompoundName(id));
    }

    String getBusId(Map<String, Object> properties) {

        String busId = null;

        // first check properties
        if (null != properties) {
            busId = (String)properties.get(BUS_ID_PROPERTY);
            if (null != busId && !"".equals(busId)) {
                return busId;
            }
        }

        // next check system properties
        busId = System.getProperty(BUS_ID_PROPERTY);
        if (null != busId && !"".equals(busId)) {
            return busId;
        }

        // otherwise use default
        return DEFAULT_BUS_ID;
    }
}
