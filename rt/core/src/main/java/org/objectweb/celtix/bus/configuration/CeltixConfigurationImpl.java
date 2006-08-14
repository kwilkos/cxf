package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationImpl;

public class CeltixConfigurationImpl extends ConfigurationImpl {

    public CeltixConfigurationImpl(ConfigurationMetadata model, CompoundName id) {
        super(model, id);
    }

    public void propertyModified(String name) {
        Bus.getCurrent().sendEvent(new ConfigurationEvent(name, ConfigurationEvent.RECONFIGURED));
    }

}
