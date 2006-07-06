package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnection;
import org.objectweb.celtix.jca.celtix.ManagedConnectionFactoryImpl;
import org.objectweb.celtix.jca.celtix.ManagedConnectionImpl;

public class HandlerTestBase extends TestCase {
    protected Bus mockBus = EasyMock.createMock(Bus.class);
    protected CeltixManagedConnection mockManagedConnection = 
                EasyMock.createMock(CeltixManagedConnection.class);

    protected CeltixInvocationHandler mockHandler = 
                EasyMock.createMock(CeltixInvocationHandler.class);

    protected ManagedConnectionFactoryImpl mcf = 
                EasyMock.createMock(ManagedConnectionFactoryImpl.class);
    protected ManagedConnectionImpl mci =
                EasyMock.createMock(ManagedConnectionImpl.class);
    protected Method testMethod;
    protected TestTarget target = new TestTarget();
    
    public HandlerTestBase(String aName) {
        super(aName);
    }

    public void setUp() {
        EasyMock.reset(mcf);
        EasyMock.reset(mci);
    
        mcf.getBus();
        EasyMock.expectLastCall().andReturn(mockBus);
        EasyMock.replay(mcf);
        
        mci.getManagedConnectionFactory();
        EasyMock.expectLastCall().andReturn(mcf);
        EasyMock.replay(mci);
        try {
            testMethod = TestTarget.class.getMethod("testMethod", new Class[0]);
        } catch (NoSuchMethodException ex) {
            fail(ex.toString());
        }
        
    }

    public void testNullTestTarget() {
       // do nothing here ,just for avoid the junit test warning
    }
    
}
