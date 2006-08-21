package org.objectweb.celtix.jaxws;

import java.net.URL;

import org.w3c.dom.Node;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.soap2.SoapBindingFactory;
import org.objectweb.celtix.bindings.soap2.SoapDestinationFactory;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.invoker.SimpleMethodInvoker;
import org.objectweb.celtix.test.AbstractCXFTest;
import org.objectweb.celtix.transports.local.LocalTransportFactory;
import org.objectweb.hello_world_soap_http.GreeterImpl;

public class GreeterTest extends AbstractCXFTest {

    private Bus bus;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        bus = getBus();
        
        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
    }

    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(GreeterImpl.class);

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        bean.setBus(bus);
        
        GreeterImpl greeter = new GreeterImpl();
        SimpleMethodInvoker invoker = new SimpleMethodInvoker(greeter);
        bean.setInvoker(invoker);
        
        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", service.getName().getNamespaceURI());

        bean.activateEndpoints();

        Node response = invoke("http://localhost:9000/SoapContext/SoapPort",
                           LocalTransportFactory.TRANSPORT_ID,
                           "GreeterMessage.xml");
        
        assertEquals(1, greeter.getInvocationCount());
        
        assertNotNull(response);
        
        addNamespace("h", "http://objectweb.org/hello_world_soap_http/types");
        
        assertValid("/s:Envelope/s:Body", response);
        assertValid("//h:sayHiResponse", response);
    }
}
