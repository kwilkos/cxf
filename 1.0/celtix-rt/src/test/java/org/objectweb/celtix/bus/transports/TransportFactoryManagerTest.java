package org.objectweb.celtix.bus.transports;

import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ObjectFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public class TransportFactoryManagerTest extends TestCase {

    public TransportFactoryManagerTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TransportFactoryManagerTest.class);
    }

    public void testPlugableTransportFactoryManager() throws Exception {
        TransportFactoryManagerImpl transportFactoryManagerImpl = createTransportFactory();
        Map<String, Object> properties = new WeakHashMap<String, Object>();
        properties.put("celtix.TRANSPORTFACTORYMANAGER", transportFactoryManagerImpl);
        Bus bus = Bus.init(new String[0], properties);

        TransportFactoryManager transportFactoryManagerImplNew = bus.getTransportFactoryManager();

        //Verify that the TransportFactoryManager is the one we plugged into bus previously,
        //other than the one created inside Bus
        assertEquals("wsdlManager is the one we expected", transportFactoryManagerImpl,
            transportFactoryManagerImplNew);
    }

    private TransportFactoryManagerImpl createTransportFactory() throws BusException {
        Bus bus = EasyMock.createMock(Bus.class);
        Configuration bc = EasyMock.createMock(Configuration.class);

        ObjectFactory of = new ObjectFactory();
        ClassNamespaceMappingListType mappings = of.createClassNamespaceMappingListType();
        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(bc);
        bc.getObject("transportFactories");
        EasyMock.expectLastCall().andReturn(mappings);
        EasyMock.replay(bus);
        EasyMock.replay(bc);

        return new TransportFactoryManagerImpl(bus);
    }
}
