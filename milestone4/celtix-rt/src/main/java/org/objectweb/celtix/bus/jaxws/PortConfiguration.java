package org.objectweb.celtix.bus.jaxws;

import javax.wsdl.Port;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class PortConfiguration extends AbstractConfigurationImpl {

    PortConfiguration(Configuration serviceConfiguration, String portName, Bus bus,
                      Port port) {
        super("config-metadata/port-config.xml", portName,
              serviceConfiguration);


        // register additional provider to obtain configuration from wsdl

        if (null != port) {
            this.getProviders().add(new WsdlPortProvider(port));

        }
    }
}
