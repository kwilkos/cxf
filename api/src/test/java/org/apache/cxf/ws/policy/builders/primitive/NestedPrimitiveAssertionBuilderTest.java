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

package org.apache.cxf.ws.policy.builders.primitive;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.neethi.Policy;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;


/**
 * 
 */
public class NestedPrimitiveAssertionBuilderTest extends TestCase {

    private static final String TEST_NAMESPACE = "http://www.w3.org/2007/01/addressing/metadata";
    private static final QName TEST_NAME1 = new QName(TEST_NAMESPACE, "Addressing");

    private NestedPrimitiveAssertionBuilder npab;
    private IMocksControl control;
    private PolicyBuilder builder;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        npab = new NestedPrimitiveAssertionBuilder();
        npab.setKnownElements(Collections.singletonList(TEST_NAME1));
        builder = control.createMock(PolicyBuilder.class);
        npab.setPolicyBuilder(builder);        
    }
    
    public void tearDown() {        
    }
    
    public void testBuildFail() throws Exception {
        String data = 
            "<wsam:Addressing wsp:Optional=\"true\""
            + " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\""
            + " xmlns:wsam=\"http://www.w3.org/2007/01/addressing/metadata\" />";
        
        try {
            npab.build(getElement(data));
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
    }
    
    public void testBuild() throws Exception {
        String data = 
            "<wsam:Addressing wsp:Optional=\"true\""
            + " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\""
            + " xmlns:wsam=\"http://www.w3.org/2007/01/addressing/metadata\">"
            + "<wsp:Policy/></wsam:Addressing>";
 
        Policy nested = control.createMock(Policy.class);
        EasyMock.expect(builder.getPolicy(EasyMock.isA(Element.class))).andReturn(nested);
        control.replay();
        NestedPrimitiveAssertion npc = (NestedPrimitiveAssertion)npab.build(getElement(data));
        assertEquals(TEST_NAME1, npc.getName());
        assertSame(nested, npc.getNested());
        assertTrue(npc.isOptional());
        control.verify();
    }
    
    Element getElement(String data) throws Exception {
        InputStream is = new ByteArrayInputStream(data.getBytes());
        return DOMUtils.readXml(is).getDocumentElement();
    }
}
