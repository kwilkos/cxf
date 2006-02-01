package org.objectweb.celtix.bus.ws.rm;

import junit.framework.TestCase;

public class RMUtilsTest extends TestCase {
    public void testRMUtils() {
        assertNotNull(RMUtils.getRMConstants());
        assertNotNull(RMUtils.getWSAFactory());
        assertNotNull(RMUtils.getWSRMConfFactory());
        assertNotNull(RMUtils.getWSRMFactory());
    }
}
