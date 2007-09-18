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
import org.junit.Ignore;
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
    
    private File outputFile(String name) {
        File f = new File(output.getPath() + File.separator + name);
        f.delete();
        return f;
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
        String[] args = new String[] {"-wsdl", "-o", wsdlFile.getAbsolutePath(), "-d", output.getPath(),
                                      "-client", "-server", "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("wsdl is not generated", wsdlFile.exists());
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
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-d",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.hello_world_doc_lit.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("wsdl is not generated", wsdlFile.exists());
    }
    
    @Test
    public void testMissingBeans() {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-d",
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
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-d",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.HelloWithNoAnno"};
        JavaToWS.main(args);
        assertTrue("wsdl is not generated", wsdlFile.exists());
        assertTrue("Class does not carry WebService error should be detected", getStdErr()
            .indexOf("does not carry a WebService annotation") > -1);
    }

    @Test
    public void testClassWithRMI() throws Exception {
        File wsdlFile = outputFile("tmp.wsdl");
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose", "-d",
                                      output.getPath(), "-frontend", "jaxws", "-client", "-server",
                                      "org.apache.cxf.tools.fortest.HelloRMI"};
        JavaToWS.main(args);
        assertTrue("wsdl is not generated", wsdlFile.exists());
        assertTrue("Parameter or return type implemented java.rmi.Remote interface error should be detected",
                   getStdErr().indexOf("implemented the java.rmi.Remote interface") > -1);
    }

    @Test
    @Ignore // CXF-1024
    public void testGenServerAndClient() throws Exception {
        File client = outputFile("org/apache/hello_world_soap12_http/GreeterClient.java");
        File server = outputFile("org/apache/hello_world_soap12_http/GreeterServer.java");

        String[] args = new String[] {"-d", output.getPath(), "-client", "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("Client is not generated", client.exists());
        assertTrue("Greeter_GreeterPort_Server.java is not generated", server.exists());
    }

    @Test
    public void testGenServerAndImpl() throws Exception {
        File server = outputFile("org/apache/hello_world_soap12_http/GreeterServer.java");
        File impl = outputFile("org/apache/hello_world_soap12_http/GreeterImpl.java");

        String[] args = new String[] {"-d", output.getPath(), "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        checkStdErr();
        assertTrue("GreeterServer.java is not generated", server.exists());
        assertTrue("GreeterImpl.java is not generated", impl.exists());
    }

    @Test
    public void testGenWrapperBean() throws Exception {
        String[] args = new String[] {"-d", output.getPath(), "-wrapperbean", "-server",
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
