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
package org.apache.cxf.systest.type_test.xml;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.type_test.AbstractTypeTestClient5;

public class XMLClientTypeTest extends AbstractTypeTestClient5 {
    static final String WSDL_PATH = "/wsdl/type_test/type_test_xml.wsdl";
    static final QName SERVICE_NAME = new QName("http://apache.org/type_test/xml", "XMLService");
    static final QName PORT_NAME = new QName("http://apache.org/type_test/xml", "XMLPort");

    public XMLClientTypeTest(String name) {
        super(name);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(XMLClientTypeTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(XMLServerImpl.class); 
                assertTrue("failed to launch server", ok);
            }
            
//            public void setUp() throws Exception {
//                // set up configuration to enable schema validation
//                URL url = getClass().getResource("../celtix-config.xml"); 
//                assertNotNull("cannot find test resource", url);
//                configFileName = url.toString(); 
//                super.setUp();
//            }
            public void setUp() throws Exception {
                super.setUp();
                initClient(AbstractTypeTestClient5.class, SERVICE_NAME, PORT_NAME, WSDL_PATH);
            }

        };
    }  
}
