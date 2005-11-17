package org.objectweb.celtix.bus.jaxws;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class PortConfiguration extends AbstractConfigurationImpl {

    PortConfiguration(Configuration serviceConfiguration, String portName, Bus bus,
                      EndpointReferenceType ref) {
        super(PortConfiguration.class, "config-metadata/port-config.xml", portName,
              serviceConfiguration);
        Port port = null;
        try  {
            port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        } catch (WSDLException ex) {
            // TODO:
        }

        // register additional provider to obtain configuration from wsdl

        if (null != port) {
            this.getProviders().add(new WsdlPortProvider(port));

        }
    }
}
