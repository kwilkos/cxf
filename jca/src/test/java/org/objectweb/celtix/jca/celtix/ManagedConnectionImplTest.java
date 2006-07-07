package org.objectweb.celtix.jca.celtix;

import javax.resource.ResourceException;
//import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.connector.Connection;
import org.objectweb.hello_world_soap_http.Greeter;

public class ManagedConnectionImplTest extends ManagedConnectionTestBase {

    //MockObject mockIonaMessageContext = MockObjectFactory.create(IonaMessageContext.class);;

    //ContextRegistry mockContextRegistry = EasyMock.createMock(ContextRegistry.class);

    public ManagedConnectionImplTest(String name) {
        super(name);
    }

   
    public void testInstanceOfConnection() throws Exception {
        assertTrue("instance of Connection", mci instanceof Connection);
        ((Connection)mci).close();
    }

    public void testGetConnectionServiceGetPortThrows() throws Exception {
        final String exMsg = "cannot create client proxy";
        // throw the exception
        /*mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new BusException(exMsg));*/

        try {
            mci.getConnection(subj, cri);
        } catch (ResourceException re) {
            assertNotNull(re.getCause());
            assertTrue(re.getCause().toString(), re.getCause() instanceof BusException);
            assertEquals(re.getCause().toString(), re.getCause().getMessage(), exMsg);
        }
    }

    public void testThreadContextClassLoaderIsSet() throws Exception {
        /*mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new MockInvoke() {
                              public Object invoke(Object[] args) throws Throwable {
                                  assertNotNull("Context class loader is set", Thread.currentThread()
                                      .getContextClassLoader());
                                  assertEquals("Context class loader is correct", Thread.currentThread()
                                      .getContextClassLoader(), DummyBus.class.getClassLoader());

                                  return MockObjectFactory.create(Remote.class);
                              }
                          });*/
        mci.getConnection(subj, cri);
    }

