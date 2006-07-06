package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;



public class ObjectMethodInvocationHandlerTest extends AbstractInvocationHandlerTest {

    ObjectMethodInvocationHandler handler; 
    CeltixInvocationHandlerData data;

    TestTarget testTarget = new TestTarget(); 
    DummyHandler dummyHandler = new DummyHandler();
    
    public ObjectMethodInvocationHandlerTest(String name) {
        super(name);
    }


    public void setUp() { 
        super.setUp(); 
        target.lastMethod = null; 
        dummyHandler.invokeCalled = false;         
        data = new CeltixInvocationHandlerDataExtend();
        data.setTarget(target);
        handler = new ObjectMethodInvocationHandler(data);
        handler.setNext((CeltixInvocationHandler)dummyHandler); 
    } 

    
    public void testToString() throws Throwable  { 

        Method toString = Object.class.getMethod("toString", new Class[0]); 
        Object result = handler.invoke(testTarget, toString, null); 
        assertTrue("object method must not be passed to next handler in chain", 
                   !dummyHandler.invokeCalled); 
        assertTrue("object must be a String", result instanceof String);
        assertTrue("checking toString method ", ((String)result).startsWith("ConnectionHandle"));
    } 


    public void xtestHashCode() throws Throwable { 

        Method hashCode = Object.class.getMethod("hashCode", new Class[0]); 
        doObjectMethodTest(hashCode); 
    } 

    
    public void testEqualsDoesNotCallNext() throws Throwable { 

        Method equals = Object.class.getMethod("equals", new Class[] {Object.class}); 
        handler.invoke(testTarget, equals, new Object[] {this}); 
        assertTrue("object method must not be passed to next handler in chain", 
                   !dummyHandler.invokeCalled); 
    } 

    public void testNonObjecMethod() throws Throwable { 

        DummyHandler dummyHandler1 = new DummyHandler(); 
        handler.setNext((CeltixInvocationHandler)dummyHandler1); 

        final Method method = TestTarget.class.getMethod("testMethod", new Class[0]); 
        
        handler.invoke(testTarget, method, new Object[0]); 

        assertTrue("non object method must be passed to next handler in chain", dummyHandler1.invokeCalled); 
    }


    public void testEqualsThroughProxies() { 

        Class[] interfaces = {TestInterface.class};
        CeltixInvocationHandlerData data1 = new CeltixInvocationHandlerDataExtend();
        CeltixInvocationHandlerData data2 = new CeltixInvocationHandlerDataExtend();
        data1.setTarget(new TestTarget());
        data2.setTarget(new TestTarget());
        ObjectMethodInvocationHandler handler1 = new ObjectMethodInvocationHandler(data1); 
        handler1.setNext((CeltixInvocationHandler)mockHandler); 
        ObjectMethodInvocationHandler handler2 = new ObjectMethodInvocationHandler(data2); 
        handler2.setNext((CeltixInvocationHandler)mockHandler); 

        TestInterface proxy1 = 
            (TestInterface)Proxy.newProxyInstance(TestInterface.class.getClassLoader(), interfaces, handler1);
        TestInterface proxy2 = 
            (TestInterface)Proxy.newProxyInstance(TestInterface.class.getClassLoader(), interfaces, handler2);

        assertEquals(proxy1, proxy1); 
        assertTrue(!proxy1.equals(proxy2)); 
    } 


    protected void doObjectMethodTest(Method method) throws Throwable { 
        doObjectMethodTest(method, null); 
    } 

    protected void doObjectMethodTest(Method method, Object[] args) throws Throwable { 

        handler.invoke(testTarget, method, args); 

        assertTrue("object method must not be passed to next handler in chain",
                   dummyHandler.invokeCalled); 
        assertEquals(method + " must be invoked directly on target object",
                     method.getName(), target.lastMethod.getName()); 
    } 



    public CeltixInvocationHandler getHandler() { 
        return handler;
    } 

      
}

