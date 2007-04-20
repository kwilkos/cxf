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

package org.apache.cxf.tools.java2wsdl.processor;

import java.io.File;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.BindingConfiguration;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingConfiguration;
import org.apache.cxf.helpers.WSDLHelper;
import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.WSDLConstants;
import org.junit.Test;

public class JavaToProcessorTest extends ProcessorTestBase {
    JavaToProcessor processor = new JavaToProcessor();
    private WSDLHelper wsdlHelper = new WSDLHelper();

    @org.junit.After
    public void tearDown() {
        super.tearDown();
    }
        
    @Test
    public void testGetWSDLVersion() {
        processor.setEnvironment(new ToolContext());
        assertEquals(WSDLConstants.WSDLVersion.WSDL11, processor.getWSDLVersion());
    }

    @Test
    public void testSimpleClass() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped_bare.wsdl");
        context.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.simple.Hello");
        processor.setEnvironment(context);
        processor.process();

        File wsdlFile = new File(output, "doc_wrapped_bare.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        String tns = "http://simple.fortest.tools.cxf.apache.org/";
        Definition def = wsdlHelper.getDefinition(wsdlFile);
        assertNotNull(def);
        Service wsdlService = def.getService(new QName(tns, "Hello"));
        assertNotNull("Generate WSDL Service Error", wsdlService);
    }

    @Test
    public void testCalculator() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/calculator.wsdl");
        context.put(ToolConstants.CFG_CLASSNAME,
                    "org.apache.cxf.tools.fortest.classnoanno.docwrapped.Calculator");
        processor.setEnvironment(context);
        processor.process();

        String expectedFile = getClass().getResource("expected/calculator.wsdl").getFile();
        assertFileEquals(new File(expectedFile), new File(output, "calculator.wsdl"));

        // Test for CXF-337
        // FIXME - check for existence and correctness of faults
    }

    @Test
    public void testIsSOAP12() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_CLASSNAME,
                    "org.apache.cxf.tools.fortest.withannotation.doc.Stock12Impl");
        processor.setEnvironment(context);
        assertTrue(processor.isSOAP12());

        context.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_soap12_http.Greeter");
        assertFalse(processor.isSOAP12());

        context.put(ToolConstants.CFG_SOAP12, "soap12");
        assertTrue(processor.isSOAP12());
    }

    @Test
    public void testGetBindingConfig() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_CLASSNAME,
                    "org.apache.cxf.tools.fortest.withannotation.doc.Stock12Impl");
        processor.setEnvironment(context);
        BindingConfiguration config = processor.getBindingConfig();
        assertTrue(config instanceof SoapBindingConfiguration);
        
        assertTrue(((SoapBindingConfiguration)config).getVersion() instanceof Soap12);

        context.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_soap12_http.Greeter");
        config = processor.getBindingConfig();
        assertTrue(((SoapBindingConfiguration)config).getVersion() instanceof Soap11);

        context.put(ToolConstants.CFG_SOAP12, "soap12");
        config = processor.getBindingConfig();
        assertTrue(((SoapBindingConfiguration)config).getVersion() instanceof Soap12);
    }

    @Test
    public void testSOAP12() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_soap12_http.Greeter");
        context.put(ToolConstants.CFG_SOAP12, "soap12");
        context.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/hello_soap12.wsdl");
        
        processor.setEnvironment(context);
        processor.process();

        String expectedFile = getClass().getResource("expected/hello_soap12.wsdl").getFile();
        assertFileEquals(new File(expectedFile), new File(output, "hello_soap12.wsdl"));
    }

}
