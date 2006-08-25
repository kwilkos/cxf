package org.apache.cxf.transport.http;

import junit.framework.TestCase;

import org.apache.cxf.transport.http.JettyContextInspector;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.mortbay.http.HttpContext;


public class JettyContextInspectorTest extends TestCase {
    private static final String CONTEXT_PATH = "/foo/bar";
    private HttpContext context;
    private IMocksControl control;
    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
        context = control.createMock(HttpContext.class);
        context.getContextPath();
        EasyMock.expectLastCall().andReturn(CONTEXT_PATH);
        control.replay();
    }

    public void tearDown() {
        control.verify();
        control = null;
        context = null;
    }
    
    public void testGetAddress() throws Exception {
        JettyContextInspector inspector = new JettyContextInspector();
        assertEquals("unexpected address",
                     CONTEXT_PATH,
                     inspector.getAddress(context));
    }
}
