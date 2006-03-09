package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;

import org.w3c.dom.*;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.hello_world_xml_http.bare.Greeter;

public class XMLBindingImplBareTest extends TestCase {
    private XMLBindingImpl binding;
    private ObjectMessageContextImpl objContext;
    private XMLMessageContextImpl xmlContext;
    private XMLUtils xmlUtils;
    private TestUtils testUtils;
    
    public XMLBindingImplBareTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLBindingImplTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        xmlUtils = new XMLUtils();
        testUtils = new TestUtils();
        binding = new XMLBindingImpl(false);
        objContext = new ObjectMessageContextImpl();
        xmlContext = new XMLMessageContextImpl(new GenericMessageContext());
        
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "greetMe"));
    }
    
    public void testBindingReference() throws Exception {
        Bus bus = Bus.init();
        binding = getBindingImpl(bus, testUtils.getEndpointReference());
        
        bus = binding.getBus();
        assertNotNull(bus);
        EndpointReferenceType reference = binding.getEndpointReference();
        assertNotNull(reference);

        // test wsdl definition from the endpoint reference
        Definition wsdlDef = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(), reference);
        assertNotNull(wsdlDef);
        QName wsdlName = new QName("http://objectweb.org/hello_world_xml_http/bare", "HelloWorld");
        assertEquals(wsdlDef.getQName(), wsdlName);
        
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
        assertNotNull(port);
        Binding b = port.getBinding();
        assertNotNull(b);
        BindingOperation operation = b.getBindingOperation("sayHi", "sayHiRequest", "sayHiResponse");
        assertNotNull(operation);
        BindingInput input = operation.getBindingInput();
        assertNotNull(input);
        
        XMLBinding xmlBinding = null;
        Iterator ite = input.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof XMLBinding) {
                xmlBinding = (XMLBinding)obj;
            }
        }
        assertNotNull(xmlBinding);
        assertEquals(new QName("http://objectweb.org/hello_world_xml_http/bare", "sayHi"),
                     xmlBinding.getRootNode());
    }

    // Test greetMe which has one part in message
    public void testMarshalBareWithoutRootNode() throws Exception {
        Bus bus = Bus.init();
        binding = getBindingImpl(bus, testUtils.getEndpointReference());
        
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

        InputStream is = getClass().getResourceAsStream("resources/GreetMeBareReq.xml");
        Document expectDOM = xmlUtils.parse(is);
        Document resultDOM = msg.getRoot();

        is.close();
        assertTrue(expectDOM.isEqualNode(resultDOM));
    }

    // Test sayHi which has no parameter to passed in
    public void testMarshalBareWithRootNode() throws Exception {
        Bus bus = Bus.init();

        binding = getBindingImpl(bus, testUtils.getEndpointReference());

        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "sayHi"));
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
        
        InputStream is = getClass().getResourceAsStream("resources/SayHiBareReq.xml");
        Document expectDOM = xmlUtils.parse(is);
        Document resultDOM = msg.getRoot();
        is.close();
        
        assertNotNull(expectDOM);
        assertNotNull(resultDOM);
        
        String expect = xmlUtils.toString(expectDOM);
        String result = xmlUtils.toString(resultDOM);
        assertTrue(expect.equals(result));
    }

    // Test the greetMe Operation in message
    public void testUnmarshalBareInputMessage() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        InputStream in = getClass().getResourceAsStream("resources/GreetMeBareReq.xml");
        assertNotNull(binding.getMessageFactory());
        XMLMessage xmlMessage = binding.getMessageFactory().createMessage(in);
        xmlContext.setMessage(xmlMessage);

        String data = new String("TestXMLInputMessage");

        //testDocLitBare method has a IN parameter
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "greetMe"));
        objContext.setMessageObjects(new Object[1]);
        binding.unmarshal(xmlContext, objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNull(objContext.getReturn());
        assertEquals(1, params.length);
        assertEquals(data, (String)params[0]);
    }

    // Test the greetMe Operation out message
    public void testUnmarshalBareOutputMessage() throws Exception {
        xmlContext.put(ObjectMessageContext.MESSAGE_INPUT, true);
        InputStream in = getClass().getResourceAsStream("resources/GreetMeBareResp.xml");
        assertNotNull(binding.getMessageFactory());
        XMLMessage xmlMessage = binding.getMessageFactory().createMessage(in);
        xmlContext.setMessage(xmlMessage);

        //testDocLitBare method has a IN parameter
        objContext.setMethod(ClassUtils.getMethod(Greeter.class, "greetMe"));
        objContext.setMessageObjects(new Object[1]);
        binding.unmarshal(xmlContext, objContext,
                          new JAXBDataBindingCallback(objContext.getMethod(),
                                                      DataBindingCallback.Mode.PARTS,
                                                      null));
        
        Object[] params = objContext.getMessageObjects();
        assertNotNull(params);
        assertNotNull(objContext.getReturn());
        assertEquals("TestXMLOutputMessage", (String)objContext.getReturn());
    }

    private XMLBindingImpl getBindingImpl(Bus bus, EndpointReferenceType ref) {
        return new XMLBindingImpl(bus, ref, false);
    }
}
