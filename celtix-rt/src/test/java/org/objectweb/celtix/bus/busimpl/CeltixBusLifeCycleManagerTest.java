package org.objectweb.celtix.bus.busimpl;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;

public class CeltixBusLifeCycleManagerTest extends TestCase {

    public void testListenerNotRegistered() {

        BusLifeCycleListener listener1 = EasyMock.createMock(BusLifeCycleListener.class);
        CeltixBusLifeCycleManager mgr = new CeltixBusLifeCycleManager();

        EasyMock.reset(listener1);
        EasyMock.replay(listener1);
        mgr.initComplete();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        EasyMock.replay(listener1);
        mgr.preShutdown();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        EasyMock.replay(listener1);
        mgr.postShutdown();
        EasyMock.verify(listener1);
    }
    
    public void testSingleListenerRegistration() {

        BusLifeCycleListener listener1 = EasyMock.createMock(BusLifeCycleListener.class);
        CeltixBusLifeCycleManager mgr = new CeltixBusLifeCycleManager();
        
        mgr.registerLifeCycleListener(listener1);

        EasyMock.reset(listener1);
        listener1.initComplete();
        EasyMock.replay(listener1);
        mgr.initComplete();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        listener1.preShutdown();
        EasyMock.replay(listener1);
        mgr.preShutdown();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        listener1.postShutdown();
        EasyMock.replay(listener1);
        mgr.postShutdown();
        EasyMock.verify(listener1);        
    }
    
    public void testDuplicateRegistration() {
        
        BusLifeCycleListener listener1 = EasyMock.createMock(BusLifeCycleListener.class);
        CeltixBusLifeCycleManager mgr = new CeltixBusLifeCycleManager();

        mgr.registerLifeCycleListener(listener1);
        mgr.registerLifeCycleListener(listener1);

        EasyMock.reset(listener1);
        listener1.initComplete();
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener1);
        mgr.initComplete();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        listener1.preShutdown();
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener1);
        mgr.preShutdown();
        EasyMock.verify(listener1);

        EasyMock.reset(listener1);
        listener1.postShutdown();
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener1);
        mgr.postShutdown();
        EasyMock.verify(listener1);
    }
    
    public void testMultipleListeners() {
       
        IMocksControl ctrl = EasyMock.createStrictControl();
        
        BusLifeCycleListener listener1 = ctrl.createMock(BusLifeCycleListener.class);
        BusLifeCycleListener listener2 = ctrl.createMock(BusLifeCycleListener.class);
        CeltixBusLifeCycleManager mgr = new CeltixBusLifeCycleManager();

        mgr.registerLifeCycleListener(listener1);
        mgr.registerLifeCycleListener(listener2);
        
        ctrl.reset();
        listener1.initComplete();
        listener2.initComplete();
        ctrl.replay();
        mgr.initComplete();
        ctrl.verify();
        
        ctrl.reset();
        listener1.preShutdown();
        listener2.preShutdown();
        ctrl.replay();
        mgr.preShutdown();
        ctrl.verify();
        
        ctrl.reset();
        listener1.postShutdown();
        listener2.postShutdown();
        ctrl.replay();
        mgr.postShutdown();
        ctrl.verify();
    }
    
    public void testDeregistration() {
        
        IMocksControl ctrl = EasyMock.createStrictControl();
        
        BusLifeCycleListener listener1 = ctrl.createMock(BusLifeCycleListener.class);
        BusLifeCycleListener listener2 = ctrl.createMock(BusLifeCycleListener.class);
        CeltixBusLifeCycleManager mgr = new CeltixBusLifeCycleManager();

        mgr.registerLifeCycleListener(listener2);
        mgr.registerLifeCycleListener(listener1);
        mgr.unregisterLifeCycleListener(listener2);
        
        ctrl.reset();
        listener1.initComplete();
        ctrl.replay();
        mgr.initComplete();
        ctrl.verify();
        
        ctrl.reset();
        listener1.preShutdown();
        ctrl.replay();
        mgr.preShutdown();
        ctrl.verify();
        
        ctrl.reset();
        listener1.postShutdown();
        ctrl.replay();
        mgr.postShutdown();
        ctrl.verify();
    }
}
