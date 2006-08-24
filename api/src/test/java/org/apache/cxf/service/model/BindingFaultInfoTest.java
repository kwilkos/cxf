package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class BindingFaultInfoTest extends TestCase {
    
    private BindingFaultInfo bindingFaultInfo;
    
    public void setUp() throws Exception {
        FaultInfo faultInfo = new FaultInfo(new QName("http://faultns/", "fault"), new QName(
            "http://apache.org/hello_world_soap_http", "faultMessage"), null);
        bindingFaultInfo = new BindingFaultInfo(faultInfo, null);
    }

    public void testBindingFaultInfo() {
        assertNotNull(bindingFaultInfo.getFaultInfo());
        assertNull(bindingFaultInfo.getBindingOperation());
        assertEquals(bindingFaultInfo.getFaultInfo().getFaultName(), new QName("http://faultns/", "fault"));
        assertEquals(bindingFaultInfo.getFaultInfo().getName().getLocalPart(), "faultMessage");
        assertEquals(bindingFaultInfo.getFaultInfo().getName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http");
    }
}
