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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.wsdl11;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.CustomizationParser;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBinding;
import org.apache.cxf.transports.jms.JMSAddressPolicyType;

public class JAXWSDefinitionBuilderTest extends TestCase {
    private ToolContext env;

    public void setUp() {
        env = new ToolContext();
    }

    public void testBuildDefinitionWithXMLBinding() {
        String qname = "http://apache.org/hello_world_xml_http/bare";
        String wsdlUrl = getClass().getResource("/wsdl/hello_world_xml_bare.wsdl").toString();

        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build(wsdlUrl);
        assertNotNull(def);
        
        Map services = def.getServices();
        assertNotNull(services);
        assertEquals(1, services.size());
        Service service = (Service)services.get(new QName(qname, "XMLService"));
        assertNotNull(service);
        
        Map ports = service.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.size());
        Port port = service.getPort("XMLPort");
        assertNotNull(port);

        assertEquals(1, port.getExtensibilityElements().size());
        assertTrue(port.getExtensibilityElements().get(0) instanceof HTTPAddress);

        Binding binding = port.getBinding();
        assertNotNull(binding);
        assertEquals(new QName(qname, "Greeter_XMLBinding"), binding.getQName());

        BindingOperation operation = binding.getBindingOperation("sayHi", null, null);
        assertNotNull(operation);

        BindingInput input = operation.getBindingInput();
        assertNotNull(input);
        assertEquals(1, input.getExtensibilityElements().size());
        assertTrue(input.getExtensibilityElements().get(0) instanceof XMLBindingMessageFormat);
    }

    public void testBuildDefinitionWithJMSTransport() {
        String qname = "http://cxf.apache.org/hello_world_jms";
        String wsdlUrl = getClass().getResource("/wsdl/jms_test.wsdl").toString();

        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build(wsdlUrl);
        assertNotNull(def);
        
        Map services = def.getServices();
        assertNotNull(services);
        assertEquals(8, services.size());
        Service service = (Service)services.get(new QName(qname, "HelloWorldQueueBinMsgService"));
        assertNotNull(service);
        
        Map ports = service.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.size());
        Port port = service.getPort("HelloWorldQueueBinMsgPort");
        assertNotNull(port);

        assertEquals(3, port.getExtensibilityElements().size());
        assertTrue(port.getExtensibilityElements().get(0) instanceof JMSAddressPolicyType);
    }



    public void testCustomization() {
        env.put(ToolConstants.CFG_WSDLURL, getClass().getResource("./hello_world.wsdl").toString());
        env.put(ToolConstants.CFG_BINDING, getClass().getResource("./binding2.xml").toString());
        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build();
        builder.customize();

        CustomizationParser parser = builder.getCustomizationParer();

        JAXWSBinding jaxwsBinding = parser.getDefinitionBindingMap().get(def.getTargetNamespace());
        assertNotNull("JAXWSBinding for definition is null", jaxwsBinding);
        assertEquals("Package customiztion for definition is not correct", "com.foo", jaxwsBinding
            .getPackage());

        QName qn = new QName(def.getTargetNamespace(), "Greeter");
        jaxwsBinding = parser.getPortTypeBindingMap().get(qn);
        assertNotNull("JAXWSBinding for PortType is null", jaxwsBinding);
        assertTrue("AsynMapping customiztion for PortType is not true", jaxwsBinding.isEnableAsyncMapping());

        qn = new QName(def.getTargetNamespace(), "greetMeOneWay");
        jaxwsBinding = parser.getOperationBindingMap().get(qn);

        assertNotNull("JAXWSBinding for Operation is null", jaxwsBinding);
        assertEquals("Method name customiztion for operation is not correct", "echoMeOneWay", jaxwsBinding
            .getMethodName());

        qn = new QName(def.getTargetNamespace(), "in");
        jaxwsBinding = parser.getPartBindingMap().get(qn);

        assertEquals("Parameter name customiztion for part is not correct", "num1", jaxwsBinding
            .getJaxwsPara().getName());

        Definition cusDef = builder.getCustomizedDefinition();

        Schema schema = (Schema)cusDef.getTypes().getExtensibilityElements().iterator().next();

        Element appinfoElement = (Element)schema.getElement()
            .getElementsByTagNameNS(ToolConstants.SCHEMA_URI, "appinfo").item(0);

        assertNotNull("Appinfo element does not  be append to schema element", appinfoElement);

        assertNotNull("typesafeEnum element does not  be append to schema element", appinfoElement
            .getElementsByTagName("jaxb:typesafeEnumClass").item(0));

    }
}
