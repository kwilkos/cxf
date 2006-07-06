package org.objectweb.celtix.jca.celtix.handlers;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnection;


public class  ProxyInvocationHandlerTest extends AbstractInvocationHandlerTest {

    ProxyInvocationHandler testObject;
    CeltixInvocationHandlerData data;

    public ProxyInvocationHandlerTest(String name) {
        super(name);
    }


    @SuppressWarnings("unchecked")
    public void setUp() { 
        super.setUp(); 
        data = new CeltixInvocationHandlerDataExtend();
        testObject = new ProxyInvocationHandler(data);
        testObject.getData().setManagedConnection((CeltixManagedConnection)mci);
        assertTrue(testObject instanceof CeltixInvocationHandlerBase); 
    } 


    public CeltixInvocationHandler getHandler() { 
        return testObject;
    }

   
    public void testInvokeSetsBusCurrent() throws Throwable {
        
        testObject.invoke(target, testMethod, new Object[] {});
        
        Bus b = Bus.getCurrent();
       
        assertSame("Current Bus has been set and is as expected, val=" + b, b, mockBus);
    }

    public static Test suite() {
        return new TestSuite(ProxyInvocationHandlerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {ProxyInvocationHandlerTest.class.getName()});
    }
}





