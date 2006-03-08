package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.jws.WebResult;

import org.w3c.dom.*;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.bindings.TestInputStreamContext;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;

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
        WebResult resultAnnotation  = callback.getWebResultAnnotation();
        assertEquals("responseType", resultAnnotation.name());
    }
}
