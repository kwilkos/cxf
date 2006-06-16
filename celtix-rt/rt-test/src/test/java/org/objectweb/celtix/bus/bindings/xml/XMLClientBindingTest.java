package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.lang.reflect.Method;
import javax.wsdl.WSDLException;

import org.w3c.dom.*;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.bindings.TestClientTransport;
import org.objectweb.celtix.bus.bindings.TestInputStreamContext;
import org.objectweb.celtix.bus.bindings.TestOutputStreamContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;

public class XMLClientBindingTest extends TestCase {
    Bus bus;
    EndpointReferenceType epr;
    XMLUtils xmlUtils;
    
    public XMLClientBindingTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLClientBindingTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        
        xmlUtils = new XMLUtils();
        TestUtils testUtils = new TestUtils();

        epr = testUtils.getWrappedReference();
    }

    public void testGetBinding() throws Exception {
        XMLClientBinding clientBinding = new XMLClientBinding(bus, epr);
        assertNotNull(clientBinding.getBinding());
    }

    public void testCreateObjectContext() throws Exception {
        XMLClientBinding clientBinding = new XMLClientBinding(bus, epr);
        assertNotNull(clientBinding.createObjectContext());
    }

    public void testRead() throws Exception {
        TestClientBinding clientBinding = new TestClientBinding(bus, epr);
        InputStream is =  getClass().getResourceAsStream("resources/SayHiWrappedResp.xml");
        TestInputStreamContext tisc = new TestInputStreamContext(null);
        tisc.setInputStream(is);
        XMLMessageContext xmlContext = new XMLMessageContextImpl(new GenericMessageContext());        
        clientBinding.getBindingImpl().read(tisc,  xmlContext);

        assertNotNull(xmlContext.getMessage());

        is =  getClass().getResourceAsStream("resources/SayHiWrappedResp.xml");
        Document expectDOM = xmlUtils.parse(is);
        assertNotNull(expectDOM);
        Document resultDOM = xmlContext.getMessage().getRoot();
        assertNotNull(resultDOM);
        is.close();

        assertTrue(expectDOM.isEqualNode(resultDOM));
    }

    public void testWrite() throws Exception {
        TestClientBinding clientBinding = new TestClientBinding(bus, epr);
        String resource = "resources/SayHiWrappedReq.xml";
        InputStream is =  getClass().getResourceAsStream(resource);
        assertNotNull(resource + " is not exist.", is);
        
        XMLMessageFactory msgFactory = XMLMessageFactory.newInstance();
        XMLMessage greetMeMsg = msgFactory.createMessage(is);
        is.close();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().
                                                                     getResourceAsStream(resource)));
        String expectString = br.readLine();
        
        XMLMessageContext xmlContext = new XMLMessageContextImpl(new GenericMessageContext());
        xmlContext.setMessage(greetMeMsg);
        
        TestOutputStreamContext tosc = new TestOutputStreamContext(null, xmlContext);
        clientBinding.getBindingImpl().write(xmlContext, tosc);

        byte[] bArray = tosc.getOutputStreamBytes();

        Document expectDOM = xmlUtils.parse(expectString);
        Document resultDOM = xmlUtils.parse(bArray);

        assertTrue(expectDOM.isEqualNode(resultDOM));
    }

    public void testInvokeOneWay() throws Exception {
        TestClientBinding clientBinding = new TestClientBinding(bus, epr);
        ObjectMessageContext objContext = clientBinding.createObjectContext();
        assertNotNull(objContext);
        
        Method method = ClassUtils.getMethod(Greeter.class, "greetMeOneWay");
        
        String arg0 = new String("TestXMLInputMessage");
        objContext.setMessageObjects(arg0);
        
        clientBinding.invokeOneWay(objContext,
                                   new JAXBDataBindingCallback(method,
                                                               DataBindingCallback.Mode.PARTS,
                                                               null));
    }
    
    class TestClientBinding extends XMLClientBinding {

        public TestClientBinding(Bus b, EndpointReferenceType ref) 
            throws WSDLException, IOException {
            super(b, ref);
        }

        protected ClientTransport createTransport(EndpointReferenceType ref)
            throws WSDLException, IOException {
            // REVISIT: non-null response callback
            return new TestClientTransport(bus, ref);
        }

    }
}
