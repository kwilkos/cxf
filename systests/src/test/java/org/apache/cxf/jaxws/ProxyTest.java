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

package org.apache.cxf.jaxws;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.SOAPService;

public class ProxyTest extends TestCase {
    
    private final QName serviceName = new QName("http://apache.org/hello_world_soap_http",
                                                "SOAPService");   
    private final QName portName = new QName("http://apache.org/hello_world_soap_http",
                                             "SoapPort1");
    
        
    public void testCreatePort() throws Exception {
        
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        Greeter greeter = service.getPort(portName, Greeter.class);
        boolean getException = false;

        try {
            String reply = greeter.sayHi();
            System.out.println("Response to sayHi() is: " + reply);
        } catch (Exception ex) {
            //ex.printStackTrace();
            // ecpect this as no server is up and running
            getException = true;
        }
        assertTrue(getException);

    }

}
