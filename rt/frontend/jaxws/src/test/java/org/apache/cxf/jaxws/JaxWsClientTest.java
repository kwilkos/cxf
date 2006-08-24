package org.apache.cxf.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.bindings.soap.SoapBindingFactory;
import org.apache.cxf.bindings.soap.SoapDestinationFactory;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.jaxws.support.JaxwsEndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.messaging.Conduit;
import org.apache.cxf.messaging.ConduitInitiatorManager;
import org.apache.cxf.messaging.Destination;
import org.apache.cxf.messaging.DestinationFactoryManager;
import org.apache.cxf.messaging.MessageObserver;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.SimpleMethodInvoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transports.local.LocalTransportFactory;
import org.apache.hello_world_soap_http.GreeterImpl;
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

        String namespace = "http://apache.org/hello_world_soap_http";
        EndpointInfo ei = service.getServiceInfo().getEndpoint(new QName(namespace, "SoapPort"));
        JaxwsEndpointImpl endpoint = new JaxwsEndpointImpl(bus, service, ei);
        
        ClientImpl client = new ClientImpl(bus, endpoint);
        
        BindingOperationInfo bop = ei.getBinding().getOperation(new QName(namespace, "sayHi"));
        assertNotNull(bop);
        bop = bop.getUnwrappedOperation();
        assertNotNull(bop);
        Object ret[] = client.invoke(bop, new Object[0], null);
        assertNotNull(ret);
        assertEquals("Wrong number of return objects", 1, ret.length);
        //right now, no message string is returned by the echoer
        assertNull(ret[0]);
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
                
                copy(in, out, 2045);

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
