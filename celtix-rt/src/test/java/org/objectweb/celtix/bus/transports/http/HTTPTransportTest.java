package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.bus.transports.TestResponseCallback;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
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
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;
import static org.easymock.EasyMock.isA;

public class HTTPTransportTest extends TestCase {

    private static final QName SERVICE_NAME = new 
        QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
    private static final String PORT_NAME = "SoapPort";
    private static final String ADDRESS = "http://localhost:9000/SoapContext/SoapPort";
    private static final String DECOUPLED_ADDRESS = "http://localhost:9999/decoupled";
    private static final int DECOUPLED_PORT = 9999;

    private static final URL WSDL_URL = HTTPTransportTest.class.getResource("/wsdl/hello_world.wsdl");
    
    private static boolean first = true;
    
    Bus bus;
    private WSDLManager wsdlManager;
    private WorkQueueManagerImpl queueManager;
    private ExecutorService executorService;
    private TestResponseCallback responseCallback;
    private HTTPTransportFactory factory;
    private Lock partialResponseReceivedLock;
    private Condition partialResponseReceivedCondition;
    private boolean partialResponseReceivedNotified;
    
    public HTTPTransportTest(String arg0) {
        super(arg0);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(HTTPTransportTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
                JettyHTTPServerEngine.destroyForPort(9000);
            }
        };
    }
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HTTPTransportTest.class);
    }
    
    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
        wsdlManager = new WSDLManagerImpl(null);
        partialResponseReceivedLock = new ReentrantLock();
        partialResponseReceivedCondition = partialResponseReceivedLock.newCondition();
        partialResponseReceivedNotified = false;
    }
    
    public void tearDown() throws Exception {
        EasyMock.reset(bus);
        checkBusRemovedEvent();
        EasyMock.replay(bus);
        
        if (queueManager != null) {
            queueManager.shutdown(false);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        JettyHTTPServerEngine.destroyForPort(DECOUPLED_PORT);
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

   
    public void testInvokeOneway() throws Exception {
        doTestInvokeOneway(false);
    } 
    
    public void testInvokeOnewayDecoupled() throws Exception {
        doTestInvokeOneway(true);
    } 
    
    public void testInvoke() throws Exception {
        doTestInvoke(false);
        doTestInvoke(false);
    }
    
    public void testInvokeDecoupled() throws Exception {
        doTestInvoke(false, true);
    }

    public void testInvokeUsingAutomaticWorkQueue() throws Exception {
        doTestInvoke(true);
    }

    public void testInvokeDecoupledUsingAutomaticWorkQueue() throws Exception {
        doTestInvoke(true, true);
    }

    public void testInvokeAsync() throws Exception {      
        doTestInvokeAsync(false);
    }
    
    public void testInvokeAsyncDecoupled() throws Exception {      
        doTestInvokeAsync(false, true);
    }
    
    public void testInvokeAsyncUsingAutomaticWorkQueue() throws Exception {      
        doTestInvokeAsync(true);
    }
    
    public void testInvokeAsyncDecoupledUsingAutomaticWorkQueue() throws Exception {      
        doTestInvokeAsync(true, true);
    }

    public void testInputStreamMessageContextCallable() throws Exception {
        factory = createTransportFactory();
        HTTPClientTransport.HTTPClientOutputStreamContext octx = 
            EasyMock.createMock(HTTPClientTransport.HTTPClientOutputStreamContext.class);
        HTTPClientTransport.HTTPClientInputStreamContext ictx =
            EasyMock.createMock(HTTPClientTransport.HTTPClientInputStreamContext.class);
        octx.createInputStreamContext();
        EasyMock.expectLastCall().andReturn(ictx);
        EasyMock.replay(octx);
        
        Callable c = new HTTPClientTransport.InputStreamMessageContextCallable(octx, factory);
        assertNotNull(c);
        InputStreamMessageContext result = (InputStreamMessageContext)c.call();
        assertEquals(result, ictx); 
    }
    
    public void doTestInvokeOneway(boolean decoupled) throws Exception {
        
        factory = createTransportFactory();
      
        ServerTransport server = 
            createServerTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        byte[] buffer = new byte[64];
        activateServer(server, false, 200, buffer, true, decoupled);
        
        ClientTransport client = 
            createClientTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS, decoupled);
        byte outBytes[] = "Hello World!!!".getBytes();

        long start = System.currentTimeMillis();
        OutputStreamMessageContext octx = doRequest(client, outBytes, true, decoupled);
        client.invokeOneway(octx);
        long stop = System.currentTimeMillis();

        octx = doRequest(client, outBytes, false, decoupled);
        client.invokeOneway(octx);
        octx = doRequest(client, outBytes, false, decoupled);
        client.invokeOneway(octx);
        long stop2 = System.currentTimeMillis();
        
        server.deactivate(); 
        EasyMock.reset(bus);
        checkBusRemovedEvent();
        EasyMock.replay(bus);   
        client.shutdown();
        
        assertTrue("Total one call: " + (stop - start), (stop - start) < 400);
        assertTrue("Total: " + (stop2 - start), (stop2 - start) < 600);
        assertEquals(new String(outBytes), new String(buffer, 0, outBytes.length));
        Thread.sleep(200);
    } 

    public void doTestInvoke(final boolean useAutomaticWorkQueue) throws Exception {
        doTestInvoke(useAutomaticWorkQueue, false);
    }
    
    public void doTestInvoke(final boolean useAutomaticWorkQueue, 
                             final boolean decoupled) throws Exception {
               
        factory = createTransportFactory();
      
        ServerTransport server = 
            createServerTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
             
        activateServer(server, useAutomaticWorkQueue, 0, null, false, decoupled);
        //short request
        ClientTransport client = 
            createClientTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS, decoupled);
        doRequestResponse(client, "Hello World".getBytes(), true, decoupled);
        
        //long request
        byte outBytes[] = new byte[5000];
        for (int x = 0; x < outBytes.length; x++) {
            outBytes[x] = (byte)('a' + (x % 26));
        }
        client = 
            createClientTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS, decoupled);
        doRequestResponse(client, outBytes, false, decoupled);
        
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
        activateServer(server, useAutomaticWorkQueue, 0, null, false, decoupled);
        doRequestResponse(client, "Hello World   3".getBytes(), false, decoupled);
        server.deactivate();        
        activateServer(server, useAutomaticWorkQueue, 0, null, false, decoupled);
        doRequestResponse(client, "Hello World   4".getBytes(), false, decoupled);
        server.deactivate();  
        EasyMock.reset(bus);
        checkBusRemovedEvent();       
        EasyMock.replay(bus);
        client.shutdown();
    }
    
    public void doTestInvokeAsync(final boolean useAutomaticWorkQueue) throws Exception {
        doTestInvokeAsync(useAutomaticWorkQueue, false);
    }
    
    public void doTestInvokeAsync(final boolean useAutomaticWorkQueue, boolean decoupled) throws Exception {
        
        Executor executor =  null;
        if (useAutomaticWorkQueue) {
            queueManager = new WorkQueueManagerImpl(bus);
            executor = queueManager.getAutomaticWorkQueue();
        } else {
            executorService = Executors.newFixedThreadPool(1);
            executor = executorService;
        }
        factory = createTransportFactory();
        
        ServerTransport server = 
            createServerTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS);
        activateServer(server, false, 400, null, false, decoupled);
        
        ClientTransport client = 
            createClientTransport(WSDL_URL, SERVICE_NAME, PORT_NAME, ADDRESS, decoupled);
        byte outBytes[] = "Hello World!!!".getBytes();
        
        // wait then read without blocking
        OutputStreamMessageContext octx = doRequest(client, outBytes, true, decoupled);
        Future<InputStreamMessageContext> f = client.invokeAsync(octx, executor);
        assertNotNull(f);
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
        doResponse(client, ictx, outBytes, decoupled);
        
        // blocking read (on new thread)
        octx = doRequest(client, outBytes, false, decoupled);        
        f = client.invokeAsync(octx, executor);
        ictx = f.get();
        assertTrue(f.isDone());
        doResponse(client, ictx, outBytes, decoupled);
        
        // blocking read times out
        boolean timeoutImplemented = false;
        if (timeoutImplemented) {
            octx = doRequest(client, outBytes, false, decoupled);        
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
        
    private void checkBusCreatedEvent() {       
        
        bus.sendEvent(isA(ComponentCreatedEvent.class));
        
        EasyMock.expectLastCall();        
    }
    
    private void checkBusRemovedEvent() {       
        
        bus.sendEvent(isA(ComponentRemovedEvent.class));
        
        EasyMock.expectLastCall();        
    }
    
    private void activateServer(ServerTransport server,
                                final boolean useAutomaticWorkQueue,
                                final int delay,
                                final byte[] buffer,
                                final boolean oneWay,
                                final boolean decoupled) throws Exception {
        ServerTransportCallback callback = new TestServerTransportCallback(server,
                                                                           useAutomaticWorkQueue,
                                                                           delay,
                                                                           buffer,
                                                                           oneWay,
                                                                           decoupled);
        EasyMock.reset(bus);
        Configuration bc = EasyMock.createMock(Configuration.class);
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        server.activate(callback);        
    }

    private void doRequestResponse(ClientTransport client, 
                                   byte outBytes[], 
                                   boolean initial, 
                                   boolean decoupled)
        throws Exception {
        OutputStreamMessageContext octx = doRequest(client, outBytes, initial, decoupled);
        InputStreamMessageContext ictx = client.invoke(octx);
        doResponse(client, ictx, outBytes, decoupled);
    }
    
    private OutputStreamMessageContext doRequest(ClientTransport client, 
                                                 byte outBytes[], 
                                                 boolean initial, 
                                                 boolean decoupled) throws Exception {
        if (decoupled) {
            if (initial) {
                assertFalse(((HTTPTransportFactory)factory).hasDecoupledEndpoint());
                EasyMock.reset(bus);
                Configuration lc = EasyMock.createMock(Configuration.class);      
                bus.getConfiguration();
                EasyMock.expectLastCall().andReturn(lc);
                EasyMock.replay(bus);
            }
            
            EndpointReferenceType decoupledEndpoint = client.getDecoupledEndpoint();
            assertNotNull(decoupledEndpoint);
            assertNotNull(decoupledEndpoint.getAddress());
            assertEquals(decoupledEndpoint.getAddress().getValue(), DECOUPLED_ADDRESS);
            assertTrue(((HTTPTransportFactory)factory).hasDecoupledEndpoint());

            if (initial) {             
                EasyMock.verify(bus);
            }
        }
        OutputStreamMessageContext octx = client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        octx.getOutputStream().write(outBytes);
        return octx;
    }
 
    private void doResponse(ClientTransport client, 
                            InputStreamMessageContext ictx, 
                            byte outBytes[],
                            boolean decoupled) throws Exception {                  
        if (decoupled) {
            assertNull(ictx);
            // discard empty partial response
            responseCallback.waitForNextResponse();
            signalPartialResponseReceived();
            doResponse(client, responseCallback.waitForNextResponse(), outBytes); 
        } else {
            doResponse(client, ictx, outBytes);
        }
    }
    
    private void doResponse(ClientTransport client, 
        InputStreamMessageContext ictx, byte outBytes[]) throws Exception {
        byte bytes[] = new byte[10000];
        int len = readBytes(bytes, ictx.getInputStream());
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
    }

    private void awaitPartialResponseReceived() throws Exception {
        partialResponseReceivedLock.lock();
        try {
            while (!partialResponseReceivedNotified) {
                partialResponseReceivedCondition.await();
            }
        } finally {
            partialResponseReceivedNotified = false;
            partialResponseReceivedLock.unlock();
        }
    }
    
    private void signalPartialResponseReceived() throws Exception {
        partialResponseReceivedLock.lock();
        try {
            partialResponseReceivedNotified = true;
            partialResponseReceivedCondition.signal();
        } finally {
            partialResponseReceivedLock.unlock();
        }
    }
        
    private HTTPTransportFactory createTransportFactory() throws BusException { 
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
        BusLifeCycleManager lifecycleManager = EasyMock.createNiceMock(BusLifeCycleManager.class);
        bus.getLifeCycleManager();
        EasyMock.expectLastCall().andReturn(lifecycleManager);
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getObject("transportFactories");
        EasyMock.expectLastCall().andReturn(mappings);    
        
        EasyMock.replay(bus);
        EasyMock.replay(bc); 
        
        TransportFactoryManager tfm = new TransportFactoryManagerImpl(bus);
        HTTPTransportFactory f = (HTTPTransportFactory)tfm.getTransportFactory(transportId);
        responseCallback = new TestResponseCallback();
        f.setResponseCallback(responseCallback);
        return f;
    }
    
    private ClientTransport createClientTransport(URL wsdlUrl, 
                                                  QName serviceName,
                                                  String portName, 
                                                  String address,
                                                  boolean decoupled) 
        throws WSDLException, IOException {
        EasyMock.reset(bus);
        
        Configuration bc = EasyMock.createMock(Configuration.class);
        Configuration sc = EasyMock.createMock(Configuration.class);
        Configuration pc = EasyMock.createMock(Configuration.class);
        
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/service-config", serviceName.toString());
        EasyMock.expectLastCall().andReturn(sc);
        sc.getChild("http://celtix.objectweb.org/bus/jaxws/port-config", portName);
        EasyMock.expectLastCall().andReturn(pc); 
        pc.getChild("http://celtix.objectweb.org/bus/transports/http/http-client-config", "http-client");
        EasyMock.expectLastCall().andReturn(null); 
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
        pc.getString("address");
        EasyMock.expectLastCall().andReturn(address);
        
        checkBusCreatedEvent();
        
        EasyMock.replay(bus);
        EasyMock.replay(bc);
        EasyMock.replay(sc);
        EasyMock.replay(pc);
        
        EndpointReferenceType ref = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, portName);
        ClientTransport transport = factory.createClientTransport(ref);
        if (decoupled) {
            ((HTTPClientTransport)transport).policy.setDecoupledEndpoint(DECOUPLED_ADDRESS);
        }
       
        EasyMock.verify(bus);
        EasyMock.verify(bc);
        EasyMock.verify(sc);
        EasyMock.verify(pc);
        return transport;
        
    }
    
    private ServerTransport createServerTransport(URL wsdlUrl,
                                                  QName serviceName,
                                                  String portName,
                                                  String address)
        throws WSDLException, IOException {

        EasyMock.reset(bus);

        Configuration bc = EasyMock.createMock(Configuration.class);
        Configuration ec = EasyMock.createMock(Configuration.class);

        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/endpoint-config", serviceName.toString());
        EasyMock.expectLastCall().andReturn(ec);
        ec.getChild("http://celtix.objectweb.org/bus/transports/http/http-server-config", "http-server");
        EasyMock.expectLastCall().andReturn(null);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager);
        if (first) {
            //first call will configure the port listener
            bus.getConfiguration();
            EasyMock.expectLastCall().andReturn(bc);
            bc.getChild("http://celtix.objectweb.org/bus/transports/http/http-listener-config", 
                        "http-listener.9000");
            EasyMock.expectLastCall().andReturn(null);
            first = false;
        }
        
        checkBusCreatedEvent();
       
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
    
    private class TestServerTransportCallback implements ServerTransportCallback {
        private ServerTransport server;
        private boolean useAutomaticWorkQueue;
        private int delay;
        private byte[] buffer;
        private boolean oneWay;
        private boolean decoupled;
        
        TestServerTransportCallback(ServerTransport s,
                                    boolean uaq,
                                    int d,
                                    byte[] b,
                                    boolean ow,
                                    boolean dc) {
            server = s;
            useAutomaticWorkQueue = uaq;
            delay = d;
            buffer = b;
            oneWay = ow;
            decoupled = dc;
        }
        
        public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
            try {
                byte[] bytes = buffer;
                if (null == bytes) {
                    bytes = new byte[10000];
                }
                int total = readBytes(bytes, ctx.getInputStream());
                
                OutputStreamMessageContext octx = null;
                if (decoupled) {
                    EndpointReferenceType ref = new EndpointReferenceType();
                    EndpointReferenceUtils.setAddress(ref, DECOUPLED_ADDRESS);
                    octx = server.rebase(ctx, ref);
                    server.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().flush();
                    octx.getOutputStream().close();
                    assertEquals(ctx.get(HTTPServerInputStreamContext.HTTP_RESPONSE), ref);
                    if (!oneWay) {
                        awaitPartialResponseReceived();
                    }
                }
                
                if (oneWay) {
                    octx = transport.createOutputStreamContext(ctx);
                    octx.setOneWay(oneWay);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().close();
                    transport.postDispatch(ctx, octx);
                }
                
                // simulate implementor call 
                if (delay > 0) {                       
                    Thread.sleep(delay);
                }
                
                if (!oneWay) {
                    octx = transport.createOutputStreamContext(ctx);
                    octx.setOneWay(oneWay);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().write(bytes, 0, total);
                    octx.getOutputStream().flush();
                    octx.getOutputStream().close();
                    transport.postDispatch(ctx, octx);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        public synchronized Executor getExecutor() {
            EasyMock.reset(bus);
            checkBusCreatedEvent();
            EasyMock.replay(bus);
            if (useAutomaticWorkQueue) {
                if (queueManager == null) {
                    queueManager = new WorkQueueManagerImpl(bus);
                }
                return queueManager.getAutomaticWorkQueue();
            } else {
                return null;
            }
        }
    }
}
