package org.objectweb.celtix.bus.ws.rm;

import junit.framework.TestCase;

public class RMUtilsTest extends TestCase {
    public void testRMUtils() {
        assertNotNull(RMUtils.getAddressingConstants());
        assertNotNull(RMUtils.getPersistenceUtils());
        assertNotNull(RMUtils.getPolicyConstants());
        assertNotNull(RMUtils.getRMConstants());
        assertNotNull(RMUtils.getWSAFactory());
        assertNotNull(RMUtils.getWSRMConfFactory());
        assertNotNull(RMUtils.getWSRMFactory());
        assertNotNull(RMUtils.getWSRMPolicyFactory());
    }
    
    public void testStatic() {
        new RMUtils();
        new Names();
        new RMContextUtils();
    }
}
