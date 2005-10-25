package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.RequestWrapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.TestCase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
/**
 * JAXBEncoderDecoderTest
 * @author apaibir
 */
public class JAXBEncoderDecoderTest extends TestCase {
    RequestWrapper wrapperAnnotation;
    public JAXBEncoderDecoderTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JAXBEncoderDecoderTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Method method = SOAPMessageUtil.getMethod(Greeter.class, "greetMe");
        wrapperAnnotation = method.getAnnotation(RequestWrapper.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMarshall() throws Exception {
        String str = new String("Hello");
        QName inCorrectElName = new QName("http://test_jaxb_marshall", "requestType");
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        Element elNode = soapElFactory.createElement(inCorrectElName);
        assertNotNull(elNode);

        Node node;
        try {
            JAXBEncoderDecoder.marshall(str, inCorrectElName,  elNode);
        } catch (Exception ex) {
            //expected - not a valid qname or anything
        }

        GreetMe obj = new GreetMe();
        obj.setRequestType("Hello");
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());
        JAXBEncoderDecoder.marshall(obj, elName, elNode);
        node = elNode.getLastChild();
        //The XML Tree Looks like
        //<GreetMe><requestType>Hello</requestType></GreetMe>
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        Node childNode = node.getFirstChild();
        assertEquals(Node.ELEMENT_NODE, childNode.getNodeType());
        childNode = childNode.getFirstChild();
        assertEquals(Node.TEXT_NODE, childNode.getNodeType());
        assertEquals(str, childNode.getNodeValue());
    }
    
    public void testMarshalRPCLit() throws Exception {
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        QName elName = new QName("http://test_jaxb_marshall", "in");
        SOAPElement elNode = soapElFactory.createElement(elName);
        JAXBEncoderDecoder.marshall(new String("TestSOAPMessage"), elName,  elNode);
        
        assertNotNull(elNode.getChildNodes());
        assertEquals("TestSOAPMessage", elNode.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testUnMarshall() throws Exception {
        //Hello World Wsdl generated namespace
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());

        //Create a XML Tree of
        //<GreetMe><requestType>Hello</requestType></GreetMe>
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        SOAPElement elNode = soapElFactory.createElement(elName);

        String str = new String("Hello Test");
        elNode.addChildElement("requestType").setValue(str);

        Object obj = JAXBEncoderDecoder.unmarshall(
                         elNode, elName, Class.forName(wrapperAnnotation.className()));
        assertNotNull(obj);

        //Add a Node and then test
        assertEquals(GreetMe.class,  obj.getClass());
        assertEquals(str, ((GreetMe)obj).getRequestType());
    }  
}

