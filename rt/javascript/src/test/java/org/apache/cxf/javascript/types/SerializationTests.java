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

package org.apache.cxf.javascript.types;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.cxf.Bus;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.BasicNameManager;
import org.apache.cxf.javascript.JavascriptTestUtilities;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.fortest.TestBean1;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.RhinoException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class SerializationTests extends AbstractDependencyInjectionSpringContextTests {
    private JavascriptTestUtilities testUtilities;
    private XMLInputFactory xmlInputFactory;

    public SerializationTests() {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:serializationTestBeans.xml"};
    }
    
    @Test
    public void testSerialization() throws Exception {
        testUtilities.setBus((Bus)applicationContext.getBean("cxf"));
        
        testUtilities.initializeRhino();
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/cxf-utils.js");

        JaxWsProxyFactoryBean clientProxyFactory = 
            (JaxWsProxyFactoryBean)applicationContext.getBean("simple-dlwu-proxy-factory");
        
        Client client = clientProxyFactory.getClientFactoryBean().create();
        List<ServiceInfo> serviceInfos = client.getEndpoint().getService().getServiceInfos();
        // there can only be one.
        assertEquals(1, serviceInfos.size());
        ServiceInfo serviceInfo = serviceInfos.get(0);
        Collection<SchemaInfo> schemata = serviceInfo.getSchemas();
        NameManager nameManager = new BasicNameManager(serviceInfo);

        for (SchemaInfo schema : schemata) {
            SchemaJavascriptBuilder builder = 
                new SchemaJavascriptBuilder(nameManager, schema);
            String allThatJavascript = builder.generateCodeForSchema(schema);
            assertNotNull(allThatJavascript);
            testUtilities.readStringIntoRhino(allThatJavascript, schema.toString());
        }
        
        testUtilities.readResourceIntoRhino("/serializationTest.js");
        DataBinding dataBinding = clientProxyFactory.getServiceFactory().getDataBinding();
        assertNotNull(dataBinding);
        
        try {
            Object serialized = testUtilities.rhinoCall("serializeTestBean1_1");
            assertTrue(serialized instanceof String);
            String xml = (String)serialized;
            DataReader<XMLStreamReader> reader = dataBinding.createReader(XMLStreamReader.class);
            StringReader stringReader = new StringReader(xml);
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
            QName testBeanQName = new QName("uri:org.apache.cxf.javascript.testns", "TestBean1");
            Object bean = reader.read(testBeanQName, xmlStreamReader, TestBean1.class);
            assertNotNull(bean);
            assertTrue(bean instanceof TestBean1);
            TestBean1 testBean = (TestBean1)bean;
            assertEquals("bean1<stringItem", testBean.stringItem);
            assertEquals(64, testBean.intItem);
            assertEquals(64000000, testBean.longItem);
            assertEquals(101, testBean.optionalIntItem);
            assertNotNull(testBean.optionalIntArrayItem);
            assertEquals(1, testBean.optionalIntArrayItem.length);
            assertEquals(543, testBean.optionalIntArrayItem[0]);
            
        } catch (RhinoException angryRhino) {
            String trace = angryRhino.getScriptStackTrace(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return true;
                }
            } 
            );
            Assert.fail("Javascript error: " + angryRhino.toString() + " " + trace);
        }
        
    }
}
