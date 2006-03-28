package org.objectweb.celtix.geronimo.container;


import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import junit.framework.TestCase;

import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.geronimo.MockBusFactory;
import org.objectweb.celtix.geronimo.builder.PortInfo;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class CeltixWebServiceContainerTest extends TestCase {

    Request req = EasyMock.createNiceMock(Request.class);
    Response resp = EasyMock.createNiceMock(Response.class);
    
    GeronimoServerTransport transport = EasyMock.createMock(GeronimoServerTransport.class);
    Bus mockBus;
    MockBusFactory mockBusFactory; 
    PortInfo portInfo; 
    
    public void setUp() throws Exception {
        mockBusFactory = new MockBusFactory();
        mockBus = mockBusFactory.createMockBus();

        portInfo = new PortInfo(); 
        portInfo.setWsdlFile("file:/tmp/foo.wsdl");
        portInfo.setServiceName(new QName("", "TestService").toString());

    }
    
    
    public void testInvoke() throws Exception { 

        // setup test fixture
        //
        BindingFactory bindingFact = mockBusFactory.getMock(BindingFactory.class);
        assertNotNull("could not get binding factory", bindingFact);
        transport.invoke(req, resp);        
        EasyMock.replay(transport);
        
        
        ServerBinding mockServerBinding = EasyMock.createMock(ServerBinding.class);
        
        bindingFact.createServerBinding((EndpointReferenceType)EasyMock.notNull(),
                                        (Endpoint)EasyMock.notNull(), 
                                        (ServerBindingEndpointCallback)EasyMock.notNull());
        EasyMock.expectLastCall().andReturn(mockServerBinding);
        mockServerBinding.activate(); 
        
        
        CeltixWebServiceContainer container = new CeltixWebServiceContainer(portInfo);
        container.setBus(mockBus);
        
        req.getAttribute(WebServiceContainer.POJO_INSTANCE);
        EasyMock.expectLastCall().andReturn(new TargetObject()); 
        EasyMock.replay(req);
        
        EasyMock.replay(mockServerBinding);       
        mockBusFactory.replay();
        container.setServerTransport(transport);
        
        // invoke the method under test 
        container.invoke(req, resp);
       
        // verify expectations 
        //
        EasyMock.verify(transport);
        EasyMock.verify(mockBus);
        // TODO : fix this
//        EasyMock.verify(bindingFact);
//        EasyMock.verify(mockServerBinding);
        
    }

    
    public void testStart() throws Exception {

        GeronimoTransportFactory factory = new GeronimoTransportFactory();
        
        TransportFactoryManager tfm = mockBusFactory.getMock(TransportFactoryManager.class);
        assertNotNull("could not get TransportFactoryManager", tfm);
        
        tfm.getTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/");
        EasyMock.expectLastCall().andReturn(factory);
        tfm.getTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/http");
        EasyMock.expectLastCall().andReturn(factory);
        tfm.getTransportFactory("http://celtix.objectweb.org/transports/http/configuration");
        EasyMock.expectLastCall().andReturn(factory);
        
        mockBusFactory.replay();
        
        CeltixWebServiceContainer cntr = new CeltixWebServiceContainer(null);
        cntr.setBus(mockBusFactory.getBus());        
        cntr.doStart();

        EasyMock.verify(mockBus);
    }
    
    static class TargetObject {
        public String sayHi() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
