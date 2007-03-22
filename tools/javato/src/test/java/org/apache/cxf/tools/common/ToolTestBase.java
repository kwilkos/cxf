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

package org.apache.cxf.tools.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.cxf.helpers.FileUtils;

public abstract class ToolTestBase extends TestCase {

    protected PrintStream oldStdErr; 
    protected PrintStream oldStdOut; 
    protected URL wsdlLocation; 
    
    protected ByteArrayOutputStream errOut = new ByteArrayOutputStream(); 
    protected ByteArrayOutputStream stdOut = new ByteArrayOutputStream(); 

    public void setUp() { 
        
        oldStdErr = System.err; 
        oldStdOut = System.out;
        
        System.setErr(new PrintStream(errOut));
        System.setOut(new PrintStream(stdOut));
        
        wsdlLocation = ToolTestBase.class.getResource("/java2wsdl_wsdl/hello_world.wsdl");
    }
    
    public void tearDown() { 
        
        System.setErr(oldStdErr);
        System.setOut(oldStdOut);
    }
    
    protected String getStdOut() {
        return new String(stdOut.toByteArray());
    }
    protected String getStdErr() {
        return new String(errOut.toByteArray());
    }

    protected String getLocation(String wsdlFile) throws URISyntaxException {
        File output = new File(getClass().getResource(".").toURI());
        output = new File(output, "resources");
        
        if (!output.exists()) {
            FileUtils.mkDir(output);            
        }
        
        return new File(output, wsdlFile).toString();
    }    
}

