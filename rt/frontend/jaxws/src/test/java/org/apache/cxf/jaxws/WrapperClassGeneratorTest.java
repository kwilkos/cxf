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

import java.util.List;

import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.service.AddNumbersImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.junit.After;
import org.junit.Assert;

public class WrapperClassGeneratorTest extends Assert {
    
    @After
    public void tearDown() {
        BusFactory.setDefaultBus(null);
    }
    
    @org.junit.Test
    public void testForAttachementRef() {
        JaxWsImplementorInfo implInfo = 
            new JaxWsImplementorInfo(AddNumbersImpl.class);
        JaxWsServiceFactoryBean jaxwsFac = new JaxWsServiceFactoryBean(implInfo);
        jaxwsFac.setBus(BusFactory.getDefaultBus());
        Service service = jaxwsFac.create();
        ServiceInfo serviceInfo =  service.getServiceInfos().get(0);
        InterfaceInfo interfaceInfo = serviceInfo.getInterface();
        WrapperClassGenerator wrapperClassGenerator = new WrapperClassGenerator(interfaceInfo);
        List<Class> list = wrapperClassGenerator.genearte();
        assertEquals(2, list.size());       
    }
}
