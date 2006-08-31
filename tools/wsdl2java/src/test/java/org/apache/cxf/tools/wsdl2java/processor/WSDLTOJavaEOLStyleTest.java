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

package org.apache.cxf.tools.wsdl2java.processor;

import java.io.File;
import java.io.FileReader;

import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;

public class WSDLTOJavaEOLStyleTest extends ProcessorTestBase {

    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }

    public void testHelloWorld() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();
        File seiFile = new File(output.getCanonicalPath()
                                + "/org/apache/hello_world_soap_http/Greeter.java");
        assertTrue("PortType file is not generated", seiFile.exists());
        FileReader fileReader = new FileReader(seiFile);
        char[] chars = new char[100];
        int size = 0;
        StringBuffer sb = new StringBuffer();
        while (size < seiFile.length()) {
            int readLen = fileReader.read(chars);
            sb.append(chars, 0, readLen);
            size = size + readLen;

        }
        String seiString = new String(sb);
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            assertTrue("EOL Style is not correct on windows platform", seiString.indexOf("\r\n") >= 0);
        } else {
            assertTrue("EOL Style is not correct on unix platform", seiString.indexOf("\r") < 0);
        }

    }

    private String getLocation(String wsdlFile) {
        return WSDLTOJavaEOLStyleTest.class.getResource(wsdlFile).getFile();
    }
}
