package org.objectweb.celtix.bus.bindings.soap;

//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.Future;

import javax.wsdl.WSDLException;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
//import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
//import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import org.objectweb.hello_world_soap_http.Greeter;

public class SOAPClientBindingTest extends TestCase {
    Bus bus;
    EndpointReferenceType epr;
    
    public SOAPClientBindingTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SOAPClientBindingTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        epr = new EndpointReferenceType();
        
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetBinding() throws Exception {
        SOAPClientBinding clientBinding = new SOAPClientBinding(bus, epr);
        assertNotNull(clientBinding.getBinding());
    }

    public void testCreateObjectContext() throws Exception {
        SOAPClientBinding clientBinding = new SOAPClientBinding(bus, epr);
        assertNotNull(clientBinding.createObjectContext());
    }
    
    public void testInvoke() throws Exception {
        TestClientBinding clientBinding = new TestClientBinding(bus, epr);
        ObjectMessageContext objContext = clientBinding.createObjectContext();
        assertNotNull(objContext);
        Method[] declMethods = Greeter.class.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals("greetMe")) {
                objContext.setMethod(method);
            }
        }
        String arg0 = new String("TestSOAPInputPMessage");
        objContext.setMessageObjects(arg0);
        
        objContext = clientBinding.invoke(objContext);        
        assertNotNull(objContext);
        assertEquals(arg0, (String)objContext.getReturn());
    }

    class TestClientBinding extends SOAPClientBinding {

        public TestClientBinding(Bus b, EndpointReferenceType ref) 
            throws WSDLException, IOException {
            super(b, ref);
        }

        protected ClientTransport createTransport(EndpointReferenceType ref)
            throws WSDLException, IOException {
            return new TestClientTransport(bus, ref);
        }
    }

    class TestClientTransport implements ClientTransport {
        public TestClientTransport(Bus b, EndpointReferenceType ref) {
        }

        public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
            throws IOException {
            return new TestOutputStreamContext(null, context);
        }

        public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        }

        public void invokeOneway(OutputStreamMessageContext context) throws IOException {
            //nothing to do
        }

        public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
            TestOutputStreamContext ctx = (TestOutputStreamContext)context;
            return ctx.createInputStreamContext();
        }

        public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context) 
            throws IOException {
            return null;
        }

        public void shutdown() {
            //nothing to do
        }
    }
    
    
}
