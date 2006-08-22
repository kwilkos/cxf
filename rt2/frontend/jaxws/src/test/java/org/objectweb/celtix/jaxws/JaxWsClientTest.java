package org.objectweb.celtix.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.soap2.SoapBindingFactory;
import org.objectweb.celtix.bindings.soap2.SoapDestinationFactory;
import org.objectweb.celtix.endpoint.ClientImpl;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.jaxws.support.JaxwsEndpointImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.invoker.SimpleMethodInvoker;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.test.AbstractCXFTest;
import org.objectweb.celtix.transports.local.LocalTransportFactory;
import org.objectweb.hello_world_soap_http.GreeterImpl;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class JaxWsClientTest extends AbstractCXFTest {

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
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        AddressType a = new AddressType();
        a.setLocation("http://localhost:9000/SoapContext/SoapPort");
        ei.addExtensor(a);

        Destination d = localTransport.getDestination(ei);
        d.setMessageObserver(new EchoObserver());
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

        String namespace = "http://objectweb.org/hello_world_soap_http";
        EndpointInfo ei = service.getServiceInfo().getEndpoint(new QName(namespace, "SoapPort"));
        JaxwsEndpointImpl endpoint = new JaxwsEndpointImpl(bus, service, ei);
        
        ClientImpl client = new ClientImpl(bus, endpoint);
        
        BindingOperationInfo bop = ei.getBinding().getOperation(new QName(namespace, "sayHi"));
        client.invoke(bop, new Object[0], null);
    }

    static class EchoObserver implements MessageObserver {

        public void onMessage(Message message) {
            try {
                Conduit backChannel = message.getDestination().getBackChannel(message, null, null);

                backChannel.send(message);

                OutputStream out = message.getContent(OutputStream.class);
                assertNotNull(out);
                InputStream in = message.getContent(InputStream.class);
                assertNotNull(in);
                
                copy(in, out, 1024);

                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void copy(final InputStream input, final OutputStream output, final int bufferSize)
        throws IOException {
        try {
            final byte[] buffer = new byte[bufferSize];

            int n = input.read(buffer);
            while (-1 != n) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
