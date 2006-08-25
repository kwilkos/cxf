package org.objectweb.celtix.bus.transports.jms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;

import javax.jms.DeliveryMode;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;

import org.objectweb.celtix.transports.jms.JMSAddressPolicyType;
import org.objectweb.celtix.transports.jms.JMSClientBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class JMSContextTest extends TestCase {

    public static final String TEST_CORRELATION_ID = "TestCorrelationId";
    private ServerTransportCallback callback;
    private Bus bus;
    private WorkQueueManagerImpl wqm;
    
    public JMSContextTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSContextTest.suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JMSContextTest.class);
        return  new JMSBrokerSetup(suite, "tcp://localhost:61500");
    }
    
    public void setUp() throws Exception {
        bus = Bus.init();
        JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                          Port.class,
                                          JMSAddressPolicyType.class);
        JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                          Port.class,
                                          JMSServerBehaviorPolicyType.class);
        JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                          Port.class,
                                          JMSClientBehaviorPolicyType.class);
    }
    
    public void tearDown() throws Exception {
        if (wqm != null) {
            wqm.shutdown(true);
        }
        if (bus != null) {
            bus.shutdown(true);
        }
    }
    
    public void testTwoWayTextQueueJMSTransport() throws Exception {
        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        doTestJMSTransport(false,  serviceName, "HelloWorldPort", "/wsdl/jms_test.wsdl");       
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
                
                int total = readBytes(bytes, ctx.getInputStream());

                JMSOutputStreamContext octx =
                    (JMSOutputStreamContext)transport.createOutputStreamContext(ctx);
                octx.setOneWay(false);
                transport.finalPrepareOutputStreamContext(octx);
                octx.getOutputStream().write(bytes, 0, total);
                octx.getOutputStream().flush();

                MessageContext replyCtx = new GenericMessageContext();
                ctx.put("ObjectMessageContext.MESSAGE_INPUT", Boolean.TRUE);
                replyCtx.putAll(ctx);
                replyCtx.put("ObjectMessageContext.MESSAGE_INPUT", Boolean.TRUE);

                ((JMSServerTransport)transport).postDispatch(replyCtx, octx);
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
        
        ServerTransport server = createServerTransport(wsdlUrl, serviceName, 
                                                       portName, address);
        setupCallbackObject(useAutomaticWorkQueue);
        
        server.activate(callback);
        
        TestJMSClientTransport client = createClientTransport(wsdlUrl, serviceName, portName, address);
        OutputStreamMessageContext octx = 
            client.createOutputStreamContext(new GenericMessageContext());
        
        setRequestContextHeader(octx);
        
        client.finalPrepareOutputStreamContext(octx);
        
        byte outBytes[] = "Hello World!!!".getBytes(); 
        octx.getOutputStream().write(outBytes);
        
        // make sure that the inner context that we used to create has the same values that we inserted 
        checkContextHeader(client.getContext(), JMSConstants.JMS_CLIENT_REQUEST_HEADERS);
        InputStreamMessageContext ictx = client.invoke(octx);
        byte bytes[] = new byte[10000];
        int len = ictx.getInputStream().read(bytes);
        assertTrue("Did not read anything " + len, len > 0);
        assertEquals(new String(outBytes), new String(bytes, 0, len));
 
        checkResponseContextHeader(ictx);
       
        server.shutdown();
        client.shutdown();
    }
    
    public void checkResponseContextHeader(MessageContext ctx) {
        assertTrue("JMSContext should contain the property " + JMSConstants.JMS_CLIENT_RESPONSE_HEADERS,
                   null != ctx.get(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS));

        JMSMessageHeadersType responseHeader = (JMSMessageHeadersType) 
                                                ctx.get(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS);
        assertTrue("JMSHeader correlation id mismatch: expected " + TEST_CORRELATION_ID, 
                   TEST_CORRELATION_ID.equals(responseHeader.getJMSCorrelationID()));
        assertTrue("JMSRedelivered should be false", 
                   !responseHeader.isJMSRedelivered());
    }
    
    public void setRequestContextHeader(OutputStreamMessageContext octx) {
        JMSMessageHeadersType requestHeader = new JMSMessageHeadersType();
        requestHeader.setJMSCorrelationID(TEST_CORRELATION_ID);
        requestHeader.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        requestHeader.setJMSExpiration(3600000L);
        requestHeader.setJMSPriority(6);
        requestHeader.setTimeToLive(3600000L);
        
        octx.put(JMSConstants.JMS_CLIENT_REQUEST_HEADERS, requestHeader);
    }
    
    public void checkContextHeader(MessageContext ctx, String headerName) {
        
        assertTrue("JMSContext should contain the property " + headerName,
                          null != ctx.get(headerName));
       
        JMSMessageHeadersType reqHdr = (JMSMessageHeadersType) 
                ctx.get(headerName);
        assertTrue("JMSHeader correlation id mismatch: expected " + TEST_CORRELATION_ID, 
                          TEST_CORRELATION_ID.equals(reqHdr.getJMSCorrelationID()));
        assertTrue("JMSRedelivered should be false", 
                   !reqHdr.isSetJMSRedelivered());
        assertTrue("JMS priority should be 6", reqHdr.getJMSPriority() == 6);
        assertTrue("JMS timetolive should be greater than 0 ", reqHdr.getTimeToLive() > 0);
    }
    
    private TestJMSClientTransport createClientTransport(URL wsdlUrl, 
                                                  QName serviceName, String portName, 
                                                  String address) throws WSDLException, IOException {        
        EndpointReferenceType ref = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, portName);
        return new TestJMSClientTransport(bus, ref, null);
    }
    
    private ServerTransport createServerTransport(URL wsdlUrl, QName serviceName,
                                                  String portName, String address) throws WSDLException,
        IOException {

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                portName);        
        EndpointReferenceUtils.setAddress(ref, address);
        return new JMSServerTransport(bus, ref);
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
    
    public class TestJMSClientTransport extends JMSClientTransport {
       
        private  MessageContext localContext;
        
        public TestJMSClientTransport(Bus theBus, 
                                      EndpointReferenceType address, 
                                      ClientBinding binding) 
            throws WSDLException, IOException {
            super(theBus, address, binding);
        }
        
        @Override
        public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
            throws IOException {
            localContext = context;
            return new JMSOutputStreamContext(context);
        }
        
        public MessageContext getContext() {
            return localContext;
        }
        
    }

}
