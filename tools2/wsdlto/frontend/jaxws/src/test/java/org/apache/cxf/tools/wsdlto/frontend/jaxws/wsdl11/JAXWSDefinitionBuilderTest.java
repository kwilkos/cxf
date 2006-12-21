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

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.CustomizationParser;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBinding;

public class JAXWSDefinitionBuilderTest extends TestCase {
    private ToolContext env;

    public void setUp() {
        env = new ToolContext();
    }

    public void testCustomization() {
        env.put(ToolConstants.CFG_WSDLURL, getClass().getResource("./hello_world.wsdl").toString());
        env.put(ToolConstants.CFG_BINDING, getClass().getResource("./binding2.xml").toString());
        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build((String)env.get(ToolConstants.CFG_WSDLURL));
        builder.customize();

        CustomizationParser parser = builder.getCustomizationParer();

        JAXWSBinding jaxwsBinding = parser.getDefinitionBindingMap().get(def.getTargetNamespace());
        assertNotNull("JAXWSBinding for definition is null", jaxwsBinding);
        assertEquals("Package customiztion for definition is not correct", "com.foo", jaxwsBinding
            .getPackage());

        
        QName qn = new QName(def.getTargetNamespace(), "Greeter");
        jaxwsBinding = parser.getPortTypeBindingMap().get(qn);
        assertNotNull("JAXWSBinding for PortType is null", jaxwsBinding);
        assertTrue("AsynMapping customiztion for PortType is not true", 
                   jaxwsBinding.isEnableAsyncMapping());

        qn = new QName(def.getTargetNamespace(), "greetMeOneWay");
        jaxwsBinding = parser.getOperationBindingMap().get(qn);
        
        assertNotNull("JAXWSBinding for Operation is null", jaxwsBinding);
        assertEquals("Method name customiztion for operation is not correct", 
                     "echoMeOneWay", jaxwsBinding.getMethodName());

        qn = new QName(def.getTargetNamespace(), "in");
        jaxwsBinding = parser.getPartBindingMap().get(qn);
       
        assertEquals("Parameter name customiztion for part is not correct", 
                     "num1", jaxwsBinding.getJaxwsPara().getName());

        // System.out.println("----size ---- " +
        // parser.getDefinitionBindingMap().size());

        // CustomizationParser cusParser = CustomizationParser.getInstance();
        // cusParser.parse(env);

    }
}
