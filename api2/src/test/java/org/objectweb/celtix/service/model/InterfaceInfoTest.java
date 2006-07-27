package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class InterfaceInfoTest extends TestCase {
    
    private InterfaceInfo interfaceInfo;

    public void setUp() throws Exception {
        interfaceInfo = new InterfaceInfo(null, new QName(
            "http://objectweb.org/hello_world_soap_http", "interfaceTest"));
    }
    
    public void testName() throws Exception {
        assertEquals(interfaceInfo.getName().getLocalPart(), "interfaceTest");
        assertEquals(interfaceInfo.getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        QName qname = new QName(
             "http://objectweb.org/hello_world_soap_http1", "interfaceTest1");
        interfaceInfo.setName(qname);
        assertEquals(interfaceInfo.getName().getLocalPart(), "interfaceTest1");
        assertEquals(interfaceInfo.getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http1");
    }
 
    
    public void testOperation() throws Exception {
        interfaceInfo.addOperation("sayHi");
        assertEquals("sayHi", interfaceInfo.getOperation("sayHi").getName());
        interfaceInfo.addOperation("greetMe");
        assertEquals(interfaceInfo.getOperations().size(), 2);
    }
    
}
