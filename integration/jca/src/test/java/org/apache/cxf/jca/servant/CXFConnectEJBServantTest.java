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
package org.apache.cxf.jca.servant;

//import java.lang.reflect.Method;

import java.lang.reflect.Method;

import javax.ejb.EJBObject;
import javax.naming.InitialContext;


import org.apache.cxf.Bus;
import org.apache.cxf.jca.cxf.JCABusFactory;
import org.apache.cxf.jca.cxf.ManagedConnectionFactoryImpl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CXFConnectEJBServantTest extends Assert { 
  
    protected CXFConnectEJBServant ejbservant;

    @Before
    public void setUp() throws Exception {
        Bus mockBus = createMockBus();
        ejbservant = createCXFConnectEJBServant(mockBus);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws Exception {
        assertTrue("constructor works", 
                   createCXFConnectEJBServant(createMockBus()) instanceof CXFConnectEJBServant);
    }
    
    @Test
    public void testGetTargetObjectSetsThreadContextClassloader() throws Exception {
        final ClassLoader cl = EasyMock.createMock(ClassLoader.class);
        final EJBObject ejb = EasyMock.createMock(EJBObject.class);
        JCABusFactory rai = EasyMock.createMock(JCABusFactory.class);
        InitialContext ic = EasyMock.createMock(InitialContext.class);
        ClassLoader current = Thread.currentThread().getContextClassLoader();

        ic.lookup(EasyMock.isA(String.class));
        EasyMock.expectLastCall().
            andReturn(new ThreadContextCheckerHome((Object)ejb, (ClassLoader)current, this)).anyTimes();
        EasyMock.replay(ic);
        
        rai.setBus(null);
        EasyMock.expectLastCall();
        rai.getBus();
        EasyMock.expectLastCall().andReturn(null);
        rai.getInitialContext();
        EasyMock.expectLastCall().andReturn(ic);
        rai.getAppserverClassLoader();
        EasyMock.expectLastCall().andReturn(cl).andReturn(current);
        // retrun current classloader for getTargetObject pass
        EasyMock.replay(rai);
        
        
        EJBServant testServant = createCXFConnectEJBServant(rai, null);
        
        
        assertTrue("thread classloader is not set before call, current=" + current,
                    !cl.equals(current));
        
        Thread.currentThread().setContextClassLoader(current);
        Object o = testServant.getTargetObject();
        assertSame("we got back out test object from create, o=" + o,
                   o, ejb);
        
        current = Thread.currentThread().getContextClassLoader();        
        assertTrue("thread classloader is again not set after call, current=" + current,
                   !cl.equals(current));       
        EasyMock.verify(ic);
        EasyMock.verify(rai);
       
    }
        
    @Ignore
    public void testServantInvoke() throws Exception { 

        Greeter target = new GreeterImpl(); 
        Method method = target.getClass().getMethod("sayHi", new Class[0]);
        try {
            ejbservant.invoke(target, method, new Object[0]);
            
            assertTrue("target method invoked", ((GreeterImpl)target).getSayHiCalled());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    /*
    @Ignore
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
    }*/

    protected Bus createMockBus() {
        return EasyMock.createMock(Bus.class);
    }

    protected CXFConnectEJBServant createCXFConnectEJBServant(Bus bus) throws Exception {
        JCABusFactory bf = new JCABusFactory(new ManagedConnectionFactoryImpl());
        return createCXFConnectEJBServant(bf, bus);
    }

    protected CXFConnectEJBServant createCXFConnectEJBServant(JCABusFactory bf, Bus bus) throws Exception {
        bf.setBus(bus);
        return new CXFConnectEJBServant(bf, "", "", null);
    }

}



