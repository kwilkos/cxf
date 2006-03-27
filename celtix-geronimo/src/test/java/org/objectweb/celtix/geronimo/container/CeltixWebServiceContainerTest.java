package org.objectweb.celtix.geronimo.container;


import junit.framework.TestCase;

import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.easymock.classextension.EasyMock;

public class CeltixWebServiceContainerTest extends TestCase {

    Request req = EasyMock.createNiceMock(Request.class);
    Response resp = EasyMock.createNiceMock(Response.class);
    
    GeronimoServerTransport transport = EasyMock.createMock(GeronimoServerTransport.class);
   
    
    public void testContainerDelegatesToTransport() throws Exception {

        transport.invoke(req, resp);
        
        EasyMock.replay(transport);
        
        CeltixWebServiceContainer container = new CeltixWebServiceContainer(); 
        container.setServerTransport(transport);
        container.invoke(req, resp);
        
        EasyMock.verify(transport);
    }
    
    public void testInvokeWithNoTransport() throws Exception { 
        try {
            CeltixWebServiceContainer container = new CeltixWebServiceContainer(); 
            container.invoke(req, resp);
            fail("did not receive expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
