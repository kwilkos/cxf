package org.objectweb.celtix.bus.configuration;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;

public class CeltixConfigurationBuilder extends ConfigurationBuilderImpl {

    public CeltixConfigurationBuilder() {

        addModel("config-metadata/bus-config.xml");
        addModel("config-metadata/endpoint-config.xml");
        addModel("config-metadata/http-client-config.xml");
        addModel("config-metadata/http-listener-config.xml");
        addModel("config-metadata/http-server-config.xml");
        addModel("config-metadata/port-config.xml");
        addModel("config-metadata/jms-client-config.xml");
        addModel("config-metadata/jms-server-config.xml");
        addModel("config-metadata/rm-config.xml");
        addModel("config-metadata/wsa-config.xml");
        addModel("config-metadata/instrumentation-config.xml");
    }

    public Configuration buildConfiguration(String namespaceUri, String id, Configuration parent) {
        ConfigurationMetadata model = getModel(namespaceUri);
        if (null == model) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
        }
        /*
        if (parent != null && !isValidChildConfiguration(model, parent)) {
            throw new ConfigurationException(new Message("INVALID_CHILD_CONFIGURATION",
                                                         BUNDLE, namespaceUri,
                                                         parent.getModel().getNamespaceURI()));
        }
        */
        if (parent == null && !isValidTopConfiguration(model, parent)) {
            throw new ConfigurationException(new Message("INVALID_TOP_CONFIGURATION",
                                                         BUNDLE, namespaceUri));
        }

        Configuration c = new CeltixConfigurationImpl(model, id, parent);
        if (null == parent) {
            Map<String, Configuration> instances = configurations.get(namespaceUri);
            if (null == instances) {
                instances = new HashMap<String, Configuration>();
                configurations.put(namespaceUri, instances);
            }
            instances.put(id, c);
        }
        return c;
    }
}
