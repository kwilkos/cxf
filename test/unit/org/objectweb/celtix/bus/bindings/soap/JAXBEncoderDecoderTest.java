package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.xml.ws.RequestWrapper;
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
            JAXBEncoderDecoder test = new JAXBEncoderDecoder(null);
            fail("Should have thrown a exception");
            assert test == null;
        } catch (SOAPException ex) {
            //Expected Exception
        }

        String packageName = wrapperAnnotation.type();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        
        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbEncoder = new JAXBEncoderDecoder(packageName);
        
        jaxbEncoder.marshall(str, inCorrectElName,  elNode);
        assertTrue(elNode.hasChildNodes());
        Node node = elNode.getFirstChild();
        assertEquals(Node.TEXT_NODE, node.getNodeType());        
        assertEquals(str, node.getNodeValue());
        
        GreetMe obj = new GreetMe();
        obj.setRequestType("Hello");
        QName elName = new QName(wrapperAnnotation.namespace(), wrapperAnnotation.name());
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

    public void testUnMarshall() throws Exception {
        //Hello World Wsdl generated namespace
        String packageName = wrapperAnnotation.type();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        
        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbDecoder = new JAXBEncoderDecoder(packageName);
        QName elName = new QName(wrapperAnnotation.namespace(), wrapperAnnotation.name());

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

