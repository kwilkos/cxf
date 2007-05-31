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

package org.apache.cxf.management.utils;

import org.junit.Assert;
import org.junit.Test;

public class ManagementConsoleTest extends Assert {
    private ManagementConsole mc = new ManagementConsole();
    
    @Test
    public void paraserCommandTest() {
        String[] listArgs = new String[] {"-operation", "listall"};
        mc.parserArguments(listArgs);
        assertEquals("It is not right operation name", "listall", mc.operationName);
        assertEquals("The portName should be cleared", "", mc.portName);
        String[] startArgs = new String[] {"-operation", "start", "-jmx", 
                                           "service:jmx:rmi:///jndi/rmi://localhost:1234/jmxrmi",
                                           "-port", 
                                           "\"{http://apache.org/hello_world_soap_http}SoapPort\"",
                                           "-service",
                                           "\"{http://apache.org/hello_world_soap_http}SOAPService\""};
        mc.parserArguments(startArgs);
        assertEquals("It is not right operation name", "start", mc.operationName);
        assertEquals("It is not right port name", 
                    "\"{http://apache.org/hello_world_soap_http}SoapPort\"", mc.portName);
        assertEquals("It is not right service name",
                    "\"{http://apache.org/hello_world_soap_http}SOAPService\"", mc.serviceName);
        assertEquals("It is not a jmx url",
                    "service:jmx:rmi:///jndi/rmi://localhost:1234/jmxrmi",
                    mc.jmxServerURL);
    }
    
    

}
