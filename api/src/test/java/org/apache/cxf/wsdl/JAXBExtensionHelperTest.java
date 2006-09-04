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

package org.apache.cxf.wsdl;

import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.transports.jms.JMSAddressPolicyType;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.JMSNamingPropertyType;
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;

public class JAXBExtensionHelperTest extends TestCase {

    private WSDLFactory wsdlFactory;

    private WSDLReader wsdlReader;

    private Definition wsdlDefinition;

    private ExtensionRegistry registry;

    public void setUp() throws Exception {

        wsdlFactory = WSDLFactory.newInstance();
        wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        registry = wsdlReader.getExtensionRegistry();
        if (registry == null) {
            registry = wsdlFactory.newPopulatedExtensionRegistry();
        }
    }

    public void tearDown() {

    }

    public void testAddXMLBindingExtension() throws Exception {

        JAXBExtensionHelper.addExtensions(registry, "javax.wsdl.BindingInput",
                        "org.apache.cxf.bindings.xformat.XMLBindingMessageFormat", Thread.currentThread()
                                        .getContextClassLoader());

        String file = this.getClass().getResource("/wsdl/hello_world_xml_bare.wsdl").getFile();

        wsdlReader.setExtensionRegistry(registry);

        wsdlDefinition = wsdlReader.readWSDL(file);

        Binding b = wsdlDefinition.getBinding(new QName("http://apache.org/hello_world_xml_http/bare",
                        "Greeter_XMLBinding"));
        BindingOperation bo = b.getBindingOperation("sayHi", null, null);
        BindingInput bi = bo.getBindingInput();
        List extList = bi.getExtensibilityElements();
        XMLBindingMessageFormat extIns = null;
        for (Object ext : extList) {
            if (ext instanceof XMLBindingMessageFormat) {
                extIns = (XMLBindingMessageFormat) ext;
            }
        }
        assertEquals("can't found ext element XMLBindingMessageFormat", true, extIns != null);
        QName rootNode = extIns.getRootNode();
        assertEquals("get rootNode value back from extension element", "sayHi", rootNode.getLocalPart());
    }

    public void testAddJMSExtension() throws Exception {

        JAXBExtensionHelper.addExtensions(registry, "javax.wsdl.Port",
                        "org.apache.cxf.transports.jms.JMSAddressPolicyType", Thread.currentThread()
                                        .getContextClassLoader());

        JAXBExtensionHelper.addExtensions(registry, "javax.wsdl.Port",
                        "org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType", Thread.currentThread()
                                        .getContextClassLoader());

        JAXBExtensionHelper.addExtensions(registry, "javax.wsdl.Port",
                        "org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType", Thread.currentThread()
                                        .getContextClassLoader());

        String file = this.getClass().getResource("/wsdl/jms_wsdlext_test.wsdl").getFile();

        wsdlReader.setExtensionRegistry(registry);

        wsdlDefinition = wsdlReader.readWSDL(file);
        Service s = wsdlDefinition.getService(new QName("http://cxf.apache.org/hello_world_jms",
                        "HelloWorldQueueBinMsgService"));
        Port p = s.getPort("HelloWorldQueueBinMsgPort");
        List extPortList = p.getExtensibilityElements();

        JMSAddressPolicyType extAddr = null;
        JMSClientBehaviorPolicyType extClient = null;
        JMSServerBehaviorPolicyType extServer = null;
        for (Object ext : extPortList) {
            if (ext instanceof JMSAddressPolicyType) {
                extAddr = (JMSAddressPolicyType) ext;
            }
            if (ext instanceof JMSClientBehaviorPolicyType) {
                extClient = (JMSClientBehaviorPolicyType) ext;
            }
            if (ext instanceof JMSServerBehaviorPolicyType) {
                extServer = (JMSServerBehaviorPolicyType) ext;
            }
        }
        assertEquals("can't found ext element JMSAddress ExtensionElement", true, extAddr != null);
        assertEquals("can't found ext element JMS Client Behavior", true, extClient != null);
        assertEquals("can't found ext element JMS Server Behavior", true, extServer != null);
        assertEquals("can't get JndiDestinationName", "dynamicQueues/test.jmstransport.binary", extAddr
                        .getJndiDestinationName());

        List<JMSNamingPropertyType> extJmsNamingPropertiesList = null;
        extJmsNamingPropertiesList = extAddr.getJMSNamingProperty();        
        assertEquals("can't found ext element extJmsNamingPropertiesList", true,
                        extJmsNamingPropertiesList != null);
        assertEquals("can't get 2 element of list", 2, extJmsNamingPropertiesList.size());

    }

}
