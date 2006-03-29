package org.objectweb.celtix.bus.transports.jms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.bindings.TestClientBinding;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
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
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSPropertyType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class JMSTransportTest extends TestCase {

    public static final String JMSTRANSPORT_SKIP_RESPONSE = "JMSTransport.skipResponse";
    private ServerTransportCallback callback;
    private ServerTransportCallback callback1;
    private Bus bus;
    private String serverRcvdInOneWayCall;
    private WorkQueueManagerImpl wqm;

    public JMSTransportTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSTransportTest.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JMSTransportTest.class);
        return  new JMSBrokerSetup(suite, "tcp://localhost:61500");
    }

    public void setUp() throws Exception {
        bus = Bus.init();
    }

    public void tearDown() throws Exception {
        if (wqm != null) {
            wqm.shutdown(true);
        }
        if (bus != null) {
            bus.shutdown(true);
        }
    }

    public void testOneWayTextQueueJMSTransport() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms",
                                                           "HelloWorldOneWayQueueService");
        doOneWayTestJMSTranport(false,  serviceName, "HelloWorldOneWayQueuePort",
                                    "/wsdl/jms_test.wsdl");
    }

    public void testPubSubJMSTransport() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms",
                                                           "HelloWorldPubSubService");
        doOneWayTestJMSTranport(false,  serviceName, "HelloWorldPubSubPort",
                                               "/wsdl/jms_test.wsdl");
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

    public void test2WayStaticReplyQTextMessageJMSTransport() throws Exception {
        QName serviceName =
            new QName("http://celtix.objectweb.org/hello_world_jms",
                                     "HWStaticReplyQTextMsgService");
        doTestJMSTransport(false,  serviceName, "HWStaticReplyQTextPort", "/wsdl/jms_test.wsdl");
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


    public class TestServerTransportCallback implements ServerTransportCallback {
        boolean useAutomaticWorkQueue;

        public TestServerTransportCallback(boolean useAutoWQ) {
            useAutomaticWorkQueue = useAutoWQ;
        }

        public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {

            try {
                byte bytes[] = new byte[10000];
                if (ctx.containsKey(JMSConstants.JMS_SERVER_HEADERS)) {
                    JMSMessageHeadersType msgHdr =
                        (JMSMessageHeadersType) ctx.get(JMSConstants.JMS_SERVER_HEADERS);
                    if (msgHdr.getProperty().contains(JMSTRANSPORT_SKIP_RESPONSE)) {
                        //no need to process the response.
                        return;                    
                    }
                }

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
             //
            }
        }

        public Executor getExecutor() {
            if (useAutomaticWorkQueue) {
                if (wqm == null) {
                    wqm = new WorkQueueManagerImpl(bus);
                }
                return wqm.getAutomaticWorkQueue();
            } else {
                return null;
            }
        }
    }

    public void setupCallbackObject(final boolean useAutomaticWorkQueue) {
        callback = new TestServerTransportCallback(useAutomaticWorkQueue);
    }

    public void doTestJMSTransport(final boolean useAutomaticWorkQueue,
                        QName serviceName,
                        String portName,
                        String testWsdlFileName)
        throws Exception {

        String address = "http://localhost:9000/SoapContext/SoapPort";
        URL wsdlUrl = getClass().getResource(testWsdlFileName);
        assertNotNull(wsdlUrl);

        createConfiguration(wsdlUrl, serviceName, portName);
        TransportFactory factory = createTransportFactory();

        ServerTransport server = createServerTransport(factory, wsdlUrl, serviceName,
                                                       portName, address);
        setupCallbackObject(useAutomaticWorkQueue);

        server.activate(callback);

        ClientTransport client = createClientTransport(factory, wsdlUrl, serviceName, portName, address);

        OutputStreamMessageContext octx = null;
        byte outBytes[] = "Hello World!!!".getBytes();
        InputStreamMessageContext ictx = doClientInvoke(client, octx, outBytes, false);

        byte bytes[] = new byte[10000];
        int len = ictx.getInputStream().read(bytes);
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));

        //long request
        outBytes = new byte[5000];
        for (int x = 0; x < outBytes.length; x++) {
            outBytes[x] = (byte)('a' + (x % 26));
        }

        ictx = doClientInvoke(client, octx, outBytes, false);
        int total = readBytes(bytes, ictx.getInputStream());

        assertTrue("Did not read anything " + total, total > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, total));

        outBytes = "Hello World!!!".getBytes();

        server.deactivate();

        try {
            ictx = doClientInvoke(client, octx, outBytes, true);
            len = ictx.getInputStream().read(bytes);

            if (len != -1) {
                fail("was able to process a message after the servant was deactivated: " + len
                     + " - " + new String(bytes));
            }
        } catch (IOException ex) {
            //ignore - this is what we want
        }

        server.activate(callback);

        outBytes = "New String and must match with response".getBytes();
        ictx = doClientInvoke(client, octx, outBytes, false);
        len = ictx.getInputStream().read(bytes);
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
        server.shutdown();
        client.shutdown();
    }

    public InputStreamMessageContext doClientInvoke(ClientTransport client,
                                                    OutputStreamMessageContext octx,
                                                    byte[] outBytes,
                                                    boolean insertContextInfo)
        throws Exception {
        octx = client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        octx.getOutputStream().write(outBytes);
        if (insertContextInfo) {
            insertContextInfo(octx);
        }
        return client.invoke(octx);
    }

    public void insertContextInfo(OutputStreamMessageContext octx) {
        //Set time to live and default receive timeout so as to timeout the client
        JMSMessageHeadersType requestHeader = new JMSMessageHeadersType();
        requestHeader.setTimeToLive(100L);
        List<JMSPropertyType> props = requestHeader.getProperty();
        JMSPropertyType skipResponseProperty = new JMSPropertyType();
        skipResponseProperty.setName("JMSTransportTest.skipResponse");
        skipResponseProperty.setValue("true");
        props.add(skipResponseProperty);
        octx.put(JMSConstants.JMS_CLIENT_REQUEST_HEADERS, requestHeader);
        octx.put(JMSConstants.JMS_CLIENT_RECEIVE_TIMEOUT, new Long(10));

    }

    public void setupOneWayCallbackObject(final boolean useAutomaticWorkQueue) {
        callback1 = new ServerTransportCallback() {
            public void dispatch(InputStreamMessageContext ctx, ServerTransport transport) {
                try {
                    byte bytes[] = new byte[10000];
                    readBytes(bytes, ctx.getInputStream());

                    JMSOutputStreamContext octx =
                        (JMSOutputStreamContext) transport.createOutputStreamContext(ctx);
                    octx.setOneWay(true);
                    transport.finalPrepareOutputStreamContext(octx);
                    serverRcvdInOneWayCall = new String(bytes);

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
                    if (wqm == null) {
                        wqm = new WorkQueueManagerImpl(bus);
                    }
                    return wqm.getAutomaticWorkQueue();
                } else {
                    return null;
                }

            }
        };
    }

    public void doOneWayTestJMSTranport(final boolean useAutomaticWorkQueue,
                                                            QName serviceName,
                                                             String portName,
                                                             String testWsdlFileName)
        throws Exception {

        String address = "http://localhost:9000/SoapContext/SoapPort";
        URL wsdlUrl = getClass().getResource(testWsdlFileName);
        assertNotNull(wsdlUrl);

        createConfiguration(wsdlUrl, serviceName, portName);
        TransportFactory factory = createTransportFactory();
        setupOneWayCallbackObject(useAutomaticWorkQueue);

        ServerTransport server = createServerTransport(factory, wsdlUrl, serviceName,
                                                       portName, address);


        server.activate(callback1);

        ClientTransport client = createClientTransport(factory, wsdlUrl, serviceName, portName, address);
        OutputStreamMessageContext octx =
            client.createOutputStreamContext(new GenericMessageContext());
        client.finalPrepareOutputStreamContext(octx);
        byte outBytes[] = "Hello World!!!".getBytes();
        octx.getOutputStream().write(outBytes);
        client.invokeOneway(octx);
        Thread.sleep(500L);
        assertEquals(new String(outBytes),
                          serverRcvdInOneWayCall.substring(0, outBytes.length));

        server.shutdown();
        client.shutdown();
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
        ClientTransport transport = 
            factory.createClientTransport(ref, new TestClientBinding(bus, ref));

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

    // Create bus and populate the configuration for Endpoint, Service and port.
    // This test uses all the info. either coming from WSDL or default and do not use
    // any configuration files.
    //

    private void createConfiguration(URL wsdlUrl, QName serviceName,
                          String portName) throws WSDLException,
                          IOException, BusException {
        assert bus != null;
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                portName);
        Configuration busCfg = bus.getConfiguration();
        assert null != busCfg;
        String id = EndpointReferenceUtils.getServiceName(ref).toString();
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        cb.buildConfiguration(JMSConstants.ENDPOINT_CONFIGURATION_URI, id, busCfg);
        cb.buildConfiguration(JMSConstants.PORT_CONFIGURATION_URI,
                              id + "/" + EndpointReferenceUtils.getPortName(ref).toString(),
                              busCfg);
    }
}
