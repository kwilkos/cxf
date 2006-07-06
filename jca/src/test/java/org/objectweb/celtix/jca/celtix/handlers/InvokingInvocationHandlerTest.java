package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.Proxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;

public class InvokingInvocationHandlerTest 
    extends AbstractInvocationHandlerTest {

    TestInterface test;
    TestInterface test2;
    TestTarget target;
    CeltixInvocationHandler handler;
    CeltixInvocationHandlerData data;

    public InvokingInvocationHandlerTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        target = new TestTarget();
        data = new CeltixInvocationHandlerDataExtend();
        data.setTarget(target);
        handler = new InvokingInvocationHandler(data);
        Class[] interfaces = {TestInterface.class};

        test = (TestInterface)Proxy.newProxyInstance(TestInterface.class.getClassLoader(), interfaces,
                                                     handler);
        handler.getData().setTarget(target);

        CeltixInvocationHandlerData data2 = new CeltixInvocationHandlerDataExtend();
        CeltixInvocationHandler handler2 = new InvokingInvocationHandler(data2);
        test2 = (TestInterface)Proxy.newProxyInstance(TestInterface.class.getClassLoader(), interfaces,
                                                      handler2);
        handler2.getData().setTarget(target);
    }

    /**
     * override this test - this handler is alway the last in the chain and is
     * responsible for delegating the invocation to the target object
     */
    public void testHandlerInvokesNext() throws Throwable {
        assertTrue("target method  must not have be called", !target.methodInvoked);
        handler.invoke(target, testMethod, new Object[0]);
        assertTrue("target method must be called", target.methodInvoked);
    }

    public void testInvocationThroughProxy() throws IllegalArgumentException {

        assertTrue("target object must no have been invoked", !target.methodInvoked);
        test.testMethod();
        assertTrue("target object must be invoked", target.methodInvoked);
    }

    protected CeltixInvocationHandler getHandler() {

        return handler;
    }

    public static Test suite() {
        return new TestSuite(InvokingInvocationHandlerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {InvokingInvocationHandlerTest.class.getName()});
    }
}
