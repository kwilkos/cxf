package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class BindingOperationInfoTest extends TestCase {
    private static final String TEST_NS = "urn:test:ns";
    private BindingOperationInfo bindingOperationInfo;
    
    public void setUp() throws Exception {
        OperationInfo operationInfo = new OperationInfo(null, new QName(TEST_NS, "operationTest"));
        MessageInfo inputMessage = operationInfo.createMessage(new QName(
            "http://objectweb.org/hello_world_soap_http", "testInputMessage"));
        operationInfo.setInput("input", inputMessage);
        
        MessageInfo outputMessage = operationInfo.createMessage(new QName(
            "http://objectweb.org/hello_world_soap_http", "testOutputMessage"));
        operationInfo.setOutput("output", outputMessage);
        operationInfo.addFault(new QName(TEST_NS, "fault"), new QName(
            "http://objectweb.org/hello_world_soap_http", "faultMessage"));
        bindingOperationInfo = new BindingOperationInfo(null, operationInfo);
    }
    
    public void testName() throws Exception {
        assertEquals(bindingOperationInfo.getName(), new QName(TEST_NS, "operationTest"));
    }
    
    public void testBinding() throws Exception {
        assertNull(bindingOperationInfo.getBinding());
    }
    
    public void testOperation() throws Exception {
        assertEquals(bindingOperationInfo.getOperationInfo().getName(), new QName(TEST_NS, "operationTest"));
        assertTrue(bindingOperationInfo.getOperationInfo().hasInput());
        assertTrue(bindingOperationInfo.getOperationInfo().hasOutput());
        assertEquals(bindingOperationInfo.getOperationInfo().getInputName(), "input");
        assertEquals(bindingOperationInfo.getOperationInfo().getOutputName(), "output");
        assertEquals(bindingOperationInfo.getFaults().iterator().next().getFaultInfo().getFaultName(),
                     new QName(TEST_NS, "fault"));
        assertEquals(1, bindingOperationInfo.getFaults().size());
    }
    
    public void testInputMessage() throws Exception {
        BindingMessageInfo inputMessage = bindingOperationInfo.getInput();
        assertNotNull(inputMessage);
        assertEquals(inputMessage.getMessageInfo().getName().getLocalPart(), "testInputMessage");
        assertEquals(inputMessage.getMessageInfo().getName().getNamespaceURI(), 
                     "http://objectweb.org/hello_world_soap_http");
    }
    
    public void testOutputMessage() throws Exception {
        BindingMessageInfo outputMessage = bindingOperationInfo.getOutput();
        assertNotNull(outputMessage);
        assertEquals(outputMessage.getMessageInfo().getName().getLocalPart(), "testOutputMessage");
        assertEquals(outputMessage.getMessageInfo().getName().getNamespaceURI(), 
                     "http://objectweb.org/hello_world_soap_http");
    }
    
    public void testFaultMessage() throws Exception {
        BindingFaultInfo faultMessage = bindingOperationInfo.getFaults().iterator().next();
        assertNotNull(faultMessage);
        assertEquals(faultMessage.getFaultInfo().getName().getLocalPart(), "faultMessage");
        assertEquals(faultMessage.getFaultInfo().getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
    }
}
