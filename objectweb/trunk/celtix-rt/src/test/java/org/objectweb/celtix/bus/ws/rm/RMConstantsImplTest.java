package org.objectweb.celtix.bus.ws.rm;

import junit.framework.TestCase;

public class RMConstantsImplTest extends TestCase {
    private RMConstantsImpl constants;
    
    public void setUp() {
        constants = new RMConstantsImpl();
    }
    public void testGetNamespaceURI() {
        assertNotNull(constants.getNamespaceURI());
    }
     
    public void testGetRMPolicyNamespaceURI() {
        assertNotNull(constants.getRMPolicyNamespaceURI());
    }

    public void testGetWSDLNamespaceURI() {
        assertNotNull(constants.getWSDLNamespaceURI());
    }
    
    public void testGetCreateSequenceAction() {
        assertNotNull(constants.getCreateSequenceAction());
    }

    public void  testGetCreateSequenceResponseAction() {
        assertNotNull(constants.getCreateSequenceResponseAction());
    }
    
    public void testGetTerminateSequenceAction() {
        assertNotNull(constants.getTerminateSequenceAction());
    }
    
    public void testGetLastMessageAction() {
        assertNotNull(constants.getLastMessageAction());
    }
    
    public void testGetSequenceAcknowledgmentAction() {
        assertNotNull(constants.getSequenceAcknowledgmentAction());
    }
    
    public void testGetSequenceInfoAction() {
        assertNotNull(constants.getSequenceInfoAction());
    }
    
    public void testGetUnknownSequenceFaultCode() {
        assertNotNull(constants.getUnknownSequenceFaultCode());
    }
        
    public void testGetSequenceTerminatedFaultCode() {
        assertNotNull(constants.getSequenceTerminatedFaultCode());
    }
        
    public void testGetInvalidAcknowledgmentFaultCode() {
        assertNotNull(constants.getInvalidAcknowledgmentFaultCode());
    }
  
    public void testGetMessageNumberRolloverFaultCode() {
        assertNotNull(constants.getMessageNumberRolloverFaultCode());
    }
    
    public void testGetCreateSequenceRefusedFaultCode() {
        assertNotNull(constants.getCreateSequenceRefusedFaultCode());
    }
    
    public void testGetLastMessageNumberExceededFaultCode() {
        assertNotNull(constants.getLastMessageNumberExceededFaultCode());
    }
}
