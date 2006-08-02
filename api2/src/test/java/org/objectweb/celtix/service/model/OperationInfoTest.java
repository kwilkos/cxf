package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class OperationInfoTest extends TestCase {

    private OperationInfo operationInfo;
    
    public void setUp() throws Exception {
        operationInfo = new OperationInfo(null, new QName("urn:test:ns", "operationTest"));
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertNull(operationInfo.getInterface());
        assertEquals("operationTest", operationInfo.getName().getLocalPart());
        operationInfo.setName(new QName("urn:test:ns", "operationTest2"));
        assertEquals("operationTest2", operationInfo.getName().getLocalPart());
        try {
            operationInfo.setName(null);
            fail("should catch IllegalArgumentException since name is null");
        } catch (NullPointerException e) {
            // intentionally empty
        }
    }
    
    public void testInput() throws Exception {
        assertFalse(operationInfo.hasInput());
        MessageInfo inputMessage = operationInfo.createMessage(new QName(
            "http://objectweb.org/hello_world_soap_http", "testInputMessage"));
        operationInfo.setInput("input", inputMessage);
        assertTrue(operationInfo.hasInput());
        inputMessage = operationInfo.getInput();
        assertEquals("testInputMessage", inputMessage.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http",
                     inputMessage.getName().getNamespaceURI());
        assertEquals(operationInfo.getInputName(), "input");
    }
    
    public void testOutput() throws Exception {
        assertFalse(operationInfo.hasOutput());
        MessageInfo outputMessage = operationInfo.createMessage(new QName(
            "http://objectweb.org/hello_world_soap_http", "testOutputMessage"));
        operationInfo.setOutput("output", outputMessage);
        assertTrue(operationInfo.hasOutput());
        outputMessage = operationInfo.getOutput();
        assertEquals("testOutputMessage", outputMessage.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http",
                     outputMessage.getName().getNamespaceURI());
        assertEquals(operationInfo.getOutputName(), "output");
    }
    
    public void testOneWay() throws Exception {
        assertFalse(operationInfo.isOneWay());
        MessageInfo inputMessage = operationInfo.createMessage(new QName(
            "http://objectweb.org/hello_world_soap_http", "testInputMessage"));
        operationInfo.setInput("input", inputMessage);
        assertTrue(operationInfo.isOneWay());
    }
    
    public void testFault() throws Exception {
        assertEquals(operationInfo.getFaults().size(), 0);
        QName faultName = new QName("urn:test:ns", "fault");
        operationInfo.addFault(faultName, new QName(
            "http://objectweb.org/hello_world_soap_http", "faultMessage"));
        assertEquals(operationInfo.getFaults().size(), 1);
        FaultInfo fault = operationInfo.getFault(faultName);
        assertNotNull(fault);
        assertEquals(fault.getFaultName().getLocalPart(), "fault");
        assertEquals(fault.getName().getLocalPart(), "faultMessage");
        assertEquals(fault.getName().getNamespaceURI(), 
                     "http://objectweb.org/hello_world_soap_http");
        operationInfo.removeFault(faultName);
        assertEquals(operationInfo.getFaults().size(), 0);
    }
}
