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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.apache.cxf.helpers.FileUtils;

public class ProcessorTestBase extends TestCase {

    protected ToolContext env = new ToolContext();
    protected File output;

    public void setUp() throws Exception {
        URL url = getClass().getResource(".");
        output = new File(url.toURI());
        output = new File(output, "/resources");
        FileUtils.mkDir(output);
    }

    public void tearDown() {
        FileUtils.removeDir(output);
        output = null;
        env = null;
    }

    protected String getClassPath() {
        ClassLoader loader = getClass().getClassLoader();
        StringBuffer classPath = new StringBuffer();
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlLoader = (URLClassLoader)loader;
            for (URL url : urlLoader.getURLs()) {               
                File file = new File(url.getFile());
                String filename = file.getAbsolutePath();                
                if (filename.indexOf("junit") == -1) {
                    classPath.append(filename);
                    classPath.append(System.getProperty("path.separator"));
                }
            }
        }
        return classPath.toString();
    }
    
    protected String getLocation(String wsdlFile) throws URISyntaxException {
        return getClass().getResource(wsdlFile).toString();
    }
}
