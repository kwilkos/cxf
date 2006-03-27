package org.objectweb.celtix.geronimo;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.jaxws.EndpointRegistry;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.WSDLManager;

public final class MockBusFactory {

    private Bus mockBus; 
    private Configuration mockConfiguration; 
    private BindingManager mockBindingMgr; 
    private ServerBinding mockServerBinding; 
    private BindingFactory mockBindingFactory; 
    private WSDLManager mockWsdlManager; 
    private ExtensionRegistry mockExtensionRegistry; 
    private BusLifeCycleManager mockLifeCycleManager; 
    
    
    public void replay() {
        EasyMock.replay(mockBus); 
        EasyMock.replay(mockConfiguration); 
        EasyMock.replay(mockBindingMgr); 
        EasyMock.replay(mockServerBinding); 
        EasyMock.replay(mockBindingFactory);
        EasyMock.replay(mockWsdlManager);
        EasyMock.replay(mockExtensionRegistry);        
        EasyMock.replay(mockLifeCycleManager);        
    }
    
    
    public Bus createMockBus() throws BusException, WSDLException, IOException {

        mockBus = EasyMock.createNiceMock(Bus.class);
        mockConfiguration = EasyMock.createNiceMock(Configuration.class);       
        mockBindingMgr = EasyMock.createNiceMock(BindingManager.class);
        mockBindingFactory = EasyMock.createNiceMock(BindingFactory.class);
        mockServerBinding = EasyMock.createNiceMock(ServerBinding.class);
        mockWsdlManager = EasyMock.createNiceMock(WSDLManager.class);
        mockExtensionRegistry = EasyMock.createNiceMock(ExtensionRegistry.class);
        mockLifeCycleManager = EasyMock.createNiceMock(BusLifeCycleManager.class);
        
        EndpointRegistry mockEndpoingRegistry = EasyMock.createNiceMock(EndpointRegistry.class);        
        Binding mockBinding = EasyMock.createNiceMock(Binding.class);
        
        ResourceManager resMgr = EasyMock.createNiceMock(ResourceManager.class);
        EasyMock.expect(mockBus.getResourceManager())
            .andReturn(resMgr).anyTimes();
        EasyMock.expect(mockBus.getConfiguration())
            .andReturn(mockConfiguration).anyTimes();
        EasyMock.expect(mockBus.getBindingManager())
            .andReturn(mockBindingMgr).anyTimes();
        
        EasyMock.expect(mockBus.getEndpointRegistry())
            .andReturn(mockEndpoingRegistry).anyTimes();
        
        EasyMock.expect(mockBindingMgr.getBindingFactory((String)EasyMock.anyObject()))        
                .andReturn(mockBindingFactory).anyTimes();

        EasyMock.expect(mockBindingFactory.createServerBinding((EndpointReferenceType)EasyMock.anyObject(), 
                        (Endpoint)EasyMock.isA(Endpoint.class),
                        (ServerBindingEndpointCallback)EasyMock.anyObject()))
                        .andReturn(mockServerBinding);
        
        EasyMock.expect(mockServerBinding.getBinding()).andReturn(mockBinding).anyTimes();
        
        EasyMock.expect(mockBus.getWSDLManager()).andReturn(mockWsdlManager).anyTimes();
        EasyMock.expect(mockWsdlManager.getExtenstionRegistry()).andReturn(mockExtensionRegistry)
                       .anyTimes();
        EasyMock.expect(mockBus.getLifeCycleManager()).andReturn(mockLifeCycleManager).anyTimes();

        return mockBus;     
    }

    public Configuration addChildConfig(String namespaceURI, Object id, Configuration childConfig) {

        if (childConfig == null) {
            childConfig = EasyMock.createNiceMock(Configuration.class);
        }

        EasyMock.expect(mockConfiguration.getChild(EasyMock.eq(namespaceURI), 
                                            id != null
                                            ? EasyMock.eq(id)
                                            : EasyMock.anyObject()))
                                            .andReturn(childConfig)
                                            .anyTimes();
        return childConfig;
    }

    public Bus getBus() {
        return mockBus;
    }
}
