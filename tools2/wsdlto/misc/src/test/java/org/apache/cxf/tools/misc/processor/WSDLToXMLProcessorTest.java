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

package org.apache.cxf.tools.misc.processor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.xml.namespace.QName;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.bindings.xformat.XMLFormatBinding;
import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.misc.WSDLToXML;


public class WSDLToXMLProcessorTest extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void tearDown() {
    }

    public void testAllDefault() throws Exception {
        String[] args = new String[] {"-i", "Greeter", "-d", output.getCanonicalPath(),
                                      getLocation("/misctools_wsdl/hello_world.wsdl")};
        WSDLToXML.main(args);

        File outputFile = new File(output, "hello_world-xmlbinding.wsdl");
        assertTrue("New wsdl file is not generated", outputFile.exists());
        
        WSDLToXMLProcessor processor = new WSDLToXMLProcessor();
        processor.setEnvironment(env);
        

        try {
            processor.parseWSDL(outputFile.getAbsolutePath());
            Binding binding = processor.getWSDLDefinition().getBinding(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "Greeter_XMLBinding"));
            if (binding == null) {
                fail("Element wsdl:binding Greeter_XMLBinding Missed!");
            }
            Iterator it = binding.getExtensibilityElements().iterator();
            boolean found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof XMLFormatBinding) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Element <xformat:binding/> Missed!");
            }
            BindingOperation bo = binding.getBindingOperation("sayHi", null, null);
            if (bo == null) {
                fail("Element <wsdl:operation name=\"sayHi\"> Missed!");
            }
            it = bo.getBindingInput().getExtensibilityElements().iterator();
            found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof XMLBindingMessageFormat
                    && ((XMLBindingMessageFormat)obj).getRootNode().getLocalPart().equals("sayHi")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Element <xformat:body rootNode=\"tns:sayHi\" /> Missed!");
            }
            Service service = processor.getWSDLDefinition().getService(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "Greeter_XMLService"));
            if (service == null) {
                fail("Element wsdl:service Greeter_XMLService Missed!");
            }
            it = service.getPort("Greeter_XMLPort").getExtensibilityElements().iterator();
            found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof HTTPAddress) {
                    HTTPAddress xmlHttpAddress = (HTTPAddress)obj;
                    if (xmlHttpAddress.getLocationURI() != null) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                fail("Element http:address of service port Missed!");
            }
        } catch (ToolException e) {
            fail("Exception Encountered when parsing wsdl, error: " + e.getMessage());
        }
    }

    private String getLocation(String wsdlFile) throws URISyntaxException {
        return WSDLToXMLProcessorTest.class.getResource(wsdlFile).toURI().toString();
    }

}
