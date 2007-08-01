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

package org.apache.cxf.systest.aegis;


import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.authservice.AuthService;
import org.apache.cxf.authservice.Authenticate;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class AegisClientServerTest extends AbstractBusClientServerTestBase {
    static final Logger LOG = Logger.getLogger(AegisClientServerTest.class.getName());
    
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(AegisServer.class));
    }
    
    @Test
    public void testAegisClient() throws Exception {
        AegisDatabinding aegisBinding = new AegisDatabinding();
        ClientProxyFactoryBean proxyFactory = new ClientProxyFactoryBean();
        proxyFactory.setDataBinding(aegisBinding);
        proxyFactory.setServiceClass(AuthService.class);
        proxyFactory.setAddress("http://localhost:9002/service");
        AuthService service = (AuthService) proxyFactory.create();
        assertTrue(service.authenticate("Joe", "Joe", "123"));
        assertFalse(service.authenticate("Joe1", "Joe", "fang"));      
        List<String> list = service.getRoles("Joe");
        assertEquals(1, list.size());
        assertEquals("Joe", list.get(0));
        assertEquals("get Joe", service.getAuthentication("Joe"));
        Authenticate au = new Authenticate();
        au.setSid("ffang");
        au.setUid("ffang");
        assertTrue(service.authenticate(au));
        au.setUid("ffang1");
        assertFalse(service.authenticate(au));
    }
    
    @Test
    public void testJaxWsAegisClient() throws Exception {
        AegisDatabinding aegisBinding = new AegisDatabinding();
        JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
        proxyFactory.setDataBinding(aegisBinding);
        proxyFactory.setServiceClass(AuthService.class);
        proxyFactory.setAddress("http://localhost:9002/jaxwsAndAegis");
        AuthService service = (AuthService) proxyFactory.create();
        assertTrue(service.authenticate("Joe", "Joe", "123"));
        assertFalse(service.authenticate("Joe1", "Joe", "fang"));      
        List<String> list = service.getRoles("Joe");
        assertEquals(1, list.size());
        assertEquals("Joe", list.get(0));
        assertEquals("get Joe", service.getAuthentication("Joe"));
        Authenticate au = new Authenticate();
        au.setSid("ffang");
        au.setUid("ffang");
        assertTrue(service.authenticate(au));
        au.setUid("ffang1");
        assertFalse(service.authenticate(au));
    }
}
