package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;

public class SoapBindingImplRPCLitTest extends TestCase {

    private SOAPBindingImpl binding;
    private ObjectMessageContextImpl objContext;
    private SOAPMessageContextImpl soapContext;

    public SoapBindingImplRPCLitTest(String arg0) {
        super(arg0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapBindingImplRPCLitTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        binding = new SOAPBindingImpl();
        objContext = new ObjectMessageContextImpl();
        soapContext = new SOAPMessageContextImpl(new GenericMessageContext());
        
        Method greetMe = SOAPMessageUtil.getMethod(GreeterRPCLit.class, "greetMe");
        objContext.setMethod(greetMe);
    }

    public void testMarshalRPCLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);

        String arg0 = new String("TestSOAPInputPMessage");
        objContext.setMessageObjects(arg0);

        SOAPMessage msg = binding.marshalMessage(objContext, soapContext,
                                                 new JAXBDataBindingCallback(objContext.getMethod(),
                                                                             DataBindingCallback.Mode.PARTS));
        //msg.writeTo(System.out);
        soapContext.setMessage(msg);
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node operationNode = list.item(0);
        assertEquals(objContext.getMethod().getName(), operationNode.getLocalName());
 
        WebService wsAnnotation = objContext.getMethod().getDeclaringClass().getAnnotation(WebService.class);
        String expectedNameSpace = wsAnnotation.targetNamespace();
        assertTrue(expectedNameSpace.equals(operationNode.getNamespaceURI()));
 
        assertTrue(operationNode.hasChildNodes());
        assertEquals(arg0, operationNode.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testMarshalRPCLitOutputMessage() throws Exception {
        //Test The Output of GreetMe Operation
        soapContext.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);

        String arg0 = new String("TestSOAPOutputMessage");
        objContext.setReturn(arg0);

        SOAPMessage msg = binding.marshalMessage(objContext, soapContext,
                                                 new JAXBDataBindingCallback(objContext.getMethod(),
                                                                             DataBindingCallback.Mode.PARTS));
        soapContext.setMessage(msg);
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node operationNode = list.item(0);
        assertEquals(objContext.getMethod().getName() + "Response", operationNode.getLocalName());
        
        WebService wsAnnotation = objContext.getMethod().getDeclaringClass().getAnnotation(WebService.class);
        String expectedNameSpace = wsAnnotation.targetNamespace();
        assertTrue(expectedNameSpace.equals(operationNode.getNamespaceURI()));
        
        assertTrue(operationNode.hasChildNodes());
        assertEquals(arg0, operationNode.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testParseRPCLitInputMessage() throws Exception {

        QName opName = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_rpclit", "in");
        String data = new String("TestSOAPInputMessage");
        String str = SOAPMessageUtil.createRPCLitSOAPMessage(opName, elName, data);

        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        binding.parseMessage(in, soapContext);

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();

        assertEquals(1, list.getLength());
        Node operationNode = list.item(0);
        assertEquals(objContext.getMethod().getName(), operationNode.getLocalName());
        assertTrue(operationNode.hasChildNodes());
        assertEquals(data, operationNode.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testUnmarshalRPCLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName opName = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_rpclit", "in");
        String data = new String("TestSOAPInputMessage");
        String str = SOAPMessageUtil.createRPCLitSOAPMessage(opName, elName, data);

        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        soapContext.put(ObjectMessageContextImpl.MESSAGE_INPUT, false);

        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);
        //GreetMe has a IN parameter
        objContext.setMessageObjects(new Object[]{null});

        binding.unmarshalMessage(soapContext, objContext,
                                 new JAXBDataBindingCallback(objContext.getMethod(),
                                                             DataBindingCallback.Mode.PARTS));

        assertNull(objContext.getReturn());
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(data, (String)params[0]);
    }

    public void testUnmarshalRPCLitOutputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName opName = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_rpclit", "out");
        String data = new String("TestSOAPOutputMessage");
        String str = SOAPMessageUtil.createRPCLitSOAPMessage(opName, elName, data);
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());

        soapContext.put(ObjectMessageContextImpl.MESSAGE_INPUT, true);
        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        binding.unmarshalMessage(soapContext, objContext,
                                 new JAXBDataBindingCallback(objContext.getMethod(),
                                                             DataBindingCallback.Mode.PARTS));

        assertNull(objContext.getMessageObjects());
        assertNotNull(objContext.getReturn());
        assertEquals(data, (String)objContext.getReturn());
    }

}
