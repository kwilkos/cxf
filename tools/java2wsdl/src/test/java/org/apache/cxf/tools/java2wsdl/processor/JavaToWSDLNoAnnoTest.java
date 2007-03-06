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

import org.apache.cxf.tools.common.ToolConstants;

public class JavaToWSDLNoAnnoTest extends ProcessorTestBase {

    private JavaToWSDLProcessor j2wProcessor;

    public void setUp() throws Exception {
        super.setUp();
        j2wProcessor = new JavaToWSDLProcessor();
        System.setProperty("java.class.path", getClassPath());
    }

    public void tearDown() {
        super.tearDown();
        j2wProcessor = null;
    }

    
    public void testGeneratedWithElementryClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_bare.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.classnoanno.docbare.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

    }
    
    public void testGeneratedWithDocWrappedClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME,
                "org.apache.cxf.tools.fortest.classnoanno.docwrapped.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

    }
   
    public void testGeneratedWithRPCClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/rpc.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.classnoanno.rpc.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
    }


}
