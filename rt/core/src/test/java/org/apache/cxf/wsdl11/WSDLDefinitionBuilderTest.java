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

package org.apache.cxf.wsdl11;

import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class WSDLDefinitionBuilderTest extends TestCase {

    public void testBuildSimpleWSDL() throws Exception {
        String qname = "http://apache.org/hello_world_soap_http";
        String wsdlUrl = getClass().getResource("hello_world.wsdl").toString();
        
        WSDLDefinitionBuilder builder = new WSDLDefinitionBuilder();
        Definition def = builder.build(wsdlUrl);
        assertNotNull(def);
        
        Map services = def.getServices();
        assertNotNull(services);
        assertEquals(1, services.size());
        Service service = (Service)services.get(new QName(qname, "SOAPService"));
        assertNotNull(service);
        
        Map ports = service.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.size());
        Port port = service.getPort("SoapPort");
        assertNotNull(port);        
    }
}
