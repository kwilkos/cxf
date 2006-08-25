package org.objectweb.celtix.bus.bindings.xml;

import javax.wsdl.Port;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class XMLBindingTransportTest extends TestCase {

    private static final String PORT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/port-config";

    TestUtils utils;
    Bus bus;
    
    public void setUp() throws Exception {
        utils = new TestUtils();
        bus = Bus.init();
    }
    
    public void testGetAddress() throws Exception {
        EndpointReferenceType ref = utils.getEndpointReference();
        assertNotNull(ref);
        Configuration portConfiguration =
            createPortConfiguration(new QName("http://objectweb.org/xml_http_bare",
                                                                            "XMLPort"),
                                                                  ref);
        assertNotNull(portConfiguration);
        String address = portConfiguration.getString("address");
        assertNotNull(address);
        assertEquals("http://localhost:9090/XMLService/XMLPort", address);
    }

    private Configuration createPortConfiguration(QName portName,
                                                  EndpointReferenceType ref) throws Exception {
        Configuration portCfg = null;
        String id = portName.getLocalPart();
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        portCfg = cb.buildConfiguration(PORT_CONFIGURATION_URI, id, bus.getConfiguration());
        
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        assertNotNull(port);
        portCfg.getProviders().add(new WsdlPortProvider(port));
        return portCfg;
    }
}

    
