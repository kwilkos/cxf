package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class BindingFaultInfoTest extends TestCase {
    
    private BindingFaultInfo bindingFaultInfo;
    
    public void setUp() throws Exception {
        FaultInfo faultInfo = new FaultInfo("fault", new QName(
            "http://objectweb.org/hello_world_soap_http", "faultMessage"), null);
        bindingFaultInfo = new BindingFaultInfo(faultInfo, null);
    }

    public void testBindingFaultInfo() {
        assertNotNull(bindingFaultInfo.getFaultInfo());
        assertNull(bindingFaultInfo.getBindingOperation());
        assertEquals(bindingFaultInfo.getFaultInfo().getFaultName(), "fault");
        assertEquals(bindingFaultInfo.getFaultInfo().getName().getLocalPart(), "faultMessage");
        assertEquals(bindingFaultInfo.getFaultInfo().getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
    }
}