    public void testGetConnectionWithNoWSDLInvokesCreateClientWithTwoParameters() throws Exception {

        //final QName qname = new QName("service");
        Object o = mci.getConnection(subj, cri);

        assertTrue("checking implementation of Connection interface", o instanceof Connection);
        assertTrue("checking implementation of passed interface", o instanceof Greeter);
    }
    /*
    public void testGetConnectionWithNoWSDLInvokesCreateClientWithTwoArgsThrowsBusException()
        throws Exception {

        final BusException be = new BusException(new Message("test", null, null));
        // thow be from the mock bus
        
        cri = new CeltixConnectionRequestInfo(Foo.class, null, new QName("service"), null);
        
        try {
            mci.getConnection(subj, cri);
            fail("Expected ResourceAdapterInternalException");
        } catch (ResourceAdapterInternalException raie) {
            assertTrue("asserting ResourceException.", raie.getMessage()
                .indexOf("Failed to create proxy client for ") != -1);
            assertSame("asserting ResourceException.", raie.getCause(), be);

        }

    }

    public void testGetConnectionWithNoPortReturnsConnection() throws Exception {

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new MockInvoke() {
                              public Object invoke(Object[] args) throws Throwable {
                                  throw new BusException("test");
                              }
                          });

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, Class.class},
                          new MockInvoke() {
                              public Object invoke(Object[] args) throws Throwable {
                                  return MockObjectFactory.create(Remote.class);
                              }
                          });

        cri = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo.wsdl"), new QName("service"),
                                             null);
        
        Object o = mci.getConnection(subj, cri);

        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Foo);
    }

    public void testGetConnectionReturnsConnection() throws ResourceException {
        Object o = mci.getConnection(subj, cri);
        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Foo);
    }

    private void verifyProxyInterceptors(Object o) {

        assertTrue(o instanceof Proxy);
        Proxy proxy = (Proxy)o;

        assertEquals("fist handler must be a ProxyInvocation Handler", ProxyInvocationHandler.class, proxy
            .getInvocationHandler(o).getClass());
    }

    public void testGetConnectionWithDudSubjectA() throws ResourceException {
        Subject subj = new Subject();
        Object o = mci.getConnection(subj, cri);

        verifyProxyInterceptors(o);
    }

    public void testGetConnectionWithDudSubjectB() throws ResourceException {
        Subject subj = new Subject();
        String user = new String("user");
        char password[] = {'a', 'b', 'c'};
        PasswordCredential creds = new PasswordCredential(user, password);
        subj.getPrivateCredentials().add(creds);

        Object o = mci.getConnection(subj, cri);

        verifyProxyInterceptors(o);
    }

    public void testGetConnectionWithSubject() throws ResourceException {
        Subject subj = new Subject();
        String user = new String("user");
        char password[] = {'a', 'b', 'c'};
        PasswordCredential creds = new PasswordCredential(user, password);
        creds.setManagedConnectionFactory(factory);
        subj.getPrivateCredentials().add(creds);

        Object o = mci.getConnection(subj, cri);

        verifyProxyInterceptors(o);
    }

    public void testConnectionHandleEqualityDifferentCRI() throws Exception {

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new _FooStub());
        Object conn1 = mci.getConnection(subj, cri);

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new _FooStub());
        Object conn2 = mci.getConnection(subj, cri2);
        assertNotNull(conn1);
        assertNotNull(conn2);

        assertTrue("Connection must be equals to itself.", conn1.equals(conn1));
        assertTrue("Connections must be different.", !conn1.equals(conn2));

        HashMap hm = new HashMap();
        hm.put(conn1, conn1);
        assertTrue("got it back from hashmap", conn1.equals(hm.get(conn1)));
    }

    public void testConnectionHandleEqualitySameCRI() throws Exception {

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new _FooStub());
        Object conn1 = mci.getConnection(subj, cri);

        mockBus.setResult("createClient", new Class[] {URL.class, QName.class, String.class, Class.class},
                          new _FooStub());
        Object conn2 = mci.getConnection(subj, cri);
        assertNotNull(conn1);
        assertNotNull(conn2);

        assertTrue("Connection must be equals to itself.", conn1.equals(conn1));
        assertTrue("Connections must be not equal.", !conn1.equals(conn2));

        HashMap hm = new HashMap();
        hm.put(conn1, conn1);
        assertTrue("got it back from hashmap", conn1.equals(hm.get(conn1)));

        // Closing conn2 does not affect the caching of conn1
        ((Connection)conn2).close();

        Object conn3 = mci.getConnection(subj, cri);
        assertNotNull(conn3);
        assertTrue("Connection conn1 and conn3 must not be equal.", !conn1.equals(conn3));

        // Closing conn2 does not affect the caching of conn1
        ((Connection)conn1).close();

        conn3 = mci.getConnection(subj, cri);
        assertNotNull(conn3);
        assertTrue("Connection conn1 and conn3 must be equal.", conn1.equals(conn3));

    }

    public void testConnectionProxyDelegatesToService() throws Exception {
        Method createClient = Bus.class.getMethod("createClient", new Class[] {URL.class, QName.class,
                                                                               String.class, Class.class});
        mockBus.setResult(createClient, new _FooStub());

        Object o = mci.getConnection(subj, cri);
        assertTrue("returned connection does not implement Connection interface", o instanceof Foo);

        Foo f = (Foo)o;
        f.ping();

        assertTrue("invocation was not delegated to target proxy", _FooStub.pingCalled);
    }

    public void testSecureConnectionProxyDelegatesToService() throws Exception {

        Method createClient = Bus.class.getMethod("createClient", new Class[] {URL.class, QName.class,
                                                                               String.class, Class.class});

        mockBus.setResult(createClient, new _FooStub());

        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setCeltixInstallDir(DummyBus.vobRoot());
        mcf.setCeltixCEURL(DummyBus.CeltixCEURL);

        System.setProperty("test.bus.class", DummyBus.class.getName());

        // initialise mcf
        mcf.createConnectionFactory((ConnectionManager)MockObjectFactory.create(ConnectionManager.class));

        // setup Subject
        String user = new String("user");
        char password[] = {'a', 'b', 'c'};
        PasswordCredential creds = new PasswordCredential(user, password);
        creds.setManagedConnectionFactory(mcf);

        Subject subj = new Subject();
        subj.getPrivateCredentials().add(creds);

        ManagedConnectionImpl mc = (ManagedConnectionImpl)mcf.createManagedConnection(subj, cri);

        assertNull(mc.getSubject());

        Object o = mc.getConnection(subj, cri);
        assertTrue("returned connection does not implement Connection interface", o instanceof Foo);

        assertEquals("managed connection must have subject", subj, mc.getSubject());

        Foo f = (Foo)o;
        f.ping();

        assertTrue("invocation was not delegated to target proxy", _FooStub.pingCalled);

        // again with exception from setRequestContext
        mockIonaMessageContext.setResult("setRequestContext", new ContextException("feck off"));
        mockContextRegistry.setResult("isRegistered", Boolean.TRUE);

        _FooStub.pingCalled = false;
        try {
            f.ping();
            fail("except exception on call with context exception");
        } catch (Exception expected) {
            assertTrue("Is JAXRPCException " + expected, expected instanceof JAXRPCException);
            assertTrue("Is ResourceException " + ((JAXRPCException)expected).getLinkedCause(),
                       ((JAXRPCException)expected).getLinkedCause() instanceof ContextException);
        }

        assertTrue("invocation was not delegated to target proxy", !_FooStub.pingCalled);

    }

    public void testCloseConnection() throws Exception {
        final Connection conn = (Connection)mci.getConnection(subj, cri);

        mockListener.setResult("connectionClosed", new MockInvoke() {
            public Object invoke(Object[] args) throws Throwable {
                assertTrue("the connection passed on the listen event is an exact match",
                           ((ConnectionEvent)args[0]).getConnectionHandle() == conn);

                assertEquals("equals works for connection handle", ((ConnectionEvent)args[0])
                    .getConnectionHandle(), conn);
                return null;
            }
        });

        conn.close();
        assertTrue(mockListener.getCalled("connectionClosed"));
    }

    public void testAssociateConnection() throws Exception {

        // Create the additional ManagedConnectionImpl ..

        CeltixConnectionRequestInfo cri2 = new CeltixConnectionRequestInfo(Foo.class,
                                                                         new URL("file:/tmp/foo2"),
                                                                         new QName("service2"),
                                                                         new QName("fooPort2"));
        ManagedConnectionImpl mci2 = new ManagedConnectionImpl(factory, cri2, new Subject());
        mci2.addConnectionEventListener((ConnectionEventListener)mockListener);

        Object o = mci.getConnection(subj, cri);

        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Foo);
        assertTrue("returned connection is not a java.lang.reflect.Proxy instance", o instanceof Proxy);

        InvocationHandler handler = Proxy.getInvocationHandler(o);

        assertTrue("Asserting handler class: " + handler.getClass(),
                   handler instanceof CeltixInvocationHandler);

        Object assocMci = ((CeltixInvocationHandler)handler).getData().getManagedConnection();

        assertTrue("asserting associated ManagedConnection.", mci == assocMci);
        assertTrue("asserting associated ManagedConnection.", mci2 != assocMci);

        mci2.associateConnection(o);

        assocMci = ((CeltixInvocationHandler)handler).getData().getManagedConnection();

        assertTrue("asserting associated ManagedConnection.", mci2 == assocMci);
        assertTrue("asserting associated ManagedConnection.", mci != assocMci);

    }

    public void testAssociateConnectionThrowsException() throws Exception {

        InvocationHandler ih = (InvocationHandler)MockObjectFactory.create(InvocationHandler.class);
        ((MockObject)ih).setResult("invoke", "foo");
        Object dodgyHandle = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Foo.class}, ih);

        try {
            mci.associateConnection(dodgyHandle);
        } catch (ResourceAdapterInternalException raie) {
            assertTrue("asserting ResourceException.",
                       raie.getMessage().indexOf("Error associating handle") != -1);
            assertTrue("asserting ResourceException.", raie.getCause() instanceof ClassCastException);
        }

    }

    public void testGetMetaData() throws Exception {
        try {
            mci.getMetaData();
            fail("expect exception on getMetaData");
        } catch (NotSupportedException expected) {
        }
    }

    public void testGetXAResource() throws Exception {
        ((MockObject)factory).setResult("getConfigurationScope", "j2ee");
        mockTxSystem.setResult("getXAResource", new MockInvoke() {
            public Object invoke(Object[] args) throws Throwable {
                assertEquals(args[0], "j2ee");
                return MockObjectFactory.create(XAResource.class);
            }
        });

        Object xaResource = mci.getXAResource();
        assertTrue("getXAResource must be called in TxSystem.", mockTxSystem.getCalled("getXAResource"));
        assertNotNull("XAResource must be not null", xaResource);
        assertTrue("XAResource must be instance of XAResource", xaResource instanceof XAResource);
    }

    public void testXAResourceIsNotStatic() throws Exception {
        ((MockObject)factory).setResult("getConfigurationScope", "j2ee");
        mockTxSystem.setResult("getXAResource", new MockInvoke() {
            public Object invoke(Object[] args) throws Throwable {
                assertEquals(args[0], "j2ee");
                return MockObjectFactory.create(XAResource.class);
            }
        });

        Object xaResource = mci.getXAResource();
        assertTrue("getXAResource must be called in TxSystem.", mockTxSystem.getCalled("getXAResource"));
        assertNotNull("XAResource must be not null", xaResource);
        assertTrue("XAResource must be instance of XAResource", xaResource instanceof XAResource);
    }*/

    public static Test suite() {
        return new TestSuite(ManagedConnectionImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {ManagedConnectionImplTest.class.getName()});
    }
}
