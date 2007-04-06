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

package org.apache.cxf.transport.http.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.neethi.Assertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class PolicyUtilsTest extends Assert {

    private IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }

    @Test
    public void testCompatibleClientPolicies() {
        HTTPClientPolicy p1 = new HTTPClientPolicy();
        assertTrue("Policy is not compatible with itself.", PolicyUtils.compatible(p1, p1));
        HTTPClientPolicy p2 = new HTTPClientPolicy();
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setBrowserType("browser");
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setBrowserType(null);
        p1.setConnectionTimeout(10000);
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setAllowChunking(false);
        assertTrue("Policies are compatible.", !PolicyUtils.compatible(p1, p2));
        p2.setAllowChunking(false);
        assertTrue("Policies are compatible.", PolicyUtils.compatible(p1, p2));
    }

    @Test
    public void testIntersectClientPolicies() {
        HTTPClientPolicy p1 = new HTTPClientPolicy();
        HTTPClientPolicy p2 = new HTTPClientPolicy();
        HTTPClientPolicy p = null;

        p1.setBrowserType("browser");
        p = PolicyUtils.intersect(p1, p2);
        assertEquals("browser", p.getBrowserType());
        p1.setBrowserType(null);
        p1.setConnectionTimeout(10000L);
        p = PolicyUtils.intersect(p1, p2);
        assertEquals(10000L, p.getConnectionTimeout());
        p1.setAllowChunking(false);
        p2.setAllowChunking(false);
        p = PolicyUtils.intersect(p1, p2);
        assertTrue(!p.isAllowChunking());
    }

    @Test
    public void testCompatibleServerPolicies() {
        HTTPServerPolicy p1 = new HTTPServerPolicy();
        assertTrue("Policy is not compatible with itself.", PolicyUtils.compatible(p1, p1));
        HTTPServerPolicy p2 = new HTTPServerPolicy();
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setServerType("server");
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setServerType(null);
        p1.setReceiveTimeout(10000);
        assertTrue("Policies are not compatible.", PolicyUtils.compatible(p1, p2));
        p1.setSuppressClientSendErrors(false);
        assertTrue("Policies are compatible.", PolicyUtils.compatible(p1, p2));
        p1.setSuppressClientSendErrors(true);
        assertTrue("Policies are compatible.", !PolicyUtils.compatible(p1, p2));
        p2.setSuppressClientSendErrors(true);
        assertTrue("Policies are compatible.", PolicyUtils.compatible(p1, p2));
    }

    @Test
    public void testIntersectServerPolicies() {
        HTTPServerPolicy p1 = new HTTPServerPolicy();
        HTTPServerPolicy p2 = new HTTPServerPolicy();
        HTTPServerPolicy p = null;

        p1.setServerType("server");
        p = PolicyUtils.intersect(p1, p2);
        assertEquals("server", p.getServerType());
        p1.setServerType(null);
        p1.setReceiveTimeout(10000L);
        p = PolicyUtils.intersect(p1, p2);
        assertEquals(10000L, p.getReceiveTimeout());
        p1.setSuppressClientSendErrors(true);
        p2.setSuppressClientSendErrors(true);
        p = PolicyUtils.intersect(p1, p2);
        assertTrue(p.isSuppressClientSendErrors());
    }

    @Test
    public void testAssertClientPolicyNoop() {
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        control.replay();
        PolicyUtils.assertClientPolicy(message, null);
        control.verify();

        control.reset();
        Collection<Assertion> as = new ArrayList<Assertion>();
        AssertionInfoMap aim = new AssertionInfoMap(as);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        control.replay();
        PolicyUtils.assertClientPolicy(message, null);
        control.verify();
    }

    @Test
    public void testAssertClientPolicyOutbound() {
        testAssertClientPolicy(true);
    }

    @Test
    public void testAssertClientPolicyInbound() {
        testAssertClientPolicy(false);
    }

    void testAssertClientPolicy(boolean outbound) {
        Message message = control.createMock(Message.class);
        HTTPClientPolicy ep = new HTTPClientPolicy();
        HTTPClientPolicy cmp = new HTTPClientPolicy();
        
        cmp.setConnectionTimeout(60000L);
        HTTPClientPolicy icmp = new HTTPClientPolicy();
        icmp.setAllowChunking(false);

        JaxbAssertion<HTTPClientPolicy> ea = 
            new JaxbAssertion<HTTPClientPolicy>(PolicyUtils.HTTPCLIENTPOLICY_ASSERTION_QNAME, false);
        ea.setData(ep);
        JaxbAssertion<HTTPClientPolicy> cma = 
            new JaxbAssertion<HTTPClientPolicy>(PolicyUtils.HTTPCLIENTPOLICY_ASSERTION_QNAME, false);
        cma.setData(cmp);
        JaxbAssertion<HTTPClientPolicy> icma = 
            new JaxbAssertion<HTTPClientPolicy>(PolicyUtils.HTTPCLIENTPOLICY_ASSERTION_QNAME, false);
        icma.setData(icmp);

        AssertionInfo eai = new AssertionInfo(ea);
        AssertionInfo cmai = new AssertionInfo(cma);
        AssertionInfo icmai = new AssertionInfo(icma);

        AssertionInfoMap aim = new AssertionInfoMap(CastUtils.cast(Collections.EMPTY_LIST, Assertion.class));
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        ais.add(eai);
        ais.add(cmai);
        ais.add(icmai);
        aim.put(PolicyUtils.HTTPCLIENTPOLICY_ASSERTION_QNAME, ais);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        Exchange ex = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(ex);
        EasyMock.expect(ex.getOutMessage()).andReturn(outbound ? message : null);
        if (!outbound) {
            EasyMock.expect(ex.getOutFaultMessage()).andReturn(null);
        }

        control.replay();
        PolicyUtils.assertClientPolicy(message, ep);
        assertTrue(eai.isAsserted());
        assertTrue(cmai.isAsserted());
        assertTrue(outbound ? !icmai.isAsserted() : icmai.isAsserted());
        control.verify();
    }
}
