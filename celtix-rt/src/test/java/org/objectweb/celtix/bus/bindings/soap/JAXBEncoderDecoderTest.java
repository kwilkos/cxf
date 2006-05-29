package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.RequestWrapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.StringStruct;
import org.objectweb.type_test.doc.TypeTestPortType;

/**
 * JAXBEncoderDecoderTest
 * @author apaibir
 */
public class JAXBEncoderDecoderTest extends TestCase {
    RequestWrapper wrapperAnnotation;
    JAXBContext context;
    Schema schema;
    
    public JAXBEncoderDecoderTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JAXBEncoderDecoderTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        context = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method method = SOAPMessageUtil.getMethod(Greeter.class, "greetMe");
        wrapperAnnotation = method.getAnnotation(RequestWrapper.class);
        
        InputStream is =  getClass().getResourceAsStream("resources/StringStruct.xsd");
        StreamSource schemaSource = new StreamSource(is);
        assertNotNull(schemaSource);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(schemaSource);
        assertNotNull(schema);
    }

    public void testMarshallIntoDOM() throws Exception {
        String str = new String("Hello");
        QName inCorrectElName = new QName("http://test_jaxb_marshall", "requestType");
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        Element elNode = soapElFactory.createElement(inCorrectElName);
        assertNotNull(elNode);

        Node node;
        try {
            JAXBEncoderDecoder.marshall(context, null, null, inCorrectElName,  elNode);
            fail("Should have thrown a ProtocolException");
        } catch (ProtocolException ex) {
            //expected - not a valid object
        }

        GreetMe obj = new GreetMe();
        obj.setRequestType("Hello");
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());
        JAXBEncoderDecoder.marshall(context, null, obj, elName, elNode);
        node = elNode.getLastChild();
        //The XML Tree Looks like
        //<GreetMe><requestType>Hello</requestType></GreetMe>
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        Node childNode = node.getFirstChild();
        assertEquals(Node.ELEMENT_NODE, childNode.getNodeType());
        childNode = childNode.getFirstChild();
        assertEquals(Node.TEXT_NODE, childNode.getNodeType());
        assertEquals(str, childNode.getNodeValue());

        // Now test schema validation during marshaling
        StringStruct stringStruct = new StringStruct();
        // Don't initialize one of the structure members.
        //stringStruct.setArg0("hello");
        stringStruct.setArg1("world");
        // Marshal without the schema should work.
        JAXBEncoderDecoder.marshall(context, null, stringStruct, elName,  elNode);
        try {
            // Marshal with the schema should get an exception.
            JAXBEncoderDecoder.marshall(context, schema, stringStruct, elName,  elNode);
            fail("Marshal with schema should have thrown a ProtocolException");
        } catch (ProtocolException ex) {
            //expected - not a valid object
        }
    }

    public void testMarshallIntoStax() throws Exception {
        GreetMe obj = new GreetMe();
        obj.setRequestType("Hello");
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputFactory opFactory = XMLOutputFactory.newInstance();
        opFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        XMLEventWriter writer = opFactory.createXMLEventWriter(baos);

        //STARTDOCUMENT/ENDDOCUMENT is not required
        //writer.add(eFactory.createStartDocument("utf-8", "1.0"));        
        JAXBEncoderDecoder.marshall(context, null, obj, elName, writer);
        //writer.add(eFactory.createEndDocument());
        writer.flush();
        writer.close();
        
        //System.out.println(baos.toString());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLInputFactory ipFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = ipFactory.createXMLEventReader(bais);
        
        Unmarshaller um = context.createUnmarshaller();        
        Object val = um.unmarshal(reader, GreetMe.class);
        assertTrue(val instanceof JAXBElement);
        val = ((JAXBElement)val).getValue();
        assertTrue(val instanceof GreetMe);
        assertEquals(obj.getRequestType(), 
                     ((GreetMe)val).getRequestType());
    }

    public void testUnmarshallFromStax() throws Exception {
        QName elName = new QName(wrapperAnnotation.targetNamespace(),
                                 wrapperAnnotation.localName());
        
        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = 
            factory.createXMLEventReader(is);

        QName[] tags = {SOAPConstants.SOAP_ENV, SOAPConstants.SOAP_BODY};

        StaxEventFilter filter = new StaxEventFilter(tags);
        reader = factory.createFilteredReader(reader, filter);

        //Remove START_DOCUMENT & START_ELEMENT pertaining to Envelope and Body Tags.

        Object val = JAXBEncoderDecoder.unmarshall(context, null, reader, elName, GreetMe.class);
        assertNotNull(val);
        assertTrue(val instanceof GreetMe);
        assertEquals("TestSOAPInputPMessage", 
                     ((GreetMe)val).getRequestType());

        is.close();
    }
    
    public void testMarshalRPCLit() throws Exception {
        SOAPFactory soapElFactory = SOAPFactory.newInstance();
        QName elName = new QName("http://test_jaxb_marshall", "in");
        SOAPElement elNode = soapElFactory.createElement(elName);
        JAXBEncoderDecoder.marshall(context, null, new String("TestSOAPMessage"), elName,  elNode);
        
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

        Object obj = JAXBEncoderDecoder.unmarshall(context, null,
                         elNode, elName, Class.forName(wrapperAnnotation.className()));
        assertNotNull(obj);

        //Add a Node and then test
        assertEquals(GreetMe.class,  obj.getClass());
        assertEquals(str, ((GreetMe)obj).getRequestType());
        
        Node n = null;
        try {
            JAXBEncoderDecoder.unmarshall(context, null, n, null, String.class);
            fail("Should have received a ProtocolException");
        } catch (ProtocolException pe) {
            //Expected Exception
        } catch (Exception ex) {
            fail("Should have received a ProtocolException, not: " + ex);
        }
        
        // Now test schema validation during unmarshaling
        elName = new QName(wrapperAnnotation.targetNamespace(),
                           "stringStruct");
        // Create an XML Tree of
        // <StringStruct><arg1>World</arg1></StringStruct>
        elNode = soapElFactory.createElement(elName);
        elNode.addNamespaceDeclaration("", elName.getNamespaceURI()); 
        str = new String("World");
        elNode.addChildElement("arg1").setValue(str);
        // Should unmarshal without problems when no schema used.
        obj = JAXBEncoderDecoder.unmarshall(context, null, elNode,  elName,
            Class.forName("org.objectweb.hello_world_soap_http.types.StringStruct"));
        assertNotNull(obj);
        assertEquals(StringStruct.class,  obj.getClass());
        assertEquals(str, ((StringStruct)obj).getArg1());
        try {
            // unmarshal with schema should raise exception.
            obj = JAXBEncoderDecoder.unmarshall(context, schema, elNode,  elName,
                Class.forName("org.objectweb.hello_world_soap_http.types.StringStruct"));
            fail("Should have thrown a ProtocolException");
        } catch (ProtocolException ex) {
            // expected - schema validation should fail.
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

