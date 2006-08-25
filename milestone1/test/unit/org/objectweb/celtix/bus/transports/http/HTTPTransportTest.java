package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPTransportTest extends TestCase {

    public HTTPTransportTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HTTPTransportTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHTTPTransport() throws Exception {
        Bus bus = Bus.init(new String[0]);
        TransportFactory factory = 
            bus.getTransportFactoryManager().getTransportFactory(
                "http://celtix.objectweb.org/transports/http/configuration");
        
        URL wsdlUrl = getClass().getResource("/org/objectweb/celtix/resources/hello_world.wsdl");
        assertNotNull(wsdlUrl);
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        EndpointReferenceType address = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, "SoapPort");
        ServerTransport server = factory.createServerTransport(address);
        
        ServerTransportCallback callback = new ServerTransportCallback() {
            public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
                try {
                    byte bytes[] = new byte[10000];
                    int len = ctx.getInputStream().read(bytes);
                    
                    OutputStreamMessageContext octx = transport.createOutputStreamContext(ctx);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().write(bytes, 0, len);
                    octx.getOutputStream().flush();
                    octx.getOutputStream().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        server.activate(callback);
        
        ClientTransport client = factory.createClientTransport(address);
        OutputStreamMessageContext octx = 
            client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        byte outBytes[] = "Hello World!!!".getBytes(); 
        octx.getOutputStream().write(outBytes);
        InputStreamMessageContext ictx = client.invoke(octx);
        byte bytes[] = new byte[10000];
        int len = ictx.getInputStream().read(bytes);
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
        
        server.deactivate();
        
        try {
            octx = client.createOutputStreamContext(new GenericMessageContext());
            client.finalPrepareOutputStreamContext(octx);
            octx.getOutputStream().write(outBytes);
            ictx = client.invoke(octx);
            len = ictx.getInputStream().read(bytes);
            if (len != -1
                && new String(bytes, 0, len).indexOf("HTTP Status 503") == -1) {
                fail("was able to process a message after the servant was deactivated: " + len 
                     + " - " + new String(bytes));
            }
        } catch (IOException ex) {
            //ignore - this is what we want
        }
        server.activate(callback);
        octx = client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        octx.getOutputStream().write(outBytes);
        ictx = client.invoke(octx);
        len = ictx.getInputStream().read(bytes);
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
    }
    
}
