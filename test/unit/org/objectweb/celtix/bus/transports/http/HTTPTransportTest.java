package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
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
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

public class HTTPTransportTest extends TestCase {

    private static final QName SERVICE_NAME = new 
        QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
    private static final String PORT_NAME = "SoapPort";
    private static final String ADDRESS = "http://localhost:9000/SoapContext/SoapPort";
    private static final URL WSDL_URL = HTTPTransportTest.class.getResource("/wsdl/hello_world.wsdl");
    
    private static boolean first = true;
    
    Bus bus;
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
    int readBytes(byte bytes[], InputStream ins) throws IOException {
        int len = ins.read(bytes);
        int total = 0;
        while (len != -1) {
            total += len;
            len = ins.read(bytes, total, bytes.length - total);
        }
        return total;
    }
    
    
    public void testInvoke() throws Exception {
        doTestInvoke(false);
        doTestInvoke(false);
    }
    
    public void testInvokeUsingAutomaticWorkQueue() throws Exception {
        doTestInvoke(true);
    }
    
    public void testInvokeAsync() throws Exception {      
        doTestInvokeAsync(false);
    }
    
    public void testInvokeAsyncUsingAutomaticWorkQueue() throws Exception {      
        doTestInvokeAsync(true);
    }
    
    public void testInvokeOneway() throws Exception {
               
        TransportFactory factory = createTransportFactory();
      
        ServerTransport server = createServerTransport(factory, WSDL_URL, SERVICE_NAME,
                                                       PORT_NAME, ADDRESS);
        byte[] buffer = new byte[64];
        activateServer(server, false, 400, buffer);
        
        ClientTransport client = createClientTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        byte outBytes[] = "Hello World!!!".getBytes();
        
        OutputStreamMessageContext octx = doRequest(client, outBytes);
        client.invokeOneway(octx);
        
        assertEquals(new String(outBytes), new String(buffer, 0, outBytes.length));
    } 

    public void testInputStreamMessageContextCallable() throws Exception {
        HTTPClientTransport.HTTPClientOutputStreamContext octx = 
            EasyMock.createMock(HTTPClientTransport.HTTPClientOutputStreamContext.class);
        HTTPClientTransport.HTTPClientInputStreamContext ictx =
            EasyMock.createMock(HTTPClientTransport.HTTPClientInputStreamContext.class);
        octx.createInputStreamContext();
        EasyMock.expectLastCall().andReturn(ictx);
        EasyMock.replay(octx);
        
        Callable c = new HTTPClientTransport.InputStreamMessageContextCallable(octx);
        assertNotNull(c);
        InputStreamMessageContext result = (InputStreamMessageContext)c.call();
        assertEquals(result, ictx); 
    }

    public void doTestInvoke(final boolean useAutomaticWorkQueue) throws Exception {
               
        TransportFactory factory = createTransportFactory();
      
        ServerTransport server = createServerTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
             
        activateServer(server, useAutomaticWorkQueue, 0, null);
        //short request
        ClientTransport client = createClientTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        doRequestResponse(client, "Hello World".getBytes());
        
        //long request
        byte outBytes[] = new byte[5000];
        for (int x = 0; x < outBytes.length; x++) {
            outBytes[x] = (byte)('a' + (x % 26));
        }
        client = createClientTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        doRequestResponse(client, outBytes);
        
        server.deactivate();
        outBytes = "HelloWorld".getBytes();
 
        try {
            OutputStreamMessageContext octx = client.createOutputStreamContext(new GenericMessageContext());
            client.finalPrepareOutputStreamContext(octx);
            octx.getOutputStream().write(outBytes);
            octx.getOutputStream().close();
            InputStreamMessageContext ictx = client.invoke(octx);
            byte bytes[] = new byte[10000];
            int len = ictx.getInputStream().read(bytes);
            if (len != -1
                && new String(bytes, 0, len).indexOf("HTTP Status 503") == -1
                && new String(bytes, 0, len).indexOf("Error 404") == -1) {
                fail("was able to process a message after the servant was deactivated: " + len 
                     + " - " + new String(bytes));
            }
        } catch (IOException ex) {
            //ignore - this is what we want
        }
        activateServer(server, useAutomaticWorkQueue, 0, null);
        doRequestResponse(client, "Hello World   3".getBytes());
        server.deactivate();        
        activateServer(server, useAutomaticWorkQueue, 0, null);
        doRequestResponse(client, "Hello World   4".getBytes());
        server.deactivate();   
        client.shutdown();
    }
    
