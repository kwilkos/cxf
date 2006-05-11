package org.objectweb.celtix.bus.ws.rm.persistence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.StoreInitParamType;
import org.objectweb.celtix.bus.configuration.wsrm.StoreType;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

public class RMStoreFactoryTest extends TestCase {
   
    public void setUp() {
        RMStoreFactory.theStore = null;
    }
    
    public void tearDown() {
        RMStoreFactory.theStore = null;
    }
    
    public void testStoreCreationFailure() {
        IMocksControl control = EasyMock.createNiceControl();      
        StoreType s = control.createMock(StoreType.class);
        s.getStoreClass();
        EasyMock.expectLastCall().andReturn("org.objectweb.celtix.bus.ws.rm.persistence.no.such.StoreClass");
        control.replay();
        
        RMStoreFactory factory = new RMStoreFactory();
        try {
            factory.createStore(s);
            fail("Expected RMStoreException was not thrown.");
        } catch (RMStoreException ex) {
            assert ex.getCause() instanceof ClassNotFoundException;
        }
        control.verify();
    }
    
    public void testStoreCreationNoParams() {
        IMocksControl control = EasyMock.createNiceControl(); 
        Configuration c = control.createMock(Configuration.class);        
        StoreType s = control.createMock(StoreType.class);
        c.getObject(StoreType.class, "store");
        EasyMock.expectLastCall().andReturn(s);
        s.getStoreClass();
        EasyMock.expectLastCall().andReturn(TestStore.class.getName());
        s.getInitParam();
        EasyMock.expectLastCall().andReturn(new ArrayList<StoreInitParamType>());
        control.replay();
        
        RMStoreFactory factory = new RMStoreFactory();
        RMStore store = factory.getStore(c);
        assert store instanceof TestStore;
        
        TestStore ts = (TestStore)store;
        assertEquals(0, ts.properties.keySet().size());
   
        control.verify();
    }
    
    public void testStoreCreationWithParams() {
        IMocksControl control = EasyMock.createNiceControl(); 
        Configuration c = control.createMock(Configuration.class);  
        StoreType s = RMUtils.getWSRMConfFactory().createStoreType();
        s.setStoreClass(TestStore.class.getName());
        StoreInitParamType param = RMUtils.getWSRMConfFactory().createStoreInitParamType();
        param.setParamName(TestStore.PARAM1);
        param.setParamValue("value1");
        s.getInitParam().add(param);
        param = RMUtils.getWSRMConfFactory().createStoreInitParamType();
        param.setParamName(TestStore.PARAM2);
        param.setParamValue("value2");
        s.getInitParam().add(param);
        c.getObject(StoreType.class, "store");
        EasyMock.expectLastCall().andReturn(s);
        control.replay();
        
        RMStoreFactory factory = new RMStoreFactory();
        RMStore store = factory.getStore(c);
        assert store instanceof TestStore;
        
        TestStore ts = (TestStore)store;
        assertEquals("value1", ts.properties.get("param1"));
        assertEquals("value2", ts.properties.get("param2"));
        
        control.verify();
    }
    
    static class TestStore implements RMStore {
        
        public static final String PARAM1 = "param1";
        public static final String PARAM2 = "param2";
        
        Map<String, String> properties;

        public void createDestinationSequence(RMDestinationSequence seq) {
            // TODO Auto-generated method stub
            
        }

        public void createSourceSequence(RMSourceSequence seq) {
            // TODO Auto-generated method stub
            
        }

        public Collection<RMDestinationSequence> getDestinationSequences(String endpointIdentifier) {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection<RMMessage> getMessages(Identifier sid, boolean outbound) {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier) {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Map<String, String> params) {
            if (null == properties) {
                properties = new HashMap<String, String>();                
            }
            for (String key : params.keySet()) {
                properties.put(key, params.get(key));
            }            
        }

        public void persistIncoming(RMDestinationSequence seq, RMMessage msg) {
            // TODO Auto-generated method stub
            
        }

        public void persistOutgoing(RMSourceSequence seq, RMMessage msg) {
            // TODO Auto-generated method stub
            
        }

        public void removeDestinationSequence(Identifier seq) {
            // TODO Auto-generated method stub
            
        }

        public void removeMessages(Identifier sid, Collection<BigInteger> messageNrs, boolean outbound) {
            // TODO Auto-generated method stub
            
        }

        public void removeSourceSequence(Identifier seq) {
            // TODO Auto-generated method stub
            
        }
        
    }
}
