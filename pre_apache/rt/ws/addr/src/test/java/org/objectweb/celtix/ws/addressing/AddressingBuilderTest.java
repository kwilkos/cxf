package org.objectweb.celtix.ws.addressing;

import junit.framework.TestCase;


public class AddressingBuilderTest extends TestCase {
    public void testGetAddressingBuilder() throws Exception {
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        assertNotNull("expected AddressingBuilder", builder);
        assertSame("expected same builder",
                   AddressingBuilder.getAddressingBuilder(),
                   builder);
    }
}
