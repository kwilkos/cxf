package org.objectweb.celtix.ws.rm;

import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.createMock;

public class RMEndpointTest extends TestCase {
    
    private RMHandler handler;
    
    public void setUp() {
        handler = createMock(RMHandler.class);
    }
    
    public void testGenerateSequenceIndentifier() {
        RMEndpoint e = new RMEndpoint(handler);
        assertSame(handler, e.getHandler());   
        Identifier sid1 = e.generateSequenceIdentifier();
        assertNotNull(sid1.getValue());
        Identifier sid2 = e.generateSequenceIdentifier();
        assertTrue(!sid1.equals(sid2));
    }
}
