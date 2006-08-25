package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class CeltixConfigurationImpl extends AbstractConfigurationImpl {

    public CeltixConfigurationImpl(ConfigurationMetadata m, String instanceId, Configuration parent) {
        super(m, instanceId, parent);
    }

    public void reconfigure(String name) {
        Bus.getCurrent().sendEvent(new ConfigurationEvent(name, ConfigurationEvent.RECONFIGURED));
    }

}
