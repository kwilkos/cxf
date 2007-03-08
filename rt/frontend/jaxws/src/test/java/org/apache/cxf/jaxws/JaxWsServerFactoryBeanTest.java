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

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.service.Hello;
import org.apache.hello_world_doc_lit.GreeterImplDoc;
import org.junit.Test;

public class JaxWsServerFactoryBeanTest extends AbstractJaxWsTest {
    
    @Test
    public void testBean() {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setBus(getBus());
        sf.setAddress("http://localhost:9000/test");
        sf.setServiceClass(Hello.class);
        sf.setStart(false);
        
        Server server = sf.create();
        assertNotNull(server);
    }
    
    @Test
    public void testBareGreeter() throws Exception {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setBus(getBus());
        sf.setServiceClass(GreeterImplDoc.class);
        sf.setStart(false);
        
        Server server = sf.create();
        assertNotNull(server);
    }
}