    public void doTestInvokeAsync(final boolean useAutomaticWorkQueue) throws Exception {
        
        Executor executor =  null;
        if (useAutomaticWorkQueue) {
            executor = new WorkQueueManagerImpl(bus).getAutomaticWorkQueue();
        } 
        TransportFactory factory = createTransportFactory();
        
        ServerTransport server = createServerTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        activateServer(server, false, 400, null);
        
        ClientTransport client = createClientTransport(factory, WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        byte outBytes[] = "Hello World!!!".getBytes();
        
        // wait then read without blocking
        OutputStreamMessageContext octx = doRequest(client, outBytes);
        Future<InputStreamMessageContext> f = client.invokeAsync(octx, executor);
        assertNotNull(f);
        assertFalse(f.isDone());
        int i = 0;
        while (i < 10) {
            Thread.sleep(100);
            if (f.isDone()) {
                break;                
            }
            i++;
        }
        assertTrue(f.isDone());
        InputStreamMessageContext ictx = f.get();
        doResponse(client, ictx, outBytes);
        
        // blocking read (on new thread)
        octx = doRequest(client, outBytes);        
        f = client.invokeAsync(octx, executor);
        ictx = f.get();
        assertTrue(f.isDone());
        doResponse(client, ictx, outBytes);
        
        // blocking read times out
        boolean timeoutImplemented = false;
        if (timeoutImplemented) {
            octx = doRequest(client, outBytes);        
            f = client.invokeAsync(octx, executor);
            try {            
                ictx = f.get(200, TimeUnit.MILLISECONDS);
                fail("Expected TimeoutException not thrown.");
            } catch (TimeoutException ex) {
                // ignore
            }
            assertTrue(!f.isDone());
        }
        server.deactivate();        
    }
    
    private void activateServer(ServerTransport server,
                                final boolean useAutomaticWorkQueue,
                                final int delay,
                                final byte[] buffer) throws Exception {
        ServerTransportCallback callback = new ServerTransportCallback() {
            public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
                try {
                    byte[] bytes = buffer;
                    if (null == bytes) {
                        bytes = new byte[10000];
                    }
                    int total = readBytes(bytes, ctx.getInputStream());
                    if (delay > 0) {                       
                        Thread.sleep(delay);
                    }
                    
                    OutputStreamMessageContext octx = transport.createOutputStreamContext(ctx);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().write(bytes, 0, total);
                    octx.getOutputStream().flush();

                    transport.postDispatch(ctx, octx);
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

        EasyMock.reset(bus);
        Configuration bc = EasyMock.createMock(Configuration.class);
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        server.activate(callback);        
    }
    
    private void doRequestResponse(ClientTransport client, byte outBytes[]) throws Exception {
        OutputStreamMessageContext octx = doRequest(client, outBytes);
        InputStreamMessageContext ictx = client.invoke(octx);
        doResponse(client, ictx, outBytes);
    }
    
    private OutputStreamMessageContext doRequest(ClientTransport client, byte outBytes[]) throws Exception {
        OutputStreamMessageContext octx = client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        octx.getOutputStream().write(outBytes);
        return octx;
    }
    
    private void doResponse(ClientTransport client, 
        InputStreamMessageContext ictx, byte outBytes[]) throws Exception {
        byte bytes[] = new byte[10000];
        int len = readBytes(bytes, ictx.getInputStream());
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
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
        
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
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
                                                  String portName, String address)
        throws WSDLException, IOException {
        EasyMock.reset(bus);

        Configuration bc = EasyMock.createMock(Configuration.class);
        Configuration ec = EasyMock.createMock(Configuration.class);

        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/endpoint-config", serviceName);
        EasyMock.expectLastCall().andReturn(ec);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
        if (first) {
            //first call will configure the port listener
            bus.getConfiguration();
            EasyMock.expectLastCall().andReturn(bc);
            first = false;
        }

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
