package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
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

    public void testMarshalDocLiteralInputMessage() throws Exception {
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
        assertEquals(arg0, list.item(0).getFirstChild().getNodeValue());
    }
    
    public void testMarshalDocLiteralOutputMessage() throws Exception {
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
        assertEquals(arg0, list.item(0).getFirstChild().getNodeValue());
    }
    
    public void testParseDocLiteralInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        StringBuffer str = new StringBuffer();
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        str.append("<SOAP-ENV:Body>");
        str.append("<requestType xmlns=\"http://objectweb.org/hello_world_soap_http\">");
        str.append("TestSOAPInputPMessage");
        str.append("</requestType>");
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");

        ByteArrayInputStream in = new ByteArrayInputStream(str.toString().getBytes());
        binding.parseInputMessage(in, soapContext);

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        
        String arg0 = new String("TestSOAPInputPMessage");
        assertEquals(arg0, list.item(0).getFirstChild().getNodeValue());
    }
    
    public void testUnmarshalDocLiteralInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName elName = new QName("http://objectweb.org/hello_world_soap_http", "requestType");
        String data = new String("TestSOAPInputMessage");
        String str = createSOAPMessage(elName, data);        
        
        ByteArrayInputStream in = new ByteArrayInputStream(str.toString().getBytes());
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
        QName elName = new QName("http://objectweb.org/hello_world_soap_http", "responseType");
        String data = new String("TestSOAPOutputMessage");
        String str = createSOAPMessage(elName, data);
        ByteArrayInputStream in = new ByteArrayInputStream(str.toString().getBytes());

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
    
    private String createSOAPMessage(QName elementName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        str.append("<SOAP-ENV:Body>");
        
        str.append("<" + elementName.getLocalPart() + " xmlns=\"" + elementName.getNamespaceURI() + "\">");
        str.append(data);
        str.append("</" + elementName.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
}
