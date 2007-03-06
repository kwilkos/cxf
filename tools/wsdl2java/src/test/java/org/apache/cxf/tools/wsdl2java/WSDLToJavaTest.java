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

package org.apache.cxf.tools.wsdl2java;

import java.io.*;

import org.apache.cxf.tools.common.ToolTestBase;

public class WSDLToJavaTest extends ToolTestBase {

    private File output;
    
    public void setUp() {
        super.setUp();
        try {
            File file = File.createTempFile("WSDLToJavaTest", "");
            output = new File(file.getAbsolutePath() + ".dir");
            file.delete();
            
            if (!output.exists()) {
                output.mkdir();
            }
        } catch (Exception e) {
            // complete
        }
    }

    private void deleteDir(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }
    public void tearDown() {
        try {
            deleteDir(output);
        } catch (IOException ex) {
            //ignore
        }
        output = null;
    }

    public void testVersionOutput() throws Exception {
        String[] args = new String[]{"-v"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testHelpOutput() {
        String[] args = new String[]{"-help"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testBadUsage() {
        String[] args = new String[]{"-bad"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testWSDLToJava() throws Exception {
        String[] args = new String[]{"-ant", "-V", "-d", output.getCanonicalPath(),
                                     wsdlLocation.toURI().getPath()};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }
}
