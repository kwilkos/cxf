package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationImpl;

public class CeltixConfigurationImpl extends ConfigurationImpl {

    public CeltixConfigurationImpl(ConfigurationMetadata m, String instanceId, Configuration parent) {
        super(m, instanceId, parent);
    }

    public void propertyModified(String name) {
        Bus.getCurrent().sendEvent(new ConfigurationEvent(name, ConfigurationEvent.RECONFIGURED));
    }

}
