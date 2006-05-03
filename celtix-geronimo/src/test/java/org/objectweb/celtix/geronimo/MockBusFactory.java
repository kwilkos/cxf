package org.objectweb.celtix.geronimo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.ws.Binding;

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
import org.objectweb.celtix.transports.TransportFactoryManager;
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
    private EndpointRegistry mockEndpointRegistry;
    private Binding mockBinding; 
    private ResourceManager mockResourceManager; 
    private TransportFactoryManager mockTransportFactoryManager; 
    
    private Map mocks = new HashMap();
    
    public void replay() {
        EasyMock.replay(mockBus); 
        EasyMock.replay(mockConfiguration); 
        EasyMock.replay(mockBindingMgr); 
        EasyMock.replay(mockServerBinding); 
        EasyMock.replay(mockBindingFactory);
        EasyMock.replay(mockWsdlManager);
        EasyMock.replay(mockExtensionRegistry);        
        EasyMock.replay(mockLifeCycleManager);        
        EasyMock.replay(mockEndpointRegistry);        
        EasyMock.replay(mockBinding);        
        EasyMock.replay(mockResourceManager);        
        EasyMock.replay(mockTransportFactoryManager);        
    }

    public <T> T getMock(Class<T> type) {
        return type.cast(mocks.get(type));
    }

    public Bus createMockBus() throws BusException, WSDLException, IOException {

        mockBus = createNiceMock(Bus.class);
        mockConfiguration = createNiceMock(Configuration.class);       
        mockBindingMgr = createNiceMock(BindingManager.class);
        mockBindingFactory = createNiceMock(BindingFactory.class);
        mockServerBinding = createNiceMock(ServerBinding.class);
        mockWsdlManager = createNiceMock(WSDLManager.class);
        mockExtensionRegistry = createNiceMock(ExtensionRegistry.class);
        mockLifeCycleManager = createNiceMock(BusLifeCycleManager.class);       
        mockEndpointRegistry = createNiceMock(EndpointRegistry.class);   
        mockBinding = createNiceMock(Binding.class);
        mockResourceManager = createNiceMock(ResourceManager.class);
        mockTransportFactoryManager = createNiceMock(TransportFactoryManager.class); 
        
        EasyMock.expect(mockBus.getResourceManager())
            .andReturn(mockResourceManager).anyTimes();        
        EasyMock.expect(mockBus.getConfiguration())
            .andReturn(mockConfiguration).anyTimes();
        EasyMock.expect(mockBus.getBindingManager())
            .andReturn(mockBindingMgr).anyTimes();
        
        EasyMock.expect(mockBus.getEndpointRegistry())
            .andReturn(mockEndpointRegistry).anyTimes();
        
        EasyMock.expect(mockBindingMgr.getBindingFactory((String)EasyMock.anyObject()))        
                .andReturn(mockBindingFactory).anyTimes();

        EasyMock.expect(mockBindingFactory.createServerBinding((EndpointReferenceType)EasyMock.anyObject(), 
                        (ServerBindingEndpointCallback)EasyMock.anyObject()))
                        .andReturn(mockServerBinding);
        
        EasyMock.expect(mockServerBinding.getBinding()).andReturn(mockBinding).anyTimes();
        
        EasyMock.expect(mockBus.getWSDLManager()).andReturn(mockWsdlManager).anyTimes();
        EasyMock.expect(mockWsdlManager.getExtenstionRegistry()).andReturn(mockExtensionRegistry)
                       .anyTimes();
        EasyMock.expect(mockBus.getLifeCycleManager()).andReturn(mockLifeCycleManager).anyTimes();
        EasyMock.expect(mockBus.getTransportFactoryManager())
                    .andReturn(mockTransportFactoryManager).anyTimes();

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
    
    @SuppressWarnings("unchecked")
    private <T> T createNiceMock(Class<T> type) {
        T ret = EasyMock.createNiceMock(type);
        mocks.put(type, ret);
        return ret;
    }


}
