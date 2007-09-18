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
package org.apache.cxf.aegis.exception;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.junit.Before;
import org.junit.Test;

public class ExceptionTest extends AbstractAegisTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Server s = createService(ExceptionService.class, new ExceptionServiceImpl(), null);
        s.getEndpoint().getService().setInvoker(new BeanInvoker(new ExceptionServiceImpl()));
    }

    @Test
    public void testHeaders() throws Exception {
        ClientProxyFactoryBean proxyFac = new ClientProxyFactoryBean();
        proxyFac.setAddress("local://ExceptionService");
        proxyFac.setServiceClass(ExceptionService.class);
        proxyFac.setBus(getBus());
        setupAegis(proxyFac.getClientFactoryBean());
        
        ExceptionService client = (ExceptionService)proxyFac.create();
        
        try {
            client.sayHiWithException();
            fail("Must throw exception!");
        } catch (HelloException e) {
            // nothing
        }
    }
    
    public static class ExceptionServiceImpl implements ExceptionService {

        public String sayHiWithException() throws HelloException {
            HelloException ex = new HelloException();
            ex.setFaultInfo("test");
            throw ex;
        }
        
    }
}
