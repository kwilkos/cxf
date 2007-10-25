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
import org.apache.cxf.tools.util.Compiler;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JavaToWSTest extends ToolTestBase {
    protected String cp;
    protected ToolContext env;
    protected File output;
    protected File classDir;

    @Before
    public void setUpResource() throws Exception {
        super.setUp();
        env = new ToolContext();
        cp = System.getProperty("java.class.path");
        URL url = getClass().getResource(".");
        output = new File(url.toURI());
        System.setProperty("java.class.path", getClassPath());
        output = new File(output, "/generated/");
        FileUtils.mkDir(output);
        classDir = new File(output, "/classes/");
        FileUtils.mkDir(classDir);    
    }
    
    @After
    public void tearDown() {
        super.tearDown();
        System.setProperty("java.class.path", cp);
        FileUtils.removeDir(output);
        output = null;
    }
    
    private File outputFile(String name) {
        return new File(output.getPath() + File.separator + name);
    }

    @Test
    public void testVersionOutput() throws Exception {
        String[] args = new String[] {"-v"};
        JavaToWS.main(args);
        assertNotNull(getStdOut());
    }

    @Test
    public void testFlagWSDL() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", wsdlFile.getAbsolutePath(), "-s", output.getPath(),
                                      "-client", "-server", "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("Failed to generate WSDL file", wsdlFile.exists());
    }

    private void checkStdErr() {
        String err = getStdErr();
        if (err != null) {
            assertEquals("errors: ", "", err);
        }
    }

    @Test
    public void testSimple() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-s",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.hello_world_doc_lit.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("Failed to generate WSDL file", wsdlFile.exists());
    }
    
    @Test
    public void testSimpleFrontend() throws Exception {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-s",
                                      output.getPath(), "-frontend", "simple", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.simple.Hello"};
        JavaToWS.main(args);
        File client = outputFile("org/apache/cxf/tools/fortest/simple/HelloPortTypeClient.java");
        File server = outputFile("org/apache/cxf/tools/fortest/simple/HelloPortTypeServer.java");
        File impl = outputFile("org/apache/cxf/tools/fortest/simple/HelloPortTypeImpl.java");
        assertTrue("Failed to generate client file for simple front end ", client.exists());
        assertTrue("Failed to generate server file for simple front end ", server.exists());
        assertTrue("Failed to generate impl file for simple front end ", impl.exists());
        Compiler compiler = new Compiler();
        String[] files = new String[]{client.getAbsoluteFile().toString(),
                                     server.getAbsoluteFile().toString(), 
                                     impl.getAbsoluteFile().toString()};
        compiler.compileFiles(files, this.classDir);
        
        
    }
        
    @Test
    public void testMissingBeans() {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-s",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "-beans", "nobodyHome.xml",
                                      "-beans", "nothing.xml",
                                      "org.apache.hello_world_doc_lit.Greeter"};
        JavaToWS.main(args);
        String err = getStdErr();
        assertTrue("Missing file error message", 
                   err.indexOf("Unable to open bean definition file nobodyHome.xml") >= 0);
    }

    @Test
    public void testClassNoWebServiceAnno() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-s",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.HelloWithNoAnno"};
        JavaToWS.main(args);
        assertTrue("Failed to generate WSDL file", wsdlFile.exists());
        assertTrue("Class does not carry WebService error should be detected", getStdErr()
            .indexOf("does not carry a WebService annotation") > -1);
    }

    @Test
    public void testClassWithRMI() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", 
                                      "-s", output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.HelloRMI"};
        JavaToWS.main(args);
        assertTrue("Failed to generate WSDL file", wsdlFile.exists());
        assertTrue("Parameter or return type implemented java.rmi.Remote interface error should be detected",
                   getStdErr().indexOf("implemented the java.rmi.Remote interface") > -1);
    }

    @Test
    @Ignore // CXF-1024
    public void testGenServerAndClient() throws Exception {
        File client = outputFile("org/apache/hello_world_soap12_http/GreeterClient.java");
        File server = outputFile("org/apache/hello_world_soap12_http/GreeterServer.java");

        String[] args = new String[] {"-s", output.getPath(), "-client", "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("Client was not generated", client.exists());
        assertTrue("Greeter_GreeterPort_Server.java was not generated", server.exists());
    }

    @Test
    public void testGenServerAndImpl() throws Exception {
        File server = outputFile("org/apache/hello_world_soap12_http/GreeterServer.java");
        File impl = outputFile("org/apache/hello_world_soap12_http/GreeterImpl.java");

        String[] args = new String[] {"-s", output.getPath(), "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("GreeterServer.java was not generated", server.exists());
        assertTrue("GreeterImpl.java was not generated", impl.exists());
    }

    @Test
    public void testGenWrapperBean() throws Exception {
        String[] args = new String[] {"-s", output.getPath(), "-wrapperbean", "-server",
                                      "org.apache.cxf.tools.java2ws.fortest.Calculator"};
        JavaToWS.main(args);
        checkStdErr();
    }

    @Test
    public void testInvalidFlag() throws Exception {
        String[] args = new String[] {"-frontend", "tmp", "-wsdl", "-o", output.getPath() + "/tmp.wsdl",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        assertTrue("invalid frontend flag should be detected", getStdErr()
            .indexOf("is not a valid frontend,") > -1);
    }

    @Test
    public void testInvalidFlag2() throws Exception {
        String[] args = new String[] {"-frontend", "simple", "-wrapperbean", "-wsdl", "-o",
                                      output.getPath() + "/tmp.wsdl",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        assertTrue("wrapperbean flag error should be detected", getStdErr()
            .indexOf("-wrapperbean is only valid for the jaxws front end.") > -1);
    }
    
    @Test
    public void testInvalidFlag3() throws Exception {
        String[] args = new String[] {"-databinding", "jaxb", "-frontend", "simple",
                                      "-wsdl", "-o",
                                      output.getPath() + "/tmp.wsdl",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        assertTrue("jaxb databinding warning should be detected", getStdErr()
                   .indexOf("Simple front end only supports aegis databinding") > -1);
    }
    
    
    @Test
    public void testImplClassWithoutSei() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-s",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.GreeterImpl"};
        JavaToWS.main(args);
        assertTrue("Failed to generate WSDL file", wsdlFile.exists());
        
        File sei = outputFile("org/apache/cxf/tools/fortest/GreeterImpl_PortType.java");
        assertTrue("Failed to generate SEI file : GreeterImpl_PortType.java", sei.exists());
        File client = outputFile("org/apache/cxf/tools/fortest/GreeterImpl_PortTypeClient.java");
        assertTrue("Failed to generate client file : GreeterImpl_PortTypeClient.java", client.exists());
        File server = outputFile("org/apache/cxf/tools/fortest/GreeterImpl_PortTypeServer.java");
        assertTrue("Failed to generate SEI file : GreeterImpl_PortTypeServer.java", server.exists());
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
