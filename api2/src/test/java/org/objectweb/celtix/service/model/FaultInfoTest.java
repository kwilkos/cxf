package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class FaultInfoTest extends TestCase {
    
    private FaultInfo faultInfo;
    
    public void setUp() throws Exception {
        faultInfo = new FaultInfo("fault", new QName(
             "http://objectweb.org/hello_world_soap_http", "faultMessage"), null);
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertEquals(faultInfo.getFaultName(), "fault");
        assertEquals(faultInfo.getName().getLocalPart(), "faultMessage");
        assertEquals(faultInfo.getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        
        faultInfo.setFaultName("fault2");
        assertEquals(faultInfo.getFaultName(), "fault2");
    }
}
