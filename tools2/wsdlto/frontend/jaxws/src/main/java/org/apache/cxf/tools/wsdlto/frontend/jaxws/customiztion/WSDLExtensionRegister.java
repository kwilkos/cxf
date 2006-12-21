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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.cxf.tools.common.ToolConstants;

public class WSDLExtensionRegister {

    private WSDLFactory wsdlFactory;
    private WSDLReader wsdlreader;

    public WSDLExtensionRegister(WSDLFactory factory, WSDLReader reader) {
        wsdlFactory = factory;
        wsdlreader = reader;
    }

    public void registerExtensions() {
        ExtensionRegistry registry = wsdlreader.getExtensionRegistry();
        if (registry == null) {
            registry = wsdlFactory.newPopulatedExtensionRegistry();
        }
        registerJAXWSBinding(registry, Definition.class);
        registerJAXWSBinding(registry, PortType.class);
        registerJAXWSBinding(registry, Operation.class);

        registerJAXWSBinding(registry, Binding.class);
        registerJAXWSBinding(registry, BindingOperation.class);
        wsdlreader.setExtensionRegistry(registry);
    }

    private void registerJAXWSBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingSerializer());

        registry.registerDeserializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingDeserializer());
        registry.mapExtensionTypes(clz, ToolConstants.JAXWS_BINDINGS, JAXWSBinding.class);
    }

}
