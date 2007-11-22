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
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.JavascriptTestUtilities;
import org.apache.cxf.javascript.JavascriptTestUtilities.JSRunnable;
import org.apache.cxf.javascript.JsSimpleDomNode;
import org.apache.cxf.javascript.fortest.BasicTypeFunctionReturnStringWrapper;
import org.apache.cxf.javascript.fortest.StringWrapper;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.context.support.GenericApplicationContext;

public class DocLitWrappedTest extends AbstractCXFSpringTest {
    private static final String BASIC_TYPE_FUNCTION_RETURN_STRING_RESPONSE_DESERIALIZE_RESPONSE = 
        "org_apache_cxf_javascript_fortest_basicTypeFunctionReturnStringResponse_deserializeResponse";
    private static final String WHAT_ROUGH_BEAST_ITS_HOUR_COME_AT_LAST = 
        "What rough beast, its hour come at last, ...";
    private static final Logger LOG = LogUtils.getL7dLogger(DocLitWrappedTest.class);
    private static final String BASIC_TYPE_FUNCTION_RETURN_STRING_SERIALIZER_NAME 
        = "org_apache_cxf_javascript_fortest_basicTypeFunctionReturnString_serializeInput";
    
    private JavascriptTestUtilities testUtilities;
    private Client client;
    private List<ServiceInfo> serviceInfos;
    private JaxWsProxyFactoryBean clientProxyFactory;
    private XMLInputFactory xmlInputFactory;
    private DocumentBuilder documentBuilder;

    public DocLitWrappedTest() throws Exception {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
        xmlInputFactory = XMLInputFactory.newInstance();
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:serializationTestBeans.xml"};
    }

    @Test 
    public void testMessageSerialization() throws Exception {
        setupClientAndRhino("simple-dlwu-proxy-factory");
        DataBinding dataBinding = clientProxyFactory.getServiceFactory().getDataBinding();
        assertNotNull(dataBinding);
        // the serialize function takes an array of the five parameters.
        final Object[] params = new Object[5];
        params[0] = testUtilities.javaToJS(new Float("3.14159"));
        params[1] = testUtilities.javaToJS(new Double("7.90834"));
        params[2] = testUtilities.javaToJS(new Integer(42));
        params[3] = testUtilities.javaToJS(new Long(420000));
        params[4] = testUtilities.javaToJS(new String("Hello<Dolly&sheep"));
        Scriptable jsParamArray =
            testUtilities.runInsideContext(Scriptable.class, new JSRunnable<Scriptable>() {
                public Scriptable run(Context context) {
                    return context.newArray(testUtilities.getRhinoScope(), params);
                }
            });
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
        // Read into the full message to find the part.
        StringReader stringReader = new StringReader((String)xmlString);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        boolean gotToPart = false;
        do {
            int item = xmlStreamReader.next();
            if (item == XMLStreamReader.START_ELEMENT) { 
                LOG.finest(xmlStreamReader.getName().toString());
                if (xmlStreamReader.getName().equals(part.getConcreteName())) {
                    gotToPart = true;
                }
            }
        } while (!gotToPart && xmlStreamReader.hasNext());
        assertTrue(gotToPart);
        Object messageObject = reader.read(part, xmlStreamReader);
        assertNotNull(messageObject);
        assertTrue(messageObject instanceof BasicTypeFunctionReturnStringWrapper);
        BasicTypeFunctionReturnStringWrapper wrapper = (BasicTypeFunctionReturnStringWrapper)messageObject;
        assertEquals(params[4], wrapper.getS());
    }
    
    @Test
    public void testResponseDeserialization() throws Exception {
        setupClientAndRhino("simple-dlwu-proxy-factory");
        DataBinding dataBinding = clientProxyFactory.getServiceFactory().getDataBinding();
        assertNotNull(dataBinding);
        StringWrapper responseObject = new StringWrapper();
        responseObject.setReturnValue(WHAT_ROUGH_BEAST_ITS_HOUR_COME_AT_LAST);
        DataWriter<Node> writer = dataBinding.createWriter(Node.class);
        ServiceInfo serviceInfo = serviceInfos.get(0); // assume we only have one.
        QName messageName = 
            new QName("uri:org.apache.cxf.javascript.fortest", "basicTypeFunctionReturnString");
        MessageInfo outputMessage = serviceInfo.getMessage(messageName);
        assertNotNull(outputMessage);
        MessagePartInfo part = outputMessage.getMessagePartByIndex(0); // has only one part.
        Document document = documentBuilder.newDocument();
        writer.write(responseObject, part, document);
        Element messageElement = document.getDocumentElement();
        Object jsUtils = testUtilities.rhinoNewObject("CxfApacheOrgUtil");
        Object jsResult = 
            testUtilities.rhinoCall(BASIC_TYPE_FUNCTION_RETURN_STRING_RESPONSE_DESERIALIZE_RESPONSE,
                                    jsUtils,
                                    JsSimpleDomNode.wrapNode(testUtilities.getRhinoScope(), 
                                                             messageElement));
        assertNotNull(jsResult);
        ScriptableObject jsResultObject = (ScriptableObject)jsResult;
        Object returnValue = ScriptableObject.callMethod(jsResultObject, 
                                                         "getReturnValue", 
                                                          new Object[0]);
        assertEquals(WHAT_ROUGH_BEAST_ITS_HOUR_COME_AT_LAST, returnValue);
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
        testUtilities.loadJavascriptForService(serviceInfo);
    }

    @Override
    protected void additionalSpringConfiguration(GenericApplicationContext context) throws Exception {
    }
}
