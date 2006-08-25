package org.objectweb.celtix.bus.bindings.xml;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLServerBindingTest extends TestCase {

    private Bus bus;
    private EndpointReferenceType epr;
    
    public void setUp() throws Exception {
        bus = Bus.init();
        TestUtils testUtils = new TestUtils();
        epr = testUtils.getWrappedReference();
    }
    
    public void testCreateServerBinding() throws Exception {
        XMLServerBinding serverBinding = new XMLServerBinding(bus, epr, null, null);
        assertNotNull(serverBinding.getBinding());
    }
}
