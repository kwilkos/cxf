package org.objectweb.celtix.ws.addressing;


import junit.framework.TestCase;

public class AddressingBuilderImplTest extends TestCase {
    private AddressingBuilderImpl builder;

    public void setUp() {
        builder = new AddressingBuilderImpl();
    }

    public void testGetAddressingProperties() throws Exception {
        AddressingProperties properties = builder.newAddressingProperties();
        assertNotNull("expected AddressingProperties ", properties);
        assertNotSame("unexpected same properties",
                      builder.newAddressingProperties(),
                      properties);
    }

    public void testGetAddressingConstants() throws Exception {
        AddressingConstants constants = builder.newAddressingConstants();
        assertNotNull("expected AddressingConstants ", constants);
        assertNotSame("unexpected same constants",
                      builder.newAddressingConstants(),
                      constants);
    }
}
