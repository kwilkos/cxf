package org.objectweb.celtix.bus.bindings.soap;

import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Holder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.header_test.TestHeader;
import org.objectweb.header_test.types.TestHeader1;
import org.objectweb.header_test.types.TestHeader2Response;
import org.objectweb.header_test.types.TestHeader3;
import org.objectweb.header_test.types.TestHeader5;

public class SoapBindingImplHeaderTest extends TestCase {
    private SOAPBindingImpl binding;
    private ObjectMessageContextImpl objContext;
    private SOAPMessageContextImpl soapContext;
    public SoapBindingImplHeaderTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapBindingImplHeaderTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        binding = new SOAPBindingImpl(false);
        objContext = new ObjectMessageContextImpl();
        soapContext = new SOAPMessageContextImpl(objContext);
    }

    public void testMarshalHeaderDocLitInputMessage() throws Exception {
        //Test The InputMessage of testHeader1 Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        Method testHeader1 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader1");
        assertNotNull(testHeader1);
        objContext.setMethod(testHeader1);
        Object arg = new TestHeader1();
        objContext.setMessageObjects(arg, arg);

        binding.marshal(
                   objContext,
                   soapContext,
                   new JAXBDataBindingCallback(testHeader1, 
                                               DataBindingCallback.Mode.PARTS,
                                               null));                
        SOAPMessage msg = soapContext.getMessage();
        
        assertNotNull(msg);
        //Test the Header Part Only
        assertNotNull(msg.getSOAPHeader());
        assertTrue(msg.getSOAPHeader().hasChildNodes());
        NodeList list = msg.getSOAPHeader().getChildNodes();
        assertEquals(1, list.getLength());
        Element headerElement = (Element)list.item(0);
        assertEquals("true", headerElement
                     .getAttributeNS(SOAPConstants.HEADER_MUSTUNDERSTAND.getNamespaceURI(),
                                     SOAPConstants.HEADER_MUSTUNDERSTAND.getLocalPart()));
        //TestHeader1 has no child elements.
        assertFalse(headerElement.hasChildNodes());
        
        //TestHeader3 InOutHeader
        Method testHeader3 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader3");
        assertNotNull(testHeader3);
        objContext.setMethod(testHeader3);
        TestHeader3 arg0 = new TestHeader3();        
        TestHeader3 arg1 = new TestHeader3();
        arg1.setRequestType("HeaderVal");
        Object[] args = SOAPMessageUtil.getMessageObjects(testHeader3, arg0, arg1);
        objContext.setMessageObjects(args);
        //Write soap headers for testHeader3 operation - tests inout headers
        binding.marshal(
                   objContext,
                   soapContext,
                   new JAXBDataBindingCallback(testHeader3, 
                                               DataBindingCallback.Mode.PARTS,
                                               null));                

        msg = soapContext.getMessage();
        assertNotNull(msg);

        //Test the Header Part Only
        assertNotNull(msg.getSOAPHeader());
        assertTrue(msg.getSOAPHeader().hasChildNodes());
        list = msg.getSOAPHeader().getChildNodes();
        assertEquals(1, list.getLength());
        headerElement = (Element)list.item(0);
        //TestHeader3 has no child elements
        assertTrue(headerElement.hasChildNodes());
        list = headerElement.getChildNodes();
        assertEquals(1, list.getLength());
        headerElement = (Element)list.item(0);
        assertTrue(headerElement.hasChildNodes());
        list = headerElement.getChildNodes();
        assertEquals(1, list.getLength());
        Text text = (Text)list.item(0);
        assertEquals(arg1.getRequestType(), text.getData());
    }
  
    public void testMarshalHeaderDocLitOutputMessage() throws Exception {
        //Test The InputMessage of testHeader1 Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        Method testHeader2 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader2");
        assertNotNull(testHeader2);
        objContext.setMethod(testHeader2);
        TestHeader2Response arg0 = new TestHeader2Response();
        arg0.setResponseType("BodyVal2");
        TestHeader2Response arg1 = new TestHeader2Response();
        arg1.setResponseType("HeaderVal2");
        
        Object[] args = SOAPMessageUtil.getMessageObjects(testHeader2, null, arg0, arg1);
        objContext.setMessageObjects(args);
        //Write the SOAP Headers for testHeader2 operation - tests out headers        
        binding.marshal(
                   objContext,
                   soapContext,
                   new JAXBDataBindingCallback(testHeader2, 
                                               DataBindingCallback.Mode.PARTS,
                                               null));                

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);

        //Test the Header Part Only
        assertNotNull(msg.getSOAPHeader());
        assertTrue(msg.getSOAPHeader().hasChildNodes());
        NodeList list = msg.getSOAPHeader().getChildNodes();
        assertEquals(1, list.getLength());
        Element headerElement = (Element)list.item(0);
        //Check for mustUndrstand Attribute
        assertEquals("true", headerElement
                     .getAttributeNS(SOAPConstants.HEADER_MUSTUNDERSTAND.getNamespaceURI(),
                                     SOAPConstants.HEADER_MUSTUNDERSTAND.getLocalPart()));
        
        //TestHeader3 has child elements.
        assertTrue(headerElement.hasChildNodes());
        list = headerElement.getChildNodes();
        assertEquals(1, list.getLength());
        assertEquals(arg1.getResponseType(), list.item(0).getFirstChild().getNodeValue());
        
        //TestHeader5 return Header
        Method testHeader5 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader5");
        assertNotNull(testHeader5);
        objContext.setMethod(testHeader5);
        
        TestHeader5 arg2 = new TestHeader5();
        arg2.setRequestType("HeaderVal5");
        
        objContext.setMessageObjects(new Object[0]);
        objContext.setReturn(arg2);
        //Write the SOAP Headers for testHeader5 operation - tests headers as return.        
        binding.marshal(
                                     objContext,
                                     soapContext,
                                     new JAXBDataBindingCallback(testHeader5, 
                                                                 DataBindingCallback.Mode.PARTS,
                                                                 null));                

        msg = soapContext.getMessage();
        assertNotNull(msg);

        //Test the Header Part Only
        assertNotNull(msg.getSOAPHeader());
        assertTrue(msg.getSOAPHeader().hasChildNodes());
        list = msg.getSOAPHeader().getChildNodes();
        assertEquals(1, list.getLength());
        headerElement = (Element)list.item(0);
        //Check for mustUndrstand Attribute
        assertEquals("true", headerElement
                     .getAttributeNS(SOAPConstants.HEADER_MUSTUNDERSTAND.getNamespaceURI(),
                                     SOAPConstants.HEADER_MUSTUNDERSTAND.getLocalPart()));
        
        //TestHeader5 has child elements.
        assertTrue(headerElement.hasChildNodes());
        list = headerElement.getChildNodes();
        assertEquals(1, list.getLength());
        assertEquals(arg2.getRequestType(), list.item(0).getFirstChild().getNodeValue());
        
    }

    public void testUnmarshalHeaderDocLitInputMessage() throws Exception {
        
        Method testHeader3 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader3");
        assertNotNull(testHeader3);
        objContext.setMethod(testHeader3);
        objContext.setMessageObjects(SOAPMessageUtil.getMessageObjects(testHeader3));
        
        InputStream is =  getClass().getResourceAsStream("resources/TestHeader3DocLitReq.xml");
        assertNotNull(binding.getMessageFactory());
        SOAPMessage headerMsg = binding.getMessageFactory().createMessage(null,  is);
        assertNotNull(headerMsg);
        soapContext.setMessage(headerMsg);
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        
        //Test The InputMessage of testHeader3 Operation
        binding.unmarshal(soapContext, objContext,
                                 new JAXBDataBindingCallback(
                                                             testHeader3,
                                                             DataBindingCallback.Mode.PARTS,
                                                             null));
        
        //Read the headers of testHeader3 Operation - tests inout headers
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(2, params.length);
        assertTrue(params[1].getClass().isAssignableFrom(Holder.class));
        Holder<?> holder = (Holder<?>)params[1];
        assertNotNull(holder.value);
        assertTrue(holder.value.getClass().isAssignableFrom(TestHeader3.class));
        TestHeader3 header3 = (TestHeader3)holder.value;
        assertNull(header3.getRequestType());
    }
    
    public void testUnmarshalHeaderDocLitOutputMessage() throws Exception {
        
        Method testHeader2 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader2");
        assertNotNull(testHeader2);
        objContext.setMethod(testHeader2);
        objContext.setMessageObjects(SOAPMessageUtil.getMessageObjects(testHeader2));
        
        InputStream is =  getClass().getResourceAsStream("resources/TestHeader2DocLitResp.xml");
        assertNotNull(binding.getMessageFactory());
        SOAPMessage headerMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(headerMsg);
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        
        binding.unmarshal(soapContext, objContext,
                                 new JAXBDataBindingCallback(
                                                             testHeader2,
                                                             DataBindingCallback.Mode.PARTS,
                                                             null));
                
        //Read the SOAP Headers
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(3, params.length);
        //Test the Header method paramaters
        assertTrue(params[2].getClass().isAssignableFrom(Holder.class));
        Holder<?> holder = (Holder<?>)params[2];
        assertNotNull(holder.value);
        assertTrue(holder.value.getClass().isAssignableFrom(TestHeader2Response.class));
        TestHeader2Response header2 = (TestHeader2Response)holder.value;
        assertEquals("HeaderVal2", header2.getResponseType());
        
        //test for return header using TestHeader5 operation 
        Method testHeader5 = SOAPMessageUtil.getMethod(TestHeader.class, "testHeader5");
        assertNotNull(testHeader5);
        objContext.setMethod(testHeader5);
        objContext.setMessageObjects(SOAPMessageUtil.getMessageObjects(testHeader5));
        
        is =  getClass().getResourceAsStream("resources/TestHeader5DocLitResp.xml");
        headerMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(headerMsg);
        
        //Test The InputMessage of testHeader3 Operation
        binding.unmarshal(soapContext, objContext,
                                 new JAXBDataBindingCallback(testHeader5,
                                                             DataBindingCallback.Mode.PARTS,
                                                             null));

        
        params = objContext.getMessageObjects();
        assertNotNull(params);
        assertEquals(1, params.length);       
 
        //Test the Header method paramaters        
        assertNotNull(objContext.getReturn());
        TestHeader5 header5 = (TestHeader5)objContext.getReturn();
        assertEquals("HeaderVal5", header5.getRequestType());
        
    }
}
