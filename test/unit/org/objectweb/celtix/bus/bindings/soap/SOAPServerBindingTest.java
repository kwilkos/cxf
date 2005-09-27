package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import org.objectweb.hello_world_soap_http.NotAnnotatedGreeterImpl;


public class SOAPServerBindingTest extends TestCase {
    Bus bus;
    EndpointReferenceType epr;
    
    public SOAPServerBindingTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SOAPServerBindingTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        epr = new EndpointReferenceType();

        URL wsdlUrl = getClass().getResource("/org/objectweb/celtix/resources/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetBinding() throws Exception {
        SOAPServerBinding serverBinding = new SOAPServerBinding(bus, epr, null);
        assertNotNull(serverBinding.getBinding());
    }

    public void testCreateObjectContext() throws Exception {
        SOAPServerBinding serverBinding = new SOAPServerBinding(bus, epr, null);
        byte[] bArray = new byte[0];
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        assertNotNull(serverBinding.createBindingMessageContext(inCtx));
    }
    
    public void testDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);

        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");
       
        byte[] bArray = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data).getBytes();
        
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());  
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();
        assertNotNull(os);

        wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse");
        elName = new QName("http://objectweb.org/hello_world_soap_http/types", "responseType");
       
        String ref = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data);
       
        assertEquals(ref, os.toString());
    }

    class TestServerBinding extends SOAPServerBinding {

        public TestServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
            super(b, ref, ep);
        }

        protected ServerTransport createTransport() {
            //return new TestServerTransport(bus, reference);
            return null;
        }

        public void testDispatch(InputStreamMessageContext inCtx, ServerTransport t) {
            super.dispatch(inCtx, t);
        }
    }

    class TestServerTransport implements ServerTransport {
        private OutputStreamMessageContext osmc;
        public TestServerTransport(Bus b, EndpointReferenceType ref) {
        }

        public void shutdown() {
            //nothing to do
        }

        public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
            throws IOException {
            osmc = new TestOutputStreamContext(null, context);
            return osmc;
        }

        public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        }
        
        public void activate(ServerTransportCallback callback) throws IOException { }

        public void deactivate() throws IOException { }   
        
        public OutputStreamMessageContext getOutputStreamContext() {
            return osmc;
        }
    }
    
    class TestEndpointImpl implements javax.xml.ws.Endpoint {

        private Object implementor;

        TestEndpointImpl(Object impl) {
            implementor = impl;
        }

        public Binding getBinding() {
            return null;
        }

        public List<Handler> getHandlerChain() {
            return null;
        }

        public Object getImplementor() {
            return implementor;
        }

        public List<Source> getMetadata() {
            return null;
        }

        public Executor getExecutor() {
            return null;
        }

        public boolean isPublished() {
            return false;
        }

        public void publish(Object serverContext) { }

        public void publish(String address) { }

        public void setHandlerChain(List<Handler> h) {            
        }

        public void setMetadata(List<Source> m) {         
        }

        public void setExecutor(Executor ex) {            
        }

        public void stop() { }
    }
    
}
