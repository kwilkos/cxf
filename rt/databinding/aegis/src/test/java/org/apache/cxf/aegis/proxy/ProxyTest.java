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
package org.apache.cxf.aegis.proxy;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.junit.Before;
import org.junit.Test;

public class ProxyTest extends AbstractAegisTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Server s = createService(HelloProxyService.class, new HelloProxyServiceImpl(), null);
        s.getEndpoint().getService().setInvoker(new BeanInvoker(new HelloProxyServiceImpl()));
    }

    @Test
    public void testProxy() throws Exception {
        ClientProxyFactoryBean proxyFac = new ClientProxyFactoryBean();
        proxyFac.setAddress("local://HelloProxyService");
        proxyFac.setServiceClass(HelloProxyService.class);
        proxyFac.setBus(getBus());
        setupAegis(proxyFac.getClientFactoryBean());
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Hello.class.getName() + ".implementation", MyHello.class.getName());
        proxyFac.setProperties(props);
        
        HelloProxyService client = (HelloProxyService)proxyFac.create();
        
        Hello h = client.sayHiWithProxy();
        assertTrue(h instanceof MyHello);
    }
    
    public static class HelloProxyServiceImpl implements HelloProxyService {

        public Hello sayHiWithProxy() {
            return new MyHello();
        }
        
    }
}
