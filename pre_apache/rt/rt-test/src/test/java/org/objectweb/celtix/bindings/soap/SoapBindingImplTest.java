package org.objectweb.celtix.bindings.soap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.TestInputStreamContext;
import org.objectweb.celtix.bindings.TestOutputStreamContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.datamodel.soap.SOAPConstants;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;
import org.objectweb.type_test.doc.TypeTestPortType;

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
        
        binding = new SOAPBindingImpl(false);
        objContext = new ObjectMessageContextImpl();
        soapContext = new SOAPMessageContextImpl(new GenericMessageContext());
        
        objContext.setMethod(SOAPMessageUtil.getMethod(Greeter.class, "greetMe"));
    }
    
    public void testCreateBindingMessageContext() throws Exception {       
        binding = new SOAPBindingImpl(false);
        byte[] bArray = new byte[0];
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        assertNotNull(binding.createBindingMessageContext(inCtx));
    }

    public void testGetMessageFactory() throws Exception {
        assertNotNull(binding.getSOAPFactory());
    }
    
    public void testMarshalWrapDocLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        String arg0 = new String("TestSOAPInputPMessage");
        objContext.setMessageObjects(arg0);

        binding.marshal(objContext,
                         soapContext,
                         new JAXBDataBindingCallback(objContext.getMethod(),
                                                     DataBindingCallback.Mode.PARTS,
                                                     null));
        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);

        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(arg0, wrappedNode.getFirstChild().getNodeValue());
    }

    public void testMarshalWrapDocLitMessageForInOutVar() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        Method testInt = SOAPMessageUtil.getMethod(TypeTestPortType.class, "testInt");
        assertNotNull(testInt);
        objContext.setMethod(testInt);
        
        Object[] methodArg = SOAPMessageUtil.getMessageObjects(testInt);
        int arg0 = 5;
        methodArg[0] = arg0;
        //INOUT Variable
        methodArg[1].getClass().getField("value").set(methodArg[1], arg0);
        objContext.setMessageObjects(methodArg);

        binding.marshal(objContext,
                         soapContext,
                         new JAXBDataBindingCallback(objContext.getMethod(),
                                                     DataBindingCallback.Mode.PARTS,
                                                     null));
        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);

        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        list = list.item(0).getChildNodes();
        assertEquals(2, list.getLength());
        Node wrappedNode = list.item(0);
        assertEquals(String.valueOf(arg0), wrappedNode.getFirstChild().getNodeValue());
        
        wrappedNode = list.item(1);
        assertEquals(String.valueOf(arg0), wrappedNode.getFirstChild().getNodeValue());        
    }
    
    public void testMarshalWrapDocLitOutputMessage() throws Exception {
        //Test The Output of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        objContext.setMessageObjects(new Object[0]);

        String arg0 = new String("TestSOAPOutputMessage");
        objContext.setReturn(arg0);
        
        binding.marshal(objContext,
                        soapContext,
                         new JAXBDataBindingCallback(objContext.getMethod(),
                                                     DataBindingCallback.Mode.PARTS,
                                                     null));
        SOAPMessage msg = soapContext.getMessage();
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
        String str = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data);        
        
        TestInputStreamContext inCtx = new TestInputStreamContext(str.getBytes());
        binding.read(inCtx, soapContext);

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();

        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(data, wrappedNode.getFirstChild().getNodeValue());
        
        //Parse SOAP 1.2 message
        InputStream is =  getClass().getResourceAsStream("resources/Soap12message.xml");
        inCtx.setInputStream(is);
        try {
            binding.read(inCtx, soapContext);
            fail("Should have received a SOAP FaultException");
        } catch (SOAPFaultException sfe) {
            SOAPFault sf = sfe.getFault();
            assertNotNull("Should have a non null soap fault", sf);
            assertEquals(SOAPConstants.FAULTCODE_VERSIONMISMATCH, sf.getFaultCodeAsQName());
        }
    }
    public void testUnmarshalWrapDocLitInputMessage() throws Exception {
        //Test The InputMessage of GreetMe Operation
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");        
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");
        String str = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data);
        InputStream in = new ByteArrayInputStream(str.getBytes());
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        //GreetMe method has a IN parameter
        objContext.setMessageObjects(new Object[1]);
        binding.unmarshal(soapContext, 
                          objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(1, params.length);
        assertEquals(data, (String)params[0]);
    }    
    public void testUnmarshalWrapDocLitMessageWithInOutVar() throws Exception {
        //Test The testInt Operation of TypeTestPortType SEI
        Method testInt = SOAPMessageUtil.getMethod(TypeTestPortType.class, "testInt");
        assertNotNull(testInt);
        objContext.setMethod(testInt);
        
        InputStream is =  getClass().getResourceAsStream("resources/TestIntDocLitTypeTestReq.xml");
        assertNotNull(binding.getMessageFactory());
        SOAPMessage faultMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(faultMsg);
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        Object[] methodArg = SOAPMessageUtil.getMessageObjects(testInt);
        assertNotNull(methodArg);
        objContext.setMessageObjects(methodArg);
        
        binding.unmarshal(soapContext, 
                          objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));

        assertNotNull(objContext.getMessageObjects());        
        methodArg = objContext.getMessageObjects();

        assertNull(objContext.getReturn());
        assertEquals(3, methodArg.length);
        assertEquals("5", String.valueOf(methodArg[0]));
    }    
  
    public void testUnmarshalWrapDocLiteralOutputMessage() throws Exception {

        QName wrapName = 
            new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse");        
        QName elName = 
            new QName("http://objectweb.org/hello_world_soap_http/types", "responseType");
        String data = new String("TestSOAPOutputMessage");
        String str = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data);
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());

        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, true);        
        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        binding.unmarshal(soapContext, 
                          objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        assertNull(objContext.getMessageObjects());
        assertNotNull(objContext.getReturn());
        assertEquals(data, (String)objContext.getReturn());
    }    
    
    public void testMarshalDocLiteralUserFaults() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        String exMessage = new String("Test Exception");
        ErrorCode ec = new ErrorCode();
        ec.setMajor((short)1);
        ec.setMinor((short)1);
        NoSuchCodeLit nscl = new NoSuchCodeLit();
        nscl.setCode(ec);
        NoSuchCodeLitFault ex = new NoSuchCodeLitFault(exMessage, nscl);
        objContext.setException(ex);

        binding.marshalFault(objContext,
                             soapContext,
                             new JAXBDataBindingCallback(objContext.getMethod(),
                                                         DataBindingCallback.Mode.PARTS,
                                                         null));
        SOAPMessage msg = soapContext.getMessage();

        assertNotNull(msg);
        Node xmlNode = msg.getSOAPBody();
        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());
        assertTrue(msg.getSOAPBody().hasFault());
        SOAPFault fault = msg.getSOAPBody().getFault();
        assertNotNull(fault);
        assertEquals(
                     getExceptionString(ex, exMessage), 
                     fault.getFaultString());
        assertTrue(fault.hasChildNodes());
        Detail detail = fault.getDetail();
        assertNotNull(detail);
        
        NodeList list = detail.getChildNodes();
        assertEquals(1, list.getLength()); 
        
        WebFault wfAnnotation = ex.getClass().getAnnotation(WebFault.class);
        assertEquals(wfAnnotation.targetNamespace(), list.item(0).getNamespaceURI());
        assertEquals(wfAnnotation.name(), list.item(0).getLocalName());
    }    
    
    public void testMarshalSystemFaults() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        SOAPException se = new SOAPException("SAAJ Exception");
        objContext.setException(se);

        binding.marshalFault(objContext, 
                             soapContext,
                             new JAXBDataBindingCallback(objContext.getMethod(),
                                                         DataBindingCallback.Mode.PARTS,
                                                         null));
        SOAPMessage msg = soapContext.getMessage();
        
        assertNotNull(msg);
        Node xmlNode = msg.getSOAPBody();
        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());

        assertTrue(msg.getSOAPBody().hasFault());
        SOAPFault fault = msg.getSOAPBody().getFault();
        assertNotNull(fault);
        assertEquals(
                     getExceptionString(se, se.getMessage()),
                     fault.getFaultString());
        assertTrue(fault.hasChildNodes());
        NodeList list = fault.getChildNodes();
        assertEquals(2, list.getLength());         
    }

    public void testUnmarshalDocLiteralUserFaults() throws Exception {
        //Test The InputMessage of GreetMe Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        objContext.setMethod(SOAPMessageUtil.getMethod(Greeter.class, "testDocLitFault"));

        InputStream is =  getClass().getResourceAsStream("resources/NoSuchCodeDocLiteral.xml");
        SOAPMessage faultMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(faultMsg);

        binding.unmarshalFault(soapContext, objContext,
                               new JAXBDataBindingCallback(objContext.getMethod(),
                                                           DataBindingCallback.Mode.PARTS,
                                                           null));
        assertNotNull(objContext.getException());
        Object faultEx = objContext.getException();
        assertTrue(NoSuchCodeLitFault.class.isAssignableFrom(faultEx.getClass()));
        NoSuchCodeLitFault nscf = (NoSuchCodeLitFault)faultEx;
        assertNotNull(nscf.getFaultInfo());
        NoSuchCodeLit faultInfo = nscf.getFaultInfo();

        assertNotNull(faultInfo.getCode());
        ErrorCode ec = faultInfo.getCode();
        assertEquals(ec.getMajor(), (short)666);
        assertEquals(ec.getMinor(), (short)999);
        
        assertEquals(nscf.getMessage(), "Test Exception");
        
        is =  getClass().getResourceAsStream("resources/BadRecordDocLiteral.xml");
        faultMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(faultMsg);
        binding.unmarshalFault(soapContext, objContext,
                               new JAXBDataBindingCallback(objContext.getMethod(),
                                                           DataBindingCallback.Mode.PARTS,
                                                           null));
        assertNotNull(objContext.getException());
        faultEx = objContext.getException();
        assertTrue(BadRecordLitFault.class.isAssignableFrom(faultEx.getClass()));
        BadRecordLitFault brlf = (BadRecordLitFault)faultEx;
        assertEquals(brlf.getFaultInfo(), "BadRecordTested");
        
        is =  getClass().getResourceAsStream("resources/SystemFault.xml");
        faultMsg = binding.getMessageFactory().createMessage(null,  is);
        soapContext.setMessage(faultMsg);
        binding.unmarshalFault(soapContext, objContext,
                               new JAXBDataBindingCallback(objContext.getMethod(),
                                                           DataBindingCallback.Mode.PARTS,
                                                           null));
        assertNotNull(objContext.getException());
        faultEx = objContext.getException();
        assertTrue("Should be a SOAPFaultException", 
                   SOAPFaultException.class.isAssignableFrom(faultEx.getClass()));
        SOAPFaultException sfe = (SOAPFaultException)faultEx;
        SOAPFault sf = sfe.getFault();
        assertNotNull(sf);
    }
    
    //Bare Doc Literal Tests
    public void testMarshalBareDocLitInputMessage() throws Exception {
        //Test The InputMessage of testDocLitBare Operation
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        objContext.setMethod(SOAPMessageUtil.getMethod(Greeter.class, "testDocLitBare"));
        
        String arg0 = new String("DocLitBareDocumentInputMessage");
        objContext.setMessageObjects(arg0);

        binding.marshal(objContext,
                        soapContext,
                        new JAXBDataBindingCallback(objContext.getMethod(),
                                                    DataBindingCallback.Mode.PARTS,
                                                    null));
        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        //msg.writeTo(System.out);
        assertTrue(msg.getSOAPBody().hasChildNodes());
        NodeList list = msg.getSOAPBody().getChildNodes();
        assertEquals(1, list.getLength());
        Node msgnode = list.item(0).getFirstChild();
        assertTrue(!msgnode.hasChildNodes());
        assertEquals(arg0, msgnode.getNodeValue());
    }
    
    public void testUnmarshalBareDocLitInputMessage() throws Exception {
        //Test The InputMessage of testDocLitBare Operation
        objContext.setMethod(SOAPMessageUtil.getMethod(Greeter.class, "testDocLitBare"));
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "BareDocument");        

        String data = new String("DocLitBareDocumentInputMessage");
        String str = SOAPMessageUtil.createBareDocLitSOAPMessage(elName, data);
        
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        assertNotNull(binding.getMessageFactory());
        SOAPMessage soapMessage = binding.getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        //testDocLitBare method has a IN parameter
        objContext.setMessageObjects(new Object[1]);
        binding.unmarshal(soapContext, objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(1, params.length);
        assertEquals(data, (String)params[0]);
    }
    
    public void testMarshalEmptyBody() throws Exception {
       
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        binding.marshal(objContext,
                        soapContext,
                        null);
        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(!msg.getSOAPBody().hasChildNodes());
        
        TestOutputStreamContext ostreamCtx = new TestOutputStreamContext(null, soapContext);
        
        binding.write(soapContext, ostreamCtx);
        
    }
    
    public void testUnmarshalEmptyBody() throws Exception {
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        InputStream is =  getClass().getResourceAsStream("resources/EmptyBody.xml");
        inCtx.setInputStream(is);
        binding.read(inCtx, soapContext);

        SOAPMessage msg = soapContext.getMessage();
        assertNotNull(msg);
        assertTrue(!msg.getSOAPBody().hasChildNodes());
        
        assertNull(objContext.getMessageObjects());
        assertNull(objContext.getReturn());
        binding.unmarshal(soapContext, objContext, null);
        assertNull(objContext.getMessageObjects());
        assertNull(objContext.getReturn());
    }
    
    
    private String getExceptionString(Exception ex, String faultString) {
        StringBuffer str = new StringBuffer();
        if (ex != null) {
            str.append(ex.getClass().getName());
            str.append(": ");
        }
        str.append(faultString);
        
        if (!ex.getClass().isAnnotationPresent(WebFault.class)) {
            str.append("\n");
            for (StackTraceElement s : ex.getStackTrace()) {
                str.append(s.toString());
                str.append("\n");
            }          
        }
        return str.toString();
    }
}
