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

package org.apache.cxf.ws.policy;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.message.Message;
import org.apache.neethi.Assertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * 
 */
public class PolicyVerificationOutInterceptorTest extends TestCase {
 
    private IMocksControl control;
    
    public void setUp() {
        control = EasyMock.createNiceControl();   
    } 
    
    public void testHandleMessage() {
        PolicyVerificationOutInterceptor interceptor = new PolicyVerificationOutInterceptor();
        
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(Message.PARTIAL_RESPONSE_MESSAGE)).andReturn(Boolean.TRUE);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        EasyMock.expect(message.get(Message.PARTIAL_RESPONSE_MESSAGE)).andReturn(null);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        Collection<Assertion> assertions = new ArrayList<Assertion>();
        AssertionInfoMap aim = new AssertionInfoMap(assertions);
        
        Assertion a1 = control.createMock(Assertion.class);   
        AssertionInfo ai1 = new AssertionInfo(a1);
        Assertion a2 = control.createMock(Assertion.class);
        AssertionInfo ai2 = new AssertionInfo(a2);
        Assertion a3 = control.createMock(Assertion.class);
        AssertionInfo ai3 = new AssertionInfo(a3);
        
        QName n1 = new QName("http://x.b.c/x", "a1"); 
        QName n2 = new QName("http://x.b.c/x", "a2");
        
        Collection<AssertionInfo> c = new ArrayList<AssertionInfo>();
        c.add(ai1);
        c.add(ai2);
        aim.put(n1, c);
        c = new ArrayList<AssertionInfo>();
        c.add(ai3);
        aim.put(n2, c);
        
        EasyMock.expect(message.get(Message.PARTIAL_RESPONSE_MESSAGE)).andReturn(null);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        
        control.replay();
        try {
            interceptor.handleMessage(message);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        control.verify();
        
        control.reset();
        ai1.setAsserted(true);
        ai2.setAsserted(true);
        ai3.setAsserted(true);

        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        
        control.replay();        
        interceptor.handleMessage(message);       
        control.verify();
    }
}
