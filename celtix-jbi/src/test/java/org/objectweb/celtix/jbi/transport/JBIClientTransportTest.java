package org.objectweb.celtix.jbi.transport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.hello_world_soap_http.Greeter;


public class JBIClientTransportTest extends TestCase {

    private static final String TEST_MESSAGE = "<message>this is the test message</message>";
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; 
    
    private final DeliveryChannel channel = EasyMock.createMock(DeliveryChannel.class);
    //private final EndpointReferenceType endpointRef = EasyMock.createMock(EndpointReferenceType.class);
    private EndpointReferenceType endpointRef;
    //private final MetadataType metaData = EasyMock.createMock(MetadataType.class);
    private final ClientBinding clientBinding = EasyMock.createMock(ClientBinding.class);


    private JBIOutputStreamMessageContext outCtx;
     
    
    private JBIClientTransport clientTransport;
    private QName serviceName; 
    private Method targetMethod;
    
    public void setUp() throws Exception { 
    
        initFixture();
        clientTransport = new JBIClientTransport(channel, endpointRef, clientBinding);

        ObjectMessageContextImpl context = new ObjectMessageContextImpl(); 
        
        outCtx = (JBIOutputStreamMessageContext)clientTransport.createOutputStreamContext(context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(TEST_MESSAGE.getBytes());       
        outCtx.setOutputStream(baos);
        
        targetMethod = Greeter.class.getMethod("sayHi");
        assertNotNull("could not set up target method", targetMethod);
        context.setMethod(targetMethod);

        WebService ws = Greeter.class.getAnnotation(WebService.class);
        assertNotNull(ws);
        serviceName = new QName(ws.targetNamespace(), ws.name());
        initFixture();
    }
    
    
    public void testJBIClientTransport() {
        

        JBIClientTransport ct = new JBIClientTransport(channel, endpointRef, clientBinding);
        assertNotNull("server transport must not be null", ct);
        assertSame("transport must JBIClientTransport", JBIClientTransport.class, ct.getClass());
        EasyMock.verify(clientBinding);
        
    }

 
    public void testInvokeOneway() {

    }
    
      
    public void testinvoke() throws Exception {

        MessageExchangeFactory factory = EasyMock.createMock(MessageExchangeFactory.class);
        InOut exchange = EasyMock.createMock(InOut.class);
        NormalizedMessage message = EasyMock.createMock(NormalizedMessage.class); 
        ByteArrayInputStream messageStream = new ByteArrayInputStream(TEST_MESSAGE.getBytes());
        
        channel.createExchangeFactoryForService(serviceName); 
        EasyMock.expectLastCall().andReturn(factory);
        factory.createInOutExchange();
        EasyMock.expectLastCall().andReturn(exchange);
        exchange.createMessage();
        EasyMock.expectLastCall().andReturn(message);
        exchange.getEndpoint();
        EasyMock.expectLastCall().andReturn(null);
        message.setContent((Source)EasyMock.notNull());
        exchange.setService(serviceName);
        exchange.setInterfaceName(new QName("http://objectweb.org/hello_world_soap_http", "Greeter"));
        exchange.setOperation(new QName(targetMethod.getName()));
        exchange.setInMessage(message); 
        channel.sendSync(exchange);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        exchange.getOutMessage();                        
        EasyMock.expectLastCall().andReturn(message);
        message.getContent();
        EasyMock.expectLastCall().andReturn(new StreamSource(messageStream));
                                
        EasyMock.replay(channel);
        EasyMock.replay(factory);
        EasyMock.replay(exchange);
        EasyMock.replay(message);
        
        InputStreamMessageContext ret =  clientTransport.invoke(outCtx);
        assertNotNull("invoke must not return null", ret);
        assertNotNull("invoke must not return an emtpy context", ret.getInputStream());

        EasyMock.verify(channel);
        EasyMock.verify(factory);
        EasyMock.verify(exchange);
        EasyMock.verify(message);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(ret.getInputStream()));
        // throw away prolog
        String s = reader.readLine();   
        assertNotNull(s);
        // chop off the XML declaration
        
        assertTrue(s.startsWith(XML_DECLARATION));
        String result = s.substring(XML_DECLARATION.length());
        assertEquals("returned message incorrect", TEST_MESSAGE, result);
    }

 
    public void testGetMessageContent() throws IOException {

        Source ret = clientTransport.getMessageContent(outCtx);
        
        assertNotNull(ret);
        assertEquals("incorrect return type", StreamSource.class, ret.getClass());
        InputStream in = ((StreamSource)ret).getInputStream();
        assertNotNull(in);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        assertEquals(TEST_MESSAGE, reader.readLine());     
    }

  
    public void testInvokeAsync() {

    }

 
    public void testFinalPrepareOutputStreamContext() {

    }

   
    public void testShutdown() {

    }

 
    public void testCreateOutputStreamContext() throws IOException {

        MessageContext messageContext = EasyMock.createMock(MessageContext.class);
        OutputStreamMessageContext ret = clientTransport.createOutputStreamContext(messageContext);
        
        assertNotNull("OutputStreamMessageContext must not be null", ret);
        assertEquals("OutputStreamMessageContext of incorrect type", 
                     JBIOutputStreamMessageContext.class, ret.getClass());
    }


    private void initFixture() {
        
        EasyMock.reset(clientBinding);
        
        endpointRef = new EndpointReferenceType();
        EndpointReferenceUtils.setServiceAndPortName(endpointRef,
                                                     new QName("http://objectweb.org/hello_world_soap_http", 
                                                               "Greeter"),
                                                               "SOAPPort");  
        ResponseCallback responseCallback = EasyMock.createMock(ResponseCallback.class);
        clientBinding.createResponseCallback();
        EasyMock.expectLastCall().andReturn(responseCallback);
        EasyMock.replay(clientBinding);

    }
}
