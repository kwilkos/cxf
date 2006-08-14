package org.objectweb.celtix.jaxws;

import java.net.URL;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.soap2.SoapBindingFactory;
import org.objectweb.celtix.bindings.soap2.SoapDestinationFactory;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.transports.http.HTTPTransportFactory;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

public class GreeterTest extends TestCase {
    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(AnnotatedGreeterImpl.class);

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        Bus bus = createBus();
        bean.setBus(bus);

        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", service.getName().getNamespaceURI());

        // bean.activateEndpoints();
    }

    Bus createBus() {
        CeltixBus bus = new CeltixBus();

        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);

        HTTPTransportFactory factory = new HTTPTransportFactory();
        factory.setBus(bus);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", factory);

        return bus;
    }
}
