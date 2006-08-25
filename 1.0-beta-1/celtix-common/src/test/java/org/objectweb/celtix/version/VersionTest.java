package org.objectweb.celtix.version;

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public void testLoadProperties() {
        String version = Version.getCurrentVersion();
        String token = "${product.version}";
        assertFalse(token.equals(version));
    }

    public void testGetVersion() {
        String completeVersion =  Version.getCompleteVersionString();
        String currentVersion = Version.getCurrentVersion();
        assertEquals(completeVersion, currentVersion);
    }
}
