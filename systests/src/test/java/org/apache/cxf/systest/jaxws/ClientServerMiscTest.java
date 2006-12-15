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

package org.apache.cxf.systest.jaxws;

import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.anonymous_complex_type.AnonymousComplexType;
import org.apache.cxf.anonymous_complex_type.AnonymousComplexTypeService;
import org.apache.cxf.anonymous_complex_type.SplitName;
import org.apache.cxf.anonymous_complex_type.SplitNameResponse;
import org.apache.cxf.anonymous_complex_type.SplitNameResponse.Names;
import org.apache.cxf.jaxb_element_test.JaxbElementTest;
import org.apache.cxf.jaxb_element_test.JaxbElementTest_Service;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;

public class ClientServerMiscTest extends ClientServerTestBase {

    private final QName portName = new QName("http://cxf.apache.org/anonymous_complex_type/",
            "anonymous_complex_typeSOAP");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClientServerMiscTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(ServerMisc.class));
            }
        };
    }

    public void testAnonymousComplexType() throws Exception {

        AnonymousComplexTypeService actService = new AnonymousComplexTypeService();
        assertNotNull(actService);
        AnonymousComplexType act = actService.getPort(portName, AnonymousComplexType.class);

        try {
            Names reply = act.splitName("Tom Li");
            assertNotNull("no response received from service", reply);
            assertEquals("Tom", reply.getFirst());
            assertEquals("Li", reply.getSecond());
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void testRefAnonymousComplexType() throws Exception {

        AnonymousComplexTypeService actService = new AnonymousComplexTypeService();
        assertNotNull(actService);
        AnonymousComplexType act = actService.getPort(portName, AnonymousComplexType.class);

        try {
            SplitName name = new SplitName();
            name.setName("Tom Li");
            SplitNameResponse reply = act.refSplitName(name);
            assertNotNull("no response received from service", reply);
            assertEquals("Tom", reply.getNames().getFirst());
            assertEquals("Li", reply.getNames().getSecond());
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void testMinOccursAndNillableJAXBElement() throws Exception {

        JaxbElementTest_Service service = new JaxbElementTest_Service();
        assertNotNull(service);
        JaxbElementTest port = service.getPort(JaxbElementTest.class);

        try {

            String response = port.newOperation("hello");
            assertNotNull(response);
            assertEquals("in=hello", response);

            response = port.newOperation(null);
            assertNotNull(response);
            assertEquals("in=null", response);

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }
}
