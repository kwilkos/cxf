package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.jws.WebResult;
import javax.xml.ws.WebFault;

import org.w3c.dom.*;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.bindings.TestInputStreamContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;
import org.objectweb.hello_world_xml_http.wrapped.PingMeFault;
import org.objectweb.hello_world_xml_http.wrapped.types.FaultDetail;

public class XMLBindingImplTest extends TestCase {
    private XMLBindingImpl binding;
    private ObjectMessageContextImpl objContext;
    private XMLMessageContextImpl xmlContext;
    private XMLUtils xmlUtils;
    
    public XMLBindingImplTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLBindingImplTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        xmlUtils = new XMLUtils();
        binding = new XMLBindingImpl(false);
        objContext = new ObjectMessageContextImpl();
        xmlContext = new XMLMessageContextImpl(new GenericMessageContext());
        
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "greetMe"));
    }
    
    public void testCreateBindingMessageContext() throws Exception {
        binding = new XMLBindingImpl(false);
        byte[] bArray = new byte[0];
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        assertNotNull(binding.createBindingMessageContext(inCtx));
    }
    
    public void testOperationName() throws Exception {
        JAXBDataBindingCallback callback =  new JAXBDataBindingCallback(objContext.getMethod(),
                                                                        DataBindingCallback.Mode.PARTS,
                                                                        null);
        String operationName = callback.getOperationName();
        assertNotNull(operationName);
        assertEquals("greetMe", operationName);
    }
    
    //Test The InputMessage of GreetMe Operation -- wrapped style
    public void testMarshalWrapInputMessage() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        String arg0 = new String("TestXMLInputMessage");
        objContext.setMessageObjects(arg0);

        binding.marshal(objContext,
                        xmlContext,
                        new JAXBDataBindingCallback(objContext.getMethod(),
                                                    DataBindingCallback.Mode.PARTS,
                                                    null));
        XMLMessage msg = xmlContext.getMessage();
        assertNotNull(msg);

        InputStream is = getClass().getResourceAsStream("resources/GreetMeWrappedReq.xml");
        Document expectDOM = xmlUtils.parse(is);
        Document resultDOM = msg.getRoot();
        is.close();
        assertTrue(expectDOM.isEqualNode(resultDOM));
    }

    // Test sayHi operation which don't have part in request message.
    public void testMarshalWrapNoPart() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "sayHi"));
        binding.marshal(objContext,
                        xmlContext,
                        new JAXBDataBindingCallback(objContext.getMethod(),
                                                    DataBindingCallback.Mode.PARTS,
                                                    null));
        XMLMessage msg = xmlContext.getMessage();
        assertNotNull(msg);

        InputStream is = getClass().getResourceAsStream("resources/SayHiWrappedReq.xml");
        Document expectDOM = xmlUtils.parse(is);
        Document resultDOM = msg.getRoot();
        is.close();
        assertTrue(expectDOM.isEqualNode(resultDOM));
    }
    
    //Test the Output of GreetMe Operation
    public void testMarshalWrapOutputMessage() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        objContext.setMessageObjects(new Object[0]);
        
        String arg0 = "TestXMLOutputMessage";
        objContext.setReturn(arg0);
        
        binding.marshal(objContext,
                        xmlContext,
                        new JAXBDataBindingCallback(objContext.getMethod(),
                                                    DataBindingCallback.Mode.PARTS,
                                                    null));
        XMLMessage msg = xmlContext.getMessage();
        assertNotNull(msg);
        assertTrue(msg.getRoot().hasChildNodes());
        NodeList list = msg.getRoot().getChildNodes();
        assertEquals(1, list.getLength());
        Node wrappedNode = list.item(0).getFirstChild();
        assertTrue(wrappedNode.hasChildNodes());
        assertEquals(arg0, wrappedNode.getFirstChild().getNodeValue());
    }

    // Test unmashall greetMe method
    public void testUnmarshalWrapInputMessage() throws Exception {
        String data = "TestXMLInputMessage";
        InputStream in = getClass().getResourceAsStream("resources/GreetMeWrappedReq.xml");
        assertNotNull(binding.getMessageFactory());
        XMLMessage xmlMessage = binding.getMessageFactory().createMessage(in);
        xmlContext.setMessage(xmlMessage);
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        
        //GreetMe method has a IN parameter
        objContext.setMessageObjects(new Object[1]);
        binding.unmarshal(xmlContext, 
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

    public void testUnmarshalWrapOutputMessage() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        String data = new String("TestXMLOutputMessage");
        
        InputStream in = getClass().getResourceAsStream("resources/GreetMeWrappedResp.xml");
        assertNotNull(binding.getMessageFactory());
        XMLMessage xmlMessage = binding.getMessageFactory().createMessage(in);
        xmlContext.setMessage(xmlMessage);

        binding.unmarshal(xmlContext, 
                          objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        assertNull(objContext.getMessageObjects());
        assertNotNull(objContext.getReturn());
        assertEquals(data, (String)objContext.getReturn());
    }    

    public void testMethodAnnotation() throws Exception {
        JAXBDataBindingCallback callback = new JAXBDataBindingCallback(objContext.getMethod(),
                                                                       DataBindingCallback.Mode.PARTS,
                                                                       null);
        WebResult resultAnnotation  = callback.getWebResult();
        assertEquals("responseType", resultAnnotation.name());
    }

    public void testWrappedMarshalFault() throws Exception {

        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        String exMessage = new String("Test Exception");
        FaultDetail ec = new FaultDetail();
        ec.setMajor((short)2);
        ec.setMinor((short)1);
        PingMeFault ex = new PingMeFault(exMessage, ec);
        objContext.setException(ex);

        binding.marshalFault(objContext,
                             xmlContext,
                             new JAXBDataBindingCallback(objContext.getMethod(),
                                                         DataBindingCallback.Mode.PARTS,
                                                         null));
        XMLMessage msg = xmlContext.getMessage();

        assertNotNull(msg);
        Node xmlNode = msg.getRoot();

        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());
        assertTrue(msg.hasFault());
        XMLFault fault = msg.getFault();
        assertNotNull(fault);
        assertEquals(getExceptionString(ex, exMessage), 
                     fault.getFaultString());

        assertTrue(fault.hasChildNodes());
        Node detail = fault.getFaultDetail();
        assertNotNull(detail);
        
        NodeList list = detail.getChildNodes();
        assertEquals(1, list.getLength()); 
        
        WebFault wfAnnotation = ex.getClass().getAnnotation(WebFault.class);
        assertEquals(wfAnnotation.targetNamespace(), list.item(0).getNamespaceURI());
        assertEquals(wfAnnotation.name(), list.item(0).getLocalName());
    }

    public void testMarshalSystemFaults() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);

        XMLBindingException se = new XMLBindingException("XML Binding  Exception");
        objContext.setException(se);

        binding.marshalFault(objContext, 
                             xmlContext,
                             new JAXBDataBindingCallback(objContext.getMethod(),
                                                         DataBindingCallback.Mode.PARTS,
                                                         null));
        XMLMessage msg = xmlContext.getMessage();
        
        assertNotNull(msg);
        Node xmlNode = msg.getRoot();
        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());

        assertTrue(msg.hasFault());
        XMLFault fault = msg.getFault();
        assertNotNull(fault);
        assertEquals(getExceptionString(se, se.getMessage()),
                     fault.getFaultString());
        assertTrue(fault.hasChildNodes());
        NodeList list = fault.getFaultRoot().getChildNodes();
        assertEquals(1, list.getLength());         
    }

    public void testUnmarshalDocLiteralUserFaults() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "pingMe"));

        InputStream is =  getClass().getResourceAsStream("resources/xmlfault.xml");
        XMLMessage faultMsg = binding.getMessageFactory().createMessage(is);
        xmlContext.setMessage(faultMsg);

        binding.unmarshalFault(xmlContext,
                               objContext,
                               new JAXBDataBindingCallback(objContext.getMethod(),
                                                           DataBindingCallback.Mode.PARTS,
                                                           null));
        assertNotNull(objContext.getException());
        Object faultEx = objContext.getException();
        
        assertTrue(PingMeFault.class.isAssignableFrom(faultEx.getClass()));
        PingMeFault nscf = (PingMeFault)faultEx;
        assertNotNull(nscf.getFaultInfo());
        FaultDetail faultInfo = nscf.getFaultInfo();

        assertEquals(faultInfo.getMajor(), (short)2);
        assertEquals(faultInfo.getMinor(), (short)1);
        assertEquals("org.objectweb.hello_world_xml_http.wrapped.PingMeFault: PingMeFault raised by server",
                     nscf.getMessage());
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
