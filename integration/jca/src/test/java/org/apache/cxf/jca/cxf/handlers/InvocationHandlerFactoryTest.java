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
package org.apache.cxf.jca.cxf.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;


import org.apache.cxf.jca.cxf.CXFInvocationHandler;
import org.junit.Test;

public class InvocationHandlerFactoryTest extends HandlerTestBase {
    
    public InvocationHandlerFactoryTest() {
        super();
    }
    
    public InvocationHandlerFactoryTest(String name) {
        super(name);
    }
    
    @Test
    public void testCreateHandlerChain() 
        throws ResourceAdapterInternalException {

        Subject testSubject = new Subject();

        InvocationHandlerFactory factory = 
            new InvocationHandlerFactory(
                 mockBus,
                 mci);

        CXFInvocationHandler handler = factory.createHandlers(target, testSubject);
        CXFInvocationHandler first = handler;
        CXFInvocationHandler last = null;

        assertNotNull("handler must not be null", handler);
        int count = 0;
        Set<Class> allHandlerTypes = new HashSet<Class>();

        while (handler != null) {

            assertSame("managed connection must be set", mci, handler.getData().getManagedConnection());
            assertSame("bus must be set", mockBus, handler.getData().getBus());
            assertSame("subject must be set", testSubject, handler.getData().getSubject());
            assertSame("target must be set", target, handler.getData().getTarget());
            allHandlerTypes.add(handler.getClass());

            last = handler;
            handler = handler.getNext();

            count++;
        }
        assertNotNull(last);

        assertEquals("must create correct number of handlers", 3, count);

        assertTrue("first handler must a ProxyInvocationHandler", first instanceof ProxyInvocationHandler);
        assertTrue("last handler must be an InvokingInvocationHandler",
                   last instanceof InvokingInvocationHandler);

        Class[] types = {ProxyInvocationHandler.class, 
                         ObjectMethodInvocationHandler.class,
                         InvokingInvocationHandler.class};

        for (int i = 0; i < types.length; i++) {
            assertTrue("handler chain must contain type: " + types[i], allHandlerTypes.contains(types[i]));
        }
    }

    

    
}
