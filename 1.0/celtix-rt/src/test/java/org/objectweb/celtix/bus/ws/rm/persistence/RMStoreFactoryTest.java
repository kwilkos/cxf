package org.objectweb.celtix.bus.ws.rm.persistence;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.StoreInitParamType;
import org.objectweb.celtix.bus.configuration.wsrm.StoreType;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.persistence.file.RMFileStore;
import org.objectweb.celtix.configuration.Configuration;
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
        EasyMock.expectLastCall().andReturn("org.objectweb.celtix.bus.ws.rm.persistence.file.RMFileStore");
        s.getInitParam();
        EasyMock.expectLastCall().andReturn(new ArrayList<StoreInitParamType>());
        control.replay();
        
        RMStoreFactory factory = new RMStoreFactory();
        RMStore store = factory.getStore(c);
        assert store instanceof RMFileStore;
   
        control.verify();
    }
    
    public void testStoreCreationWithParams() {
        IMocksControl control = EasyMock.createNiceControl(); 
        Configuration c = control.createMock(Configuration.class);  
        StoreType s = RMUtils.getWSRMConfFactory().createStoreType();
        s.setStoreClass("org.objectweb.celtix.bus.ws.rm.persistence.file.RMFileStore");
        StoreInitParamType param = RMUtils.getWSRMConfFactory().createStoreInitParamType();
        param.setParamName(RMFileStore.FILE_STORE_DIR);
        param.setParamValue("dbs/rm");
        s.getInitParam().add(param);
        param = RMUtils.getWSRMConfFactory().createStoreInitParamType();
        param.setParamName("prop2");
        param.setParamValue("val2");
        s.getInitParam().add(param);
        c.getObject(StoreType.class, "store");
        EasyMock.expectLastCall().andReturn(s);
        control.replay();
        
        RMStoreFactory factory = new RMStoreFactory();
        RMStore store = factory.getStore(c);
        assert store instanceof RMFileStore;
        
        control.verify();
    }
}
