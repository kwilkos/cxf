package org.objectweb.celtix.bus.bindings.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.TestCase;
import org.objectweb.hello_world_soap_http.Greeter;
/**
 * JAXBEncoderDecoderTest
 * @author apaibir
 */
public class JAXBEncoderDecoderTest extends TestCase {

    public JAXBEncoderDecoderTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JAXBEncoderDecoderTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
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
        } catch (SOAPException ex) {
            //Expected Exception
        }

        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbEncoder = new JAXBEncoderDecoder(Greeter.class.getPackage().getName());
        
        jaxbEncoder.marshall(str, inCorrectElName,  elNode);
        assertTrue(elNode.hasChildNodes());
        Node node = elNode.getFirstChild();
        assertEquals(Node.TEXT_NODE, node.getNodeType());        
        assertEquals(str, node.getNodeValue());
        
        str = new String("GreetMe");
        QName elName = new QName("http://objectweb.org/hello_world_soap_http", "responseType");
        jaxbEncoder.marshall(str, elName, elNode);
        node = elNode.getLastChild();
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());        
        assertEquals(Node.TEXT_NODE, node.getFirstChild().getNodeType()); 
        assertEquals(str, node.getFirstChild().getNodeValue());
    }

    public void testUnMarshall() throws Exception {
        //Hello World Wsdl generated namespace
        JAXBEncoderDecoder jaxbDecoder = new JAXBEncoderDecoder(Greeter.class.getPackage().getName());
        QName elName = new QName("http://objectweb.org/hello_world_soap_http", "responseType");        

        SOAPFactory soapElFactory = SOAPFactory.newInstance();

        SOAPElement elNode = soapElFactory.createElement(elName);
        String str = new String("Hello Test");
        elNode.setValue(str);

        Object obj = jaxbDecoder.unmarshall(elNode, elName);
        assertNotNull(obj);
            
        //Add a Node and then test
        assertEquals(String.class,  obj.getClass());
        assertEquals(str, (String)obj);
    }
}

