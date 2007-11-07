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

package org.apache.cxf.javascript.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.BasicNameManager;
import org.apache.cxf.javascript.JavascriptTestUtilities;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.types.SchemaJavascriptBuilder;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.junit.Test;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class DocLitWrappedTest extends AbstractDependencyInjectionSpringContextTests {
    private JavascriptTestUtilities testUtilities;
    private Client client;
    private List<ServiceInfo> serviceInfos;
    private Collection<SchemaInfo> schemata;
    private NameManager nameManager;
    private JaxWsProxyFactoryBean clientProxyFactory;

    public DocLitWrappedTest() {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:serializationTestBeans.xml"};
    }

    @Test 
    public void testDeserialization() throws Exception {
        setupClientAndRhino("simple-dlwu-proxy-factory");
        testUtilities.readResourceIntoRhino("/deserializationTests.js");
        DataBinding dataBinding = clientProxyFactory.getServiceFactory().getDataBinding();
        assertNotNull(dataBinding);
    }

    private void setupClientAndRhino(String clientProxyFactoryBeanId) throws IOException {
        testUtilities.setBus((Bus)applicationContext.getBean("cxf"));
        
        testUtilities.initializeRhino();
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/cxf-utils.js");

        clientProxyFactory = (JaxWsProxyFactoryBean)applicationContext.getBean(clientProxyFactoryBeanId);
        client = clientProxyFactory.getClientFactoryBean().create();
        serviceInfos = client.getEndpoint().getService().getServiceInfos();
        // there can only be one.
        assertEquals(1, serviceInfos.size());
        ServiceInfo serviceInfo = serviceInfos.get(0);
        schemata = serviceInfo.getSchemas();
        nameManager = new BasicNameManager(serviceInfo);
        for (SchemaInfo schema : schemata) {
            SchemaJavascriptBuilder builder = 
                new SchemaJavascriptBuilder(serviceInfo.getXmlSchemaCollection(), nameManager, schema);
            String allThatJavascript = builder.generateCodeForSchema(schema);
            assertNotNull(allThatJavascript);
            testUtilities.readStringIntoRhino(allThatJavascript, schema.toString() + ".js");
        }
        
        ServiceJavascriptBuilder serviceBuilder = 
            new ServiceJavascriptBuilder(serviceInfo, nameManager);
        serviceBuilder.walk();
    }
}
