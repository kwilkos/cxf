package org.objectweb.celtix.jca.celtix;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.connector.Connection;
import org.objectweb.celtix.jca.celtix.handlers.ProxyInvocationHandler;
import org.objectweb.hello_world_soap_http.Greeter;

public class ManagedConnectionImplTest extends ManagedConnectionTestBase {

    public ManagedConnectionImplTest(String name) {
        super(name);
    }

   
    public void testInstanceOfConnection() throws Exception {
        assertTrue("instance of Connection", mci instanceof Connection);
        ((Connection)mci).close();
    }

    public void testGetConnectionServiceGetPortThrows() throws Exception {
        QName serviceName = new QName("SOAPService");
        cri = new CeltixConnectionRequestInfo(Foo.class, null, serviceName, null);
        
        try {
            mci.getConnection(subj, cri);
            fail("Expected ResourceAdapterInternalException");
        } catch (ResourceAdapterInternalException raie) {
            assertTrue("asserting ResourceException.", raie.getMessage()
                .indexOf("No wsdl url specified") != -1);            
        }
    }

    public void testThreadContextClassLoaderIsSet() throws Exception {
        //set the threadContextClassLoader for Bus 
        //TODO njiang classloader things
        //check the threadContextClassLoader 
        mci.getConnection(subj, cri);
    }

    public void testGetConnectionWithNoWSDLInvokesCreateClientWithTwoParameters() throws Exception {

        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");
        cri = new CeltixConnectionRequestInfo(Greeter.class, null, serviceName, portName);
        // need to get wsdl
        Object o = mci.getConnection(subj, cri);

        assertTrue("checking implementation of Connection interface", o instanceof Connection);
        assertTrue("checking implementation of passed interface", o instanceof Greeter);
    }
    
    public void testGetConnectionWithNoWSDLInvokesCreateClientWithTwoArgs()
        throws Exception {

        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        
        cri = new CeltixConnectionRequestInfo(Greeter.class, null, serviceName, null);

        Object o = mci.getConnection(subj, cri);
        assertTrue("checking implementation of Connection interface", o instanceof Connection);
        assertTrue("checking implementation of passed interface", o instanceof Greeter);
        
     

    }

    public void testGetConnectionWithNoPortReturnsConnection() throws Exception {

        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        
        cri = new CeltixConnectionRequestInfo(Greeter.class, 
                                           wsdl,
                                           serviceName,
                                           null);
        
        Object o = mci.getConnection(subj, cri);

        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Greeter);
    }

    public void testGetConnectionReturnsConnection() throws ResourceException {
        Object o = mci.getConnection(subj, cri);
        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Greeter);
    }

    private void verifyProxyInterceptors(Object o) {

        assertTrue(o instanceof Proxy);
        
        assertEquals("fist handler must be a ProxyInvocation Handler", ProxyInvocationHandler.class, 
                     Proxy.getInvocationHandler(o).getClass());
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
 

    public void testCloseConnection() throws Exception {
                
        final Connection conn = (Connection)mci.getConnection(subj, cri);
        EasyMock.reset(mockListener);
        mockListener.connectionClosed(EasyMock.isA(ConnectionEvent.class));
        EasyMock.expectLastCall();
        EasyMock.replay(mockListener);       
        conn.close();
    }

    public void testAssociateConnection() throws Exception {

        // Create the additional ManagedConnectionImpl ..

        CeltixConnectionRequestInfo cri2 = new CeltixConnectionRequestInfo(Greeter.class,
                                                                         new URL("file:/tmp/foo2"),
                                                                         new QName("service2"),
                                                                         new QName("fooPort2"));
        ManagedConnectionImpl mci2 = new ManagedConnectionImpl(factory, cri2, new Subject());
        mci2.addConnectionEventListener(mockListener);

        Object o = mci.getConnection(subj, cri);

        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Greeter);
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

    public void testAssociateConnectionThrowsException() throws Throwable {

        
        InvocationHandler ih = EasyMock.createMock(InvocationHandler.class);
                
        Object dodgyHandle = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Foo.class}, ih);

        try {
            mci.associateConnection(dodgyHandle);
            fail("except exception on call with ClassCast Exception");
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
            // do nothing here
        }
    }
  
    public static Test suite() {
        return new TestSuite(ManagedConnectionImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {ManagedConnectionImplTest.class.getName()});
    }
}
