package org.apache.cxf.bus;

import java.util.Map;

import org.apache.cxf.configuration.CompoundName;
import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.configuration.ConfigurationBuilder;

public class BusConfigurationHelper {

    public static final String BUS_ID_PROPERTY = "org.apache.cxf.bus.id";
    public static final String BUS_CONFIGURATION_URI = "http://cxf.apache.org/configuration/bus";
    public static final String DEFAULT_BUS_ID = "cxf";

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
