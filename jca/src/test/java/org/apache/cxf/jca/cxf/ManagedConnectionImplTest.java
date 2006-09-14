/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jca.cxf;

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


import org.apache.cxf.connector.Connection;
import org.apache.cxf.jca.cxf.handlers.ProxyInvocationHandler;
import org.apache.hello_world_soap_http.Greeter;
import org.easymock.classextension.EasyMock;

public class ManagedConnectionImplTest extends ManagedConnectionTestBase {

    protected URL wsdl;
    protected QName serviceName;
    protected QName portName;

    public ManagedConnectionImplTest(String name) {
        super(name);
    }

    /*
    public void setUp() {
        subj = new Subject();
        wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        serviceName = new QName("http://apache.org/hello_world_soap_http", "SOAPService");
        portName = new QName("http://apache.org/hello_world_soap_http", "SoapPort");
        cri = new CXFConnectionRequestInfo(Greeter.class, wsdl, serviceName, portName);
    }
    */
   
    public void testInstanceOfConnection() throws Exception {
        assertTrue("instance of Connection", mci instanceof Connection);
        ((Connection)mci).close();
    }

    public void testGetConnectionServiceGetPortThrows() throws Exception {
        
        cri = new CXFConnectionRequestInfo(Foo.class, null, serviceName, null);
        
        try {
            mci.getConnection(subj, cri);
            fail("Expected ResourceAdapterInternalException");
        } catch (ResourceAdapterInternalException raie) {
            assertTrue("asserting ResourceException.", raie.getMessage()
                .indexOf("Failed to create proxy") != -1);            
        }
    }

    /*
    public void testThreadContextClassLoaderIsSet() throws Exception {
        //set the threadContextClassLoader for Bus 
        //TODO njiang classloader things
        //check the threadContextClassLoader 
        mci.getConnection(subj, cri);
    }
    */
    public void untestGetConnectionWithNoWSDLInvokesCreateClientWithTwoParameters() throws Exception {


        cri = new CXFConnectionRequestInfo(Greeter.class, null, serviceName, portName);
        // need to get wsdl
        Object o = mci.getConnection(subj, cri);

        assertTrue("checking implementation of Connection interface", o instanceof Connection);
        assertTrue("checking implementation of passed interface", o instanceof Greeter);
    }
    
    public void untestGetConnectionWithNoWSDLInvokesCreateClientWithTwoArgs()
        throws Exception {

        cri = new CXFConnectionRequestInfo(Greeter.class, null, serviceName, null);

        Object o = mci.getConnection(subj, cri);
        assertTrue("checking implementation of Connection interface", o instanceof Connection);
        assertTrue("checking implementation of passed interface", o instanceof Greeter);
        
     

    }

    public void untestGetConnectionWithNoPortReturnsConnection() throws Exception {

        cri = new CXFConnectionRequestInfo(Greeter.class, 
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
        Object o = mci.getConnection(subj, cri);

        verifyProxyInterceptors(o);
    }

    public void testGetConnectionWithDudSubjectB() throws ResourceException {
        String user = new String("user");
        char password[] = {'a', 'b', 'c'};
        PasswordCredential creds = new PasswordCredential(user, password);
        subj.getPrivateCredentials().add(creds);
        Object o = mci.getConnection(subj, cri);

        verifyProxyInterceptors(o);
    }

    public void testGetConnectionWithSubject() throws ResourceException {
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
        
        CXFConnectionRequestInfo cri2 = new CXFConnectionRequestInfo(Greeter.class,
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
                   handler instanceof CXFInvocationHandler);

        Object assocMci = ((CXFInvocationHandler)handler).getData().getManagedConnection();

        assertTrue("asserting associated ManagedConnection.", mci == assocMci);
        assertTrue("asserting associated ManagedConnection.", mci2 != assocMci);

        mci2.associateConnection(o);

        assocMci = ((CXFInvocationHandler)handler).getData().getManagedConnection();

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
