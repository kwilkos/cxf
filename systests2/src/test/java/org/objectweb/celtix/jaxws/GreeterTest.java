package org.objectweb.celtix.jaxws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogManager;

import junit.framework.TestCase;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.soap2.SoapBindingFactory;
import org.objectweb.celtix.bindings.soap2.SoapDestinationFactory;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.transports.local.LocalTransportFactory;
//import org.objectweb.celtix.ws.addressing.AttributedURIType;
//import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

import org.xmlsoap.schemas.wsdl.http.AddressType;

public class GreeterTest extends TestCase {
    private static String basedirPath;
    
    private LocalTransportFactory localTransport;
    private Bus bus;


    @Override
    protected void setUp() throws Exception {
        bus = new CeltixBus();

        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);

        localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);

    }

    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(AnnotatedGreeterImpl.class);

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        bean.setBus(bus);

        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", service.getName().getNamespaceURI());

        bean.activateEndpoints();

        /*
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType uri = new AttributedURIType();
        uri.setValue("http://localhost:9000/SoapContext/SoapPort");
        epr.setAddress(uri);
        */

        EndpointInfo ei = new EndpointInfo(service.getServiceInfo(), "http://schemas.xmlsoap.org/soap/http");
        AddressType a = new AddressType();
        a.setLocation("http://localhost:9000/SoapContext/SoapPort");
        ei.addExtensor(a);

        Conduit conduit = localTransport.getConduit(ei);

        Message m = new MessageImpl();
        conduit.send(m);

        OutputStream os = m.getContent(OutputStream.class);
        InputStream is = getResourceAsStream("GreeterMessage.xml");
        copy(is, os, 8096);
    }

    protected InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    protected Reader getResourceAsReader(String resource) {
        return new InputStreamReader(getResourceAsStream(resource));
    }

    public File getTestFile(String relativePath) {
        return new File(getBasedir(), relativePath);
    }

    public static String getBasedir() {
        if (basedirPath != null) {
            return basedirPath;
        }

        basedirPath = System.getProperty("basedir");

        if (basedirPath == null) {
            basedirPath = new File("").getAbsolutePath();
        }

        return basedirPath;
    }

    private void copy(final InputStream input, final OutputStream output, final int bufferSize)
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
