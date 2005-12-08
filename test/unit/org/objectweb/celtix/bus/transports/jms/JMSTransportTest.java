package org.objectweb.celtix.bus.transports.jms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;

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

public class JMSTransportTest extends TestCase {

    private ServerTransportCallback callback;
    private Bus bus;
    
    private Thread jmsBrokerThread;
    
    public JMSTransportTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSTransportTest.class);
    }
    
    public void setUp() throws Exception {
        bus = Bus.init();
        jmsBrokerThread = new JMSEmbeddedBroker("tcp://localhost:61616");
 
        jmsBrokerThread.start();
        Thread.sleep(5000L);
    }
    
    public void tearDown() throws Exception {
        ((JMSEmbeddedBroker) jmsBrokerThread).shutdownBroker = true;
        if (jmsBrokerThread != null) {
            jmsBrokerThread.join(5000L);
        }
    }
    
    public void testTwoWayTextQueueJMSTransport() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        doTestJMSTransport(false,  serviceName, "HelloWorldPortType", "/wsdl/jms_test.wsdl");       
    }
    
    public void testTwoWayBinaryQueueJMSTransport() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", 
                                                           "HelloWorldQueueBinMsgService");
        doTestJMSTransport(false,  serviceName, "HelloWorldQueueBinMsgPort", "/wsdl/jms_test.wsdl");       
    }
    
    public void xtestJMSTransportUsingAutomaticWorkQueue() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        doTestJMSTransport(true,  serviceName, "HelloWorldPortType", "/wsdl/jms_test.wsdl");
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
    
    public void setupCallbackObject(final boolean useAutomaticWorkQueue) {
        callback = new ServerTransportCallback() {
            public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
                try {
                    byte bytes[] = new byte[10000];
                    int total = readBytes(bytes, ctx.getInputStream());
                    
                    JMSOutputStreamContext octx = 
                        (JMSOutputStreamContext) transport.createOutputStreamContext(ctx);
                    octx.setOneWay(false);
                    transport.finalPrepareOutputStreamContext(octx);
                    octx.getOutputStream().write(bytes, 0, total);
                    octx.getOutputStream().flush();   
                    
                    MessageContext replyCtx = new GenericMessageContext();
                    ctx.put("ObjectMessageContext.MESSAGE_INPUT", Boolean.TRUE);
                    replyCtx.putAll(ctx);
                    replyCtx.put("ObjectMessageContext.MESSAGE_INPUT", Boolean.TRUE);
                    
                    ((JMSServerTransport) transport).postDispatch(replyCtx, octx);
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
    }
       
    
    public void doTestJMSTransport(final boolean useAutomaticWorkQueue,
                        QName serviceName, 
                        String portName, 
                        String testWsdlFileName) 
        throws Exception {
        
        String address = "http://localhost:9000/SoapContext/SoapPort";
        URL wsdlUrl = getClass().getResource(testWsdlFileName);
        assertNotNull(wsdlUrl);
               
        TransportFactory factory = createTransportFactory();
      
        ServerTransport server = createServerTransport(factory, wsdlUrl, serviceName, 
                                                       portName, address);        
        setupCallbackObject(useAutomaticWorkQueue);
        
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
            if (len != -1) {
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
        server.shutdown();
    }
    
    private TransportFactory createTransportFactory() throws BusException { 
        String transportId = "http://celtix.objectweb.org/transports/jms";
        ObjectFactory of = new ObjectFactory();
        ClassNamespaceMappingListType mappings = of.createClassNamespaceMappingListType();
        ClassNamespaceMappingType mapping = of.createClassNamespaceMappingType();
        mapping.setClassname("org.objectweb.celtix.bus.transports.jms.JMSTransportFactory");
        mapping.getNamespace().add(transportId);
        mappings.getMap().add(mapping);
        TransportFactoryManager tfm = new TransportFactoryManagerImpl(bus);
        return tfm.getTransportFactory(transportId);   
    }
    
    private ClientTransport createClientTransport(TransportFactory factory, URL wsdlUrl, 
                                                  QName serviceName, String portName, 
                                                  String address) throws WSDLException, IOException {        
        EndpointReferenceType ref = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, portName);
        ClientTransport transport = factory.createClientTransport(ref);

        return transport;
    }
    
    private ServerTransport createServerTransport(TransportFactory factory, URL wsdlUrl, QName serviceName,
                                                  String portName, String address) throws WSDLException,
        IOException {

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                portName);
        EndpointReferenceUtils.setAddress(ref, address);
        return factory.createServerTransport(ref);
    }
    
    class JMSEmbeddedBroker extends Thread {
        boolean shutdownBroker;
        final String brokerUrl;
        
        
        public JMSEmbeddedBroker(String url) {
            brokerUrl = url;
        }
        
        public void run() {
            try {                
                BrokerContainer container = new BrokerContainerImpl();
                container.addConnector(brokerUrl);
                container.start();
                Object lock = new Object();                
                
                while (!shutdownBroker) {
                    synchronized (lock) {
                        lock.wait(5000L);
                    }
                }
                container.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
