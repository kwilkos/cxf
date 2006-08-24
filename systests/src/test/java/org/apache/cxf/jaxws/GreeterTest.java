package org.apache.cxf.jaxws;

import java.net.URL;

import org.w3c.dom.Node;

import org.apache.cxf.Bus;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.bindings.soap2.SoapBindingFactory;
import org.apache.cxf.bindings.soap2.SoapDestinationFactory;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.messaging.ConduitInitiatorManager;
import org.apache.cxf.messaging.DestinationFactoryManager;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.SimpleMethodInvoker;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transports.local.LocalTransportFactory;
import org.apache.hello_world_soap_http.GreeterImpl;

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
        assertEquals("http://apache.org/hello_world_soap_http", service.getName().getNamespaceURI());

        bean.activateEndpoints();

        Node response = invoke("http://localhost:9000/SoapContext/SoapPort",
                           LocalTransportFactory.TRANSPORT_ID,
                           "GreeterMessage.xml");
        
        assertEquals(1, greeter.getInvocationCount());
        
        assertNotNull(response);
        
        addNamespace("h", "http://apache.org/hello_world_soap_http/types");
        
        assertValid("/s:Envelope/s:Body", response);
        assertValid("//h:sayHiResponse", response);
    }
}
