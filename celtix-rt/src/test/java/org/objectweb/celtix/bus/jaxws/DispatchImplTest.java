package org.objectweb.celtix.bus.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bus.bindings.soap.SOAPClientBinding;
import org.objectweb.celtix.bus.bindings.soap.TestOutputStreamContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class DispatchImplTest<T> extends TestCase {
    
    Bus bus;
    EndpointReferenceType epr;

    public DispatchImplTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDispatchImpl() throws Exception {
        

        DispatchImpl dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class);
        
        assertNotNull(dispImpl);
    }

    @SuppressWarnings("unchecked")
    public void testGetRequestContext() throws Exception {
        
        DispatchImpl dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class);
        
        Map m = dispImpl.getRequestContext();
        
        assertNotNull(m);   
    }

    @SuppressWarnings("unchecked")
    public void testGetResponseContext() throws Exception {
        DispatchImpl dispImpl = 
            new DispatchImpl<SOAPMessage>(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class);
        
        Map m = dispImpl.getResponseContext();
        
        assertNotNull(m);
    }

    @SuppressWarnings("unchecked")
    public void testInvoke() throws Exception {
        
        InputStream is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        TestDispatchImpl dispImpl = 
            new TestDispatchImpl(bus, epr, Service.Mode.MESSAGE, SOAPMessage.class);
        SOAPMessage soapRespMsg = (SOAPMessage)dispImpl.invoke(soapReqMsg);
        assertNotNull(soapRespMsg);
        assertEquals("Message should contain TestSOAPInputMessage",
                     soapRespMsg.getSOAPBody().getTextContent(), "TestSOAPInputMessage");    
    }
    
    class TestDispatchImpl extends DispatchImpl {
        
        private Mode mode;
        private Class<T> cl;

        @SuppressWarnings("unchecked")
        TestDispatchImpl(Bus b, EndpointReferenceType r, Service.Mode m, Class clazz) {
            super(b, r, m, clazz);
            mode = Mode.fromServiceMode(m);
            cl = clazz;
        }
        
        @SuppressWarnings("unchecked")
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
        
        private MessageContext ctx;
        
        public TestClientTransport(Bus mybus, EndpointReferenceType ref) 
            throws WSDLException, IOException {       
        }

        public void invokeOneway(OutputStreamMessageContext context) throws IOException {
            
        }

        public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
            return ((TestOutputStreamContext)context).createInputStreamContext();
        }

        public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                             Executor executor) 
            throws IOException {
            return null;
        }
        
        public MessageContext getTestMessageContext() {
            return ctx;
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
    }

}
