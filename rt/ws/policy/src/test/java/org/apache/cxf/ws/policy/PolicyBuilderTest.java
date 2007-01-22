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

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.ws.policy.builders.AssertionBuilder;
import org.apache.cxf.ws.policy.builders.xml.XMLPrimitiveAssertionBuilder;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;

public class PolicyBuilderTest extends TestCase {
    
    public void testGetPolicy() throws Exception {
        String name = "/samples/test25.xml";
        InputStream is = PolicyBuilderTest.class.getResourceAsStream(name);
        PolicyBuilder builder = new PolicyBuilder();
        AssertionBuilderRegistry abr = new AssertionBuilderRegistry();
        builder.setAssertionBuilderRegistry(abr);
        AssertionBuilder ab = new XMLPrimitiveAssertionBuilder();
        abr.registerBuilder(new QName("http://sample.org/Assertions", "A"), ab);
        abr.registerBuilder(new QName("http://sample.org/Assertions", "B"), ab);
        abr.registerBuilder(new QName("http://sample.org/Assertions", "C"), ab);        
        
        Policy p = builder.getPolicy(is);
        assertNotNull(p);
        List<PolicyComponent> a = CastUtils.cast(p.getAssertions(), PolicyComponent.class);
        assertEquals(3, a.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(Constants.TYPE_ASSERTION, a.get(i).getType());
        }
    }
}
