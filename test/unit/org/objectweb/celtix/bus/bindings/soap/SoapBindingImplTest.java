package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import junit.framework.TestCase;

import org.objectweb.celtix.bindings.ObjectMessageContextImpl;
import org.objectweb.hello_world_soap_http.Greeter;

public class SoapBindingImplTest extends TestCase {
    private SOAPBindingImpl binding;
    private ObjectMessageContextImpl objContext;
    private SOAPMessageContextImpl soapContext;
    public SoapBindingImplTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapBindingImplTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        binding = new SOAPBindingImpl();
        objContext = new ObjectMessageContextImpl();
        soapContext = new SOAPMessageContextImpl();
        
        Method[] declMethods = Greeter.class.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals("greetMe")) {
                objContext.setMethod(method);
            }
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMarshalWrapDocLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);

        String arg0 = new String("TestSOAPInputPMessage");
        objContext.setMessageObjects(arg0);

        SOAPMessage msg = binding.marshalMessage(objContext, soapContext);
        soapContext.setMessage(msg);
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(arg0, wrappedNode.getFirstChild().getNodeValue());
    }

    public void testMarshalWrapDocLitOutputMessage() throws Exception {
        //Test The Output of GreetMe Operation
        soapContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);

        String arg0 = new String("TestSOAPOutputMessage");
        objContext.setReturn(arg0);
        
        SOAPMessage msg = binding.marshalMessage(objContext, soapContext);
        soapContext.setMessage(msg);
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(arg0, wrappedNode.getFirstChild().getNodeValue());
    }

    public void testParseWrapDocLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        //Assumption the Wrapper element and the inner element are in the same namespace
        //elementFormDefault is qualified
        
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");
        String str = createWrapDocLitSOAPMessage(wrapName, elName, data);        
        
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        binding.parseInputMessage(in, soapContext);

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();

        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(data, wrappedNode.getFirstChild().getNodeValue());
    }

    public void testUnmarshalWrapDocLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");        
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");
        String str = createWrapDocLitSOAPMessage(wrapName, elName, data);
        
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        soapContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);

        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        binding.unmarshalMessage(soapContext, objContext);
        
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(1, params.length);
        assertEquals(data, (String)params[0]);
    }    

    public void testUnmarshalDocLiteralOutputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");        
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPOutputMessage");
        String str = createWrapDocLitSOAPMessage(wrapName, elName, data);
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());

        soapContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);        
        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        binding.unmarshalMessage(soapContext, objContext);
        
        Object[] params = objContext.getMessageObjects();
        //REVISIT Should it be null;
        assertNotNull(params);
        assertEquals(0, params.length);
        assertNotNull(objContext.getReturn());
        assertEquals(data, (String)objContext.getReturn());
    }    

    private String createWrapDocLitSOAPMessage(QName wrapName, QName elName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        str.append("<SOAP-ENV:Body>");

        str.append("<" + wrapName.getLocalPart() + " xmlns=\"" + wrapName.getNamespaceURI() + "\">");
        str.append("<" + elName.getLocalPart() + ">");
        str.append(data);
        str.append("</" + elName.getLocalPart() + ">");
        str.append("</" + wrapName.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
}
