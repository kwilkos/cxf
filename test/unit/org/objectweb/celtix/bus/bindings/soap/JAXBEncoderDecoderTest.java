package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
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

        Method methods[] = Greeter.class.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("greetMe")) {
                wrapperAnnotation = method.getAnnotation(RequestWrapper.class);
            }
        }
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

        try {
            JAXBEncoderDecoder test = new JAXBEncoderDecoder((String) null);
            fail("Should have thrown a exception");
            assert test == null;
        } catch (SOAPException ex) {
            //Expected Exception
        }

        String packageName = wrapperAnnotation.className();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        try {
            JAXBContext context = JAXBContext.newInstance(String.class);
            // Need to "read" from it to avoid a warning
            context.getClass();
        } catch (Exception ex) {
            throw new SOAPException("Could not create JAXB Context", ex);
        }
        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbEncoder = new JAXBEncoderDecoder(packageName);
        Node node;
        try {
            jaxbEncoder.marshall(str, inCorrectElName,  elNode);
        } catch (Exception ex) {
            //expected - not a valid qname or anything
        }

        GreetMe obj = new GreetMe();
        obj.setRequestType("Hello");
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());
        jaxbEncoder.marshall(obj, elName, elNode);
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
        
        JAXBEncoderDecoder jaxbEncoder = new JAXBEncoderDecoder(String.class);
        
        jaxbEncoder.marshall(new String("TestSOAPMessage"), elName,  elNode);
        
        assertNotNull(elNode.getChildNodes());
        assertEquals("TestSOAPMessage", elNode.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testUnMarshall() throws Exception {
        //Hello World Wsdl generated namespace
        String packageName = wrapperAnnotation.className();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));

        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbDecoder = new JAXBEncoderDecoder(packageName);
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());

        //Create a XML Tree of
        //<GreetMe><requestType>Hello</requestType></GreetMe>
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        SOAPElement elNode = soapElFactory.createElement(elName);

        String str = new String("Hello Test");
        elNode.addChildElement("requestType").setValue(str);

        Object obj = jaxbDecoder.unmarshall(elNode, elName);
        assertNotNull(obj);

        //Add a Node and then test
        assertEquals(GreetMe.class,  obj.getClass());
        assertEquals(str, ((GreetMe)obj).getRequestType());
    }  
}

