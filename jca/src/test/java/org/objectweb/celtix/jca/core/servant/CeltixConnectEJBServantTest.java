package org.objectweb.celtix.jca.core.servant;

import java.lang.reflect.Method;
//import java.util.Properties;

//import javax.naming.Context;
//import javax.naming.InitialContext;

//import javax.ejb.EJBObject;
//import javax.naming.InitialContext;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.BusFactory;
import org.objectweb.celtix.jca.celtix.ManagedConnectionFactoryImpl;

public class CeltixConnectEJBServantTest extends TestCase { 
  
    protected CeltixConnectEJBServant ejbservant;

    public CeltixConnectEJBServantTest(String name) throws Exception {
        super(name);
    }

    public void setUp() throws Exception {
        Bus mockBus = createMockBus();
        ejbservant = createCeltixConnectEJBServant(mockBus);

       
        // Configuration.setConfiguration(new SampleConfiguration());
    }

    public void tearDown() {
       // DispatchLocals.setCurrentMessageContext(null);
    }

    public static Test suite() {
        return new TestSuite(CeltixConnectEJBServantTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[]{CeltixConnectEJBServantTest.class.getName()});
    }

    public void testConstructor() throws Exception {
        assertTrue("constructor works", 
                   createCeltixConnectEJBServant(createMockBus()) instanceof CeltixConnectEJBServant);
    }

    /*
    public void testGetTargetObjectSetsThreadContextClassloader() throws Exception {
        final ClassLoader cl = EasyMock.createMock(ClassLoader.class);
        final EJBObject ejb = EasyMock.createMock(EJBObject.class);
        InitialContext ic = EasyMock.createMock(InitialContext.class);
        
        final MockObject ejb = MockObjectFactory.create(EJBObject.class);
        
        MockObject rai = MockObjectFactory.create(BusFactory.class, 
            new Class[]{ManagedConnectionFactoryImpl.class}, 
            new Object[]{null});

        ic.setResult("lookup", new ThreadContextCheckerHome((Object)ejb, (ClassLoader)cl, this));
        
        rai.setResult("getInitialContext", ic);
        rai.setResult("getJAASLoginConfigName", null);
        rai.setResult("getJAASLoginUserName", null);
        rai.setResult("getJAASLoginPassword", null);
        rai.setResult("getAppserverClassLoader", (ClassLoader)cl);
        
        EJBServant testServant = createCeltixConnectEJBServant((BusFactory)rai, null);
        
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        assertTrue("thread classloader is not set before call, current=" + current,
                    !cl.equals(current));
        
        Object o = testServant.getTargetObject();
        assertSame("we got back out test object from create, o=" + o,
                   o, ejb);
        
        current = Thread.currentThread().getContextClassLoader();
        assertTrue("thread classloader is again not set after call, current=" + current,
                   !cl.equals(current));
    }
*/    
    /*
    public void testGetTartgetObject() throws Exception {
        Properties props = new Properties();
        String wsdlLocation =
            this.getClass().getResource("resources/hello_world.wsdl").toString();
        System.out.println(" ****  wsdlLocation: " + wsdlLocation);
        props.put("GreeterBean", "{http://objectweb.org/hello_world_soap_http}SOAPService@"
                  + wsdlLocation);
//        ejbservant.setProperties(props);
        InitialContext context = new InitialContext(props);
        ejbservant.setInitialContext(context);
//        System.out.println(" ***** set properties ok.");
        assertTrue("  some thing is wrong ", 
                   "GreeterBean".equals(ejbservant.getTargetObject().getClass().toString()));
    }
    */
    public void testServantInvoke() throws Exception { 

        Greeter target = new GreeterImpl(); 
        Method method = target.getClass().getMethod("sayHi", new Class[0]); 
        ejbservant.invoke(target, method, new Object[0]);
        assertTrue("target method invoked", ((GreeterImpl)target).getSayHiCalled()); 
    }

    /*
    public void testServantInvokeThrowBusExceptionIfEJBThrowRuntimeException() throws Exception {
        final String msg = "jjljljlj";
        GreeterImpl target = new GreeterImpl();
        target.mockInvoke = new MockInvoke() {
                public Object invoke(Object[] args) throws Exception {
                    throw new NullPointerException(msg);
                }
        };

        Method method = target.getClass().getMethod("sayHi", new Class[0]);
        try {
            ejbservant.invoke(target, method, new Object[0]);
            fail("exception expected");
        } catch (Exception ex) {
            assertTrue("target method invoked. ex: " + ex, target.sayHiCalled);
            assertTrue("cause is RuntimeException", ex.getCause() instanceof RuntimeException);
            assertTrue("exception message contains " + msg, ex.getMessage().indexOf(msg)!=-1);
        }
    }
*/
    protected Bus createMockBus() {
        return EasyMock.createMock(Bus.class);
    }

    protected CeltixConnectEJBServant createCeltixConnectEJBServant(Bus bus) throws Exception {
        BusFactory bf = new BusFactory(new ManagedConnectionFactoryImpl());
        return createCeltixConnectEJBServant(bf, bus);
    }

    protected CeltixConnectEJBServant createCeltixConnectEJBServant(BusFactory bf, Bus bus) throws Exception {
        bf.setBus(bus);
        return new CeltixConnectEJBServant(bf, "", "");
    }

}



