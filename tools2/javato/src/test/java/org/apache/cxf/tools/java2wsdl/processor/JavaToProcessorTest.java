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

import java.io.*;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.cxf.helpers.WSDLHelper;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;

public class JavaToProcessorTest extends ProcessorTestBase {
    JavaToProcessor processor = new JavaToProcessor();
    private WSDLHelper wsdlHelper = new WSDLHelper();
    
    public void tearDown() {
    }
    
    public void testGetWSDLVersion() {
        processor.setEnvironment(new ToolContext());
        assertEquals(ToolConstants.WSDLVersion.WSDL11, processor.getWSDLVersion());
    }

    public void testSimpleClass() throws Exception {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped_bare.wsdl");
        context.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.simple.Hello");
        //context.put(ToolConstants.CFG_TNS, tns);
        //context.put(ToolConstants.CFG_SERVICENAME, serviceName);
        processor.setEnvironment(context);
        processor.process();

        File wsdlFile = new File(output, "doc_wrapped_bare.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        String tns = "http://simple.fortest.tools.cxf.apache.org";
        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, "Hello"));
        assertNotNull("Generate WSDL Service Error", wsdlService);
    }
}
