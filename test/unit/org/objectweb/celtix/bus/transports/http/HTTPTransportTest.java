package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.configuration.types.ObjectFactory;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

public class HTTPTransportTest extends TestCase {
    
    private Bus bus;
    private WSDLManager wsdlManager;
    
    public HTTPTransportTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HTTPTransportTest.class);
    }
    
    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
        wsdlManager = new WSDLManagerImpl(null);
    }
    
    public void testHTTPTransport() throws Exception {
        doTestHTTPTransport(false);
        // currently causes java.net.SocketTimeoutException
        // doTestHTTPTransport(false);
    }
    
    public void xtestHTTPTransportUsingAutomaticWorkQueue() throws Exception {
        doTestHTTPTransport(true);
    }

    private int readBytes(byte bytes[], InputStream ins) throws IOException {
        int len = ins.read(bytes);
        int total = 0;
        while (len != -1) {
            total += len;
            len = ins.read(bytes, total, bytes.length - total);
        }
        return total;
    }
    
    public void doTestHTTPTransport(final boolean useAutomaticWorkQueue) throws Exception {
        
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        String portName = "SoapPort";
        String address = "http://localhost:9000/SoapContext/SoapPort";
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdlUrl);
               
        TransportFactory factory = createTransportFactory();
      
        ServerTransport server = createServerTransport(factory, wsdlUrl, serviceName, 
                                                       portName, address);          
             
        ServerTransportCallback callback = new ServerTransportCallback() {
            public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
                try {
                    byte bytes[] = new byte[10000];
                    int total = readBytes(bytes, ctx.getInputStream());
                    
                    OutputStreamMessageContext octx = transport.createOutputStreamContext(ctx);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().write(bytes, 0, total);
                    octx.getOutputStream().flush();
                    octx.getOutputStream().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            public Executor getExecutor() {
                if (useAutomaticWorkQueue) {
                    return new WorkQueueManagerImpl(bus).getAutomaticWorkQueue();
                } else {
                    return null;
                }
                
            }
        };
        server.activate(callback);
        
        ClientTransport client = createClientTransport(factory, wsdlUrl, serviceName, portName, address);
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
        
        //long request
        outBytes = new byte[5000];
        for (int x = 0; x < outBytes.length; x++) {
            outBytes[x] = (byte)('a' + (x % 26));
        }
        client = createClientTransport(factory, wsdlUrl, serviceName, portName, address);
        octx = client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        octx.getOutputStream().write(outBytes);
        ictx = client.invoke(octx);
        int total = readBytes(bytes, ictx.getInputStream());
        
        assertTrue("Did not read anything " + total, total > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, total));
        
        outBytes = "Hello World!!!".getBytes();
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
        server.deactivate();
    }
    
    private TransportFactory createTransportFactory() throws BusException { 
        EasyMock.reset(bus);
        Configuration bc = EasyMock.createMock(Configuration.class);
        
        String transportId = "http://celtix.objectweb.org/transports/http/configuration";
        ObjectFactory of = new ObjectFactory();
        ClassNamespaceMappingListType mappings = of.createClassNamespaceMappingListType();
        ClassNamespaceMappingType mapping = of.createClassNamespaceMappingType();
        mapping.setClassname("org.objectweb.celtix.bus.transports.http.HTTPTransportFactory");
        mapping.getNamespace().add(transportId);
        mappings.getMap().add(mapping);
        
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getObject("transportFactories");
        EasyMock.expectLastCall().andReturn(mappings);       
        
        EasyMock.replay(bus);
        EasyMock.replay(bc); 
        
        TransportFactoryManager tfm = new TransportFactoryManagerImpl(bus);
        return tfm.getTransportFactory(transportId);   
    }
    
    private ClientTransport createClientTransport(TransportFactory factory, URL wsdlUrl, 
                                                  QName serviceName, String portName, 
                                                  String address) throws WSDLException, IOException {
        EasyMock.reset(bus);
        
        Configuration bc = EasyMock.createMock(Configuration.class);
        Configuration sc = EasyMock.createMock(Configuration.class);
        Configuration pc = EasyMock.createMock(Configuration.class);
        
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/service-config", serviceName);
        EasyMock.expectLastCall().andReturn(sc);
        sc.getChild("http://celtix.objectweb.org/bus/jaxws/port-config", portName);
        EasyMock.expectLastCall().andReturn(pc);  
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
        pc.getString("address");
        EasyMock.expectLastCall().andReturn(address);
       
        EasyMock.replay(bus);
        EasyMock.replay(bc);
        EasyMock.replay(sc);
        EasyMock.replay(pc);
        
        EndpointReferenceType ref = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, portName);
        ClientTransport transport = factory.createClientTransport(ref);
       
        EasyMock.verify(bus);
        EasyMock.verify(bc);
        EasyMock.verify(sc);
        EasyMock.verify(pc);
        return transport;
        
    }
    
    private ServerTransport createServerTransport(TransportFactory factory, URL wsdlUrl, QName serviceName,
                                                  String portName, String address) throws WSDLException,
        IOException {
        EasyMock.reset(bus);

        Configuration bc = EasyMock.createMock(Configuration.class);
        Configuration ec = EasyMock.createMock(Configuration.class);

        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/endpoint-config", serviceName);
        EasyMock.expectLastCall().andReturn(ec);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);

        EasyMock.replay(bus);
        EasyMock.replay(bc);
        EasyMock.replay(ec);

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                portName);
        EndpointReferenceUtils.setAddress(ref, address);
        ServerTransport transport = factory.createServerTransport(ref);

        EasyMock.verify(bus);
        EasyMock.verify(bc);
        EasyMock.verify(ec);
        
        return transport;

    }
}
