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
package org.apache.cxf.tools.java2ws;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavaToWSTest extends ToolTestBase {
    protected String cp;
    protected ToolContext env;
    protected File output;
    
    @Before
    public void startUp() throws Exception {
        env = new ToolContext();
        cp = System.getProperty("java.class.path");
        URL url = getClass().getResource(".");
        output = new File(url.toURI());
        output = new File(output, "/generated/");
        FileUtils.mkDir(output);
    }
    
    @After
    public void tearDown() {
        super.tearDown();
        System.setProperty("java.class.path", cp);
    }

    @Test
    public void testVersionOutput() throws Exception {
        String[] args = new String[] {"-v"};
        JavaToWS.main(args);
        assertNotNull(getStdOut());
    }
    
    @Test
    public void testFlagWSDL() throws Exception {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", 
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
        
        
    }
    
    protected String getClassPath() throws URISyntaxException {
        ClassLoader loader = getClass().getClassLoader();
        StringBuffer classPath = new StringBuffer();
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlLoader = (URLClassLoader)loader;
            for (URL url : urlLoader.getURLs()) {
                File file;
                file = new File(url.toURI());
                String filename = file.getAbsolutePath();
                if (filename.indexOf("junit") == -1) {
                    classPath.append(filename);
                    classPath.append(System.getProperty("path.separator"));
                }
            }
        }
        return classPath.toString();
    }

}
