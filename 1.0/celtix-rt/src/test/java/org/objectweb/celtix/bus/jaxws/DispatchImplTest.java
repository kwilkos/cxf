package org.objectweb.celtix.bus.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.bus.bindings.TestOutputStreamContext;
import org.objectweb.celtix.bus.bindings.soap.SOAPClientBinding;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public class DispatchImplTest<T> extends TestCase {
    
    Bus bus;
    EndpointReferenceType epr;
    Executor executor;

    public DispatchImplTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");
        executor = bus.getWorkQueueManager().getAutomaticWorkQueue();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        bus.shutdown(true);
        bus = null;
        executor = null;
        epr = null;
    }

    public void testDispatchImpl() throws Exception {
        

        DispatchImpl dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        
        assertNotNull(dispImpl);
    }

    public void testGetRequestContext() throws Exception {
        
        DispatchImpl<SOAPMessage> dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        
        Map<String, Object> m = dispImpl.getRequestContext();
        
        assertNotNull(m);   
    }

    public void testGetResponseContext() throws Exception {
        DispatchImpl<SOAPMessage> dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        
        Map<String, Object> m = dispImpl.getResponseContext();
        
        assertNotNull(m);
    }

    public void testInvoke() throws Exception {
        
        InputStream is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl<SOAPMessage> dispImpl = 
            new TestDispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        SOAPMessage soapRespMsg = dispImpl.invoke(soapReqMsg);
        assertNotNull(soapRespMsg);
        assertEquals("Message should contain TestSOAPInputMessage",
                     soapRespMsg.getSOAPBody().getTextContent(), "TestSOAPInputMessage");    
    }

    public void testInvokeForFaults() throws Exception {
        
        InputStream is =  
            getClass().getResourceAsStream("../bindings/soap/resources/BadRecordDocLiteral.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl<SOAPMessage> dispImpl = 
            new TestDispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        try {
            dispImpl.invoke(soapReqMsg);
            fail("Expecting a instance of ProtocolException");
        } catch (ProtocolException pe) {
            assertTrue("Should be instance of SOAPFaultException", pe instanceof SOAPFaultException);
            SOAPFaultException sfe = (SOAPFaultException)pe;
            assertNotNull("Should have a details obj", sfe.getFault());
            assertEquals(
                         new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), 
                         sfe.getFault().getFaultCodeAsQName());
            assertEquals("Test Exception", sfe.getFault().getFaultString());
        }
        is.close();
    }
    
    public void testInvokeOneWay() throws Exception {
        
        InputStream is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl<SOAPMessage> dispImpl = 
            new TestDispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        dispImpl.invokeOneWay(soapReqMsg);   
    }
    
    public void testInvokeAsync() throws Exception {
        
        InputStream is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl<SOAPMessage> dispImpl = 
            new TestDispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        Response response = dispImpl.invokeAsync(soapReqMsg);
        assertNotNull(response);        
        SOAPMessage soapRespMsg = (SOAPMessage)response.get();
        assertEquals("Message should contain TestSOAPInputMessage",
                     soapRespMsg.getSOAPBody().getTextContent(), "TestSOAPInputMessage"); 
    }
    
    public void testInvokeAsyncCallback() throws Exception {
        
        InputStream is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl<SOAPMessage> dispImpl = 
            new TestDispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class, executor);
        TestHandler testHandler = new TestHandler();
        Future<?> future = dispImpl.invokeAsync(soapReqMsg, testHandler);
        assertNotNull(future);   
        while (!future.isDone()) {
            //wait till done
        }        
        assertEquals("Message should contain TestSOAPInputMessage",
                     testHandler.getReplyBuffer(), "TestSOAPInputMessage"); 
    }
    
    class TestDispatchImpl<X> extends DispatchImpl<X> {
        
        private Mode mode;
        private Class<X> cl;

        TestDispatchImpl(Bus b, EndpointReferenceType r, Service.Mode m, Class<X> clazz, Executor e) {
            super(b, r, m, clazz, e);
            mode = Mode.fromServiceMode(m);
            cl = clazz;
        }
        
        protected void init() {
            try {
                cb = new TestClientBinding(bus, epr);
                callback = new DynamicDataBindingCallback(cl, mode);  
            } catch (WSDLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public TestClientBinding getTestClientBinding() {
            return (TestClientBinding)cb;
        }
        
    }
    
    class TestClientBinding extends SOAPClientBinding {

        private TestClientTransport testClientTransport;
        private Bus bus;
        private EndpointReferenceType epr;
        
        public TestClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
            super(b, ref);
            bus = b;
            epr = ref;
        }

        protected ClientTransport createTransport(EndpointReferenceType ref) 
            throws WSDLException, IOException {
            testClientTransport = new TestClientTransport(bus, epr);
            return testClientTransport;
        }
        
        public ClientTransport getTestTransport() {
            return testClientTransport;
        }
        
    }
    
    class TestClientTransport implements ClientTransport {
        
        public TestClientTransport(Bus mybus, EndpointReferenceType ref) 
            throws WSDLException, IOException {       
        }

        public void invokeOneway(OutputStreamMessageContext context) throws IOException {
            InputStreamMessageContext ismc = context.getCorrespondingInputStreamContext();
            InputStream in = ismc.getInputStream();            
            try {
                SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, in);
                assertEquals("Message should contain TestSOAPInputMessage",
                             soapMessage.getSOAPBody().getTextContent(), 
                             "TestSOAPInputMessage");                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SOAPException e) {
                e.printStackTrace();
            }
            
        }

        public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
            return context.getCorrespondingInputStreamContext();
            
        }

        public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                             Executor e) throws IOException {
            InputStreamMessageContext ismc = context.getCorrespondingInputStreamContext();
            return new TestInputStreamMessageContextFuture(ismc);
        }

        public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
            throws IOException {
            return new TestOutputStreamContext(null, context);
        }

        public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
            // TODO Auto-generated method stub
            
        }

        public void shutdown() {
            // TODO Auto-generated method stub
            
        }

        public EndpointReferenceType getDecoupledEndpoint() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public Port getPort() {
            // TODO Auto-generated method stub
            return null;
        }

        public EndpointReferenceType getTargetEndpoint() {
            // TODO Auto-generated method stub
            return null;
        }
        
        public ResponseCallback getResponseCallback() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    class TestInputStreamMessageContextFuture
        implements Future<InputStreamMessageContext> {
        
        private InputStreamMessageContext inputStreamMessageContext;
        
        public TestInputStreamMessageContextFuture(InputStreamMessageContext ismc) {
            inputStreamMessageContext = ismc;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isCancelled() {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isDone() {
            // TODO Auto-generated method stub
            return false;
        }

        public InputStreamMessageContext get() throws InterruptedException, ExecutionException {
            return inputStreamMessageContext;         
        }

        public InputStreamMessageContext get(long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
        
    }
    
    class TestHandler implements AsyncHandler<SOAPMessage> {   
        
        String replyBuffer;
        
        public void handleResponse(Response<SOAPMessage> response) {
            try {
                SOAPMessage reply = response.get();
                replyBuffer = reply.getSOAPBody().getTextContent();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        } 
        
        public String getReplyBuffer() {
            return replyBuffer;
        }
    }

}
