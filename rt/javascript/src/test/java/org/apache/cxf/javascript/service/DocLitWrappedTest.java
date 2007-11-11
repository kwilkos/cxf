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
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.BasicNameManager;
import org.apache.cxf.javascript.JavascriptTestUtilities;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.types.SchemaJavascriptBuilder;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;

public class DocLitWrappedTest extends AbstractCXFSpringTest {
    private static final Logger LOG = LogUtils.getL7dLogger(DocLitWrappedTest.class);
    private static final String BASIC_TYPE_FUNCTION_RETURN_STRING_SERIALIZER_NAME 
        = "org_apache_cxf_javascript_fortest_basicTypeFunctionReturnString_serializeInput";
    
    private JavascriptTestUtilities testUtilities;
    private Client client;
    private List<ServiceInfo> serviceInfos;
    private Collection<SchemaInfo> schemata;
    private NameManager nameManager;
    private JaxWsProxyFactoryBean clientProxyFactory;
    private XMLInputFactory xmlInputFactory;

    public DocLitWrappedTest() {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:serializationTestBeans.xml"};
    }

    @Ignore
    @Test 
    public void testMessageSerialization() throws Exception {
        setupClientAndRhino("simple-dlwu-proxy-factory");
        DataBinding dataBinding = clientProxyFactory.getServiceFactory().getDataBinding();
        assertNotNull(dataBinding);
        // the serialize function takes an array of the five parameters.
        Object[] params = new Object[5];
        params[0] = new String("Hello<Dolly&sheep");
        params[1] = new Integer(42);
        params[2] = new Long(420000);
        params[3] = new Float("3.14159");
        params[4] = new Double("7.90834");
        Scriptable jsParamArray = 
            testUtilities.getRhinoContext().newArray(testUtilities.getRhinoScope(), params);
        Object xmlString = null;
        xmlString = testUtilities.rhinoCall(BASIC_TYPE_FUNCTION_RETURN_STRING_SERIALIZER_NAME,
                                                    jsParamArray);
        assertTrue(xmlString instanceof String);
        DataReader<XMLStreamReader> reader = dataBinding.createReader(XMLStreamReader.class);
        ServiceInfo serviceInfo = serviceInfos.get(0); // assume we only have one.
        QName messageName = 
            new QName("uri:org.apache.cxf.javascript.fortest", "basicTypeFunctionReturnString");
        MessageInfo inputMessage = serviceInfo.getMessage(messageName);
        assertNotNull(inputMessage);
        MessagePartInfo part = inputMessage.getMessagePartByIndex(0); // has only one part.
        // we have the entire SOAP message in the string. Readng the entire message is
        // really organized by the full endpoint. If we want to focus for the moment at the message
        // or part part level, either need to invoke Javascript that builds less, or we need to fish out the 
        // part from the DOM or the reader.
        // we can use the DOM to local
        StringReader stringReader = new StringReader((String)xmlString);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        boolean gotToPart = false;
        do {
            xmlStreamReader.nextTag();
            if (xmlStreamReader.getName().equals(part.getElementQName())) {
                gotToPart = true;
            }
        } while (!gotToPart && xmlStreamReader.hasNext());
        assertTrue(gotToPart);
        Object messageObject = reader.read(part, xmlStreamReader);
        assertNotNull(messageObject);
    }

    private void setupClientAndRhino(String clientProxyFactoryBeanId) throws IOException {
        testUtilities.setBus(getBean(Bus.class, "cxf"));
        
        testUtilities.initializeRhino();
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/cxf-utils.js");

        clientProxyFactory = getBean(JaxWsProxyFactoryBean.class, clientProxyFactoryBeanId);
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
            LOG.info(schema.toString());
            LOG.info(allThatJavascript);
            testUtilities.readStringIntoRhino(allThatJavascript, schema.toString() + ".js");
        }
        
        ServiceJavascriptBuilder serviceBuilder = 
            new ServiceJavascriptBuilder(serviceInfo, nameManager);
        serviceBuilder.walk();
        String serviceJavascript = serviceBuilder.getCode();
        LOG.info(serviceInfo.toString());
        LOG.info(serviceJavascript);
        testUtilities.readStringIntoRhino(serviceJavascript, serviceInfo.getName() + ".js");
    }
}
