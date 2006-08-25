package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.type_test.TypeTestPortType;
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

    public void testMarshall() throws Exception {
        String str = new String("Hello");
        QName inCorrectElName = new QName("http://test_jaxb_marshall", "requestType");
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        Element elNode = soapElFactory.createElement(inCorrectElName);
        assertNotNull(elNode);

        Node node;
        try {
            JAXBEncoderDecoder.marshall(null, inCorrectElName,  elNode);
            fail("Should have thrown a WebServiceException");
        } catch (WebServiceException ex) {
            //expected - not a valid object
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
        elNode.addNamespaceDeclaration("", elName.getNamespaceURI()); 

        String str = new String("Hello Test");
        elNode.addChildElement("requestType").setValue(str);

        Object obj = JAXBEncoderDecoder.unmarshall(
                         elNode, elName, Class.forName(wrapperAnnotation.className()));
        assertNotNull(obj);

        //Add a Node and then test
        assertEquals(GreetMe.class,  obj.getClass());
        assertEquals(str, ((GreetMe)obj).getRequestType());
        
        try {
            JAXBEncoderDecoder.unmarshall(null, null, null);
            fail("Should have received a WebServiceException");
        } catch (WebServiceException wex) {
            //Expected Exception
        }
    } 
    
    public void testGetClassFromType() throws Exception {
        Method testByte = SOAPMessageUtil.getMethod(TypeTestPortType.class, "testByte");
        Type[] genericParameterTypes = testByte.getGenericParameterTypes();
        Class<?>[] paramTypes = testByte.getParameterTypes();
 
        int idx = 0;
        for (Type t : genericParameterTypes) {
            Class<?> cls = JAXBEncoderDecoder.getClassFromType(t);
            assertTrue(cls.equals(paramTypes[idx]));
            idx++;
        }
        
        Method testBase64Binary = SOAPMessageUtil.getMethod(TypeTestPortType.class, "testBase64Binary");
        genericParameterTypes = testBase64Binary.getGenericParameterTypes();
        paramTypes = testBase64Binary.getParameterTypes();
 
        idx = 0;
        for (Type t : genericParameterTypes) {
            Class<?> cls = JAXBEncoderDecoder.getClassFromType(t);
            assertTrue(cls.equals(paramTypes[idx]));
            idx++;
        }
        
        
    }
}

