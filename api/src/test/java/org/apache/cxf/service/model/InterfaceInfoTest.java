package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class InterfaceInfoTest extends TestCase {
    
    private InterfaceInfo interfaceInfo;

    public void setUp() throws Exception {
        interfaceInfo = new InterfaceInfo(null, new QName(
            "http://apache.org/hello_world_soap_http", "interfaceTest"));
    }
    
    public void testName() throws Exception {
        assertEquals(interfaceInfo.getName().getLocalPart(), "interfaceTest");
        assertEquals(interfaceInfo.getName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http");
        QName qname = new QName(
             "http://apache.org/hello_world_soap_http1", "interfaceTest1");
        interfaceInfo.setName(qname);
        assertEquals(interfaceInfo.getName().getLocalPart(), "interfaceTest1");
        assertEquals(interfaceInfo.getName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http1");
    }
 
    
    public void testOperation() throws Exception {
        QName name = new QName("urn:test:ns", "sayHi");
        interfaceInfo.addOperation(name);
        assertEquals("sayHi", interfaceInfo.getOperation(name).getName().getLocalPart());
        interfaceInfo.addOperation(new QName("urn:test:ns", "greetMe"));
        assertEquals(interfaceInfo.getOperations().size(), 2);
        boolean duplicatedOperationName = false;
        try {
            interfaceInfo.addOperation(name);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), 
                "An operation with name [{urn:test:ns}sayHi] already exists in this service");
            duplicatedOperationName = true;
        }
        if (!duplicatedOperationName) {
            fail("should get IllegalArgumentException");
        }
        boolean isNull = false;
        try {
            QName qname = null;
            interfaceInfo.addOperation(qname);
        } catch (NullPointerException e) {
            isNull = true;
            assertEquals(e.getMessage(), "Operation Name cannot be null.");
        }
        if (!isNull) {
            fail("should get NullPointerException");
        }
    }
    
}
