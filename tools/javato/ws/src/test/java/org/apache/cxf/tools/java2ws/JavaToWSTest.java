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
                                      "-d", output.getPath(), "-client", "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
    }
    
    @Test
    public void testSimple() throws Exception {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose",
                                      "-d", output.getPath(),
                                      "-frontend", "jaxws",
                                      "-client", "-server",
                                      "org.apache.hello_world_doc_lit.Greeter"};
        JavaToWS.main(args);
        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
    }
    
    //org.apache.cxf.tools.fortest
    
    @Test
    public void testClassNoWebServiceAnno() throws Exception {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose",
                                      "-d", output.getPath(),
                                      "-frontend", "jaxws",
                                      "-client", "-server",
                                      "org.apache.cxf.tools.fortest.Hello"};
        JavaToWS.main(args);
        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
        assertTrue("Class does not carry WebService error should be detected"
                   , getStdErr().indexOf("does not carry a WebService annotation") > -1);
    }
    
    @Test
    public void testClassWithRMI() throws Exception {
        String[] args = new String[] {"-wsdl", "-o", output.getPath() + "/tmp.wsdl", "-verbose",
                                      "-d", output.getPath(),
                                      "-frontend", "jaxws",
                                      "-client", "-server",
                                      "org.apache.cxf.tools.fortest.HelloRMI"};
        JavaToWS.main(args);
        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
        assertTrue("Parameter or return type implemented java.rmi.Remote interface error should be detected", 
                   getStdErr().indexOf("implemented the java.rmi.Remote interface") > -1);
    }
    
    
    
    @Test
    public void testGenServerAndClient() throws Exception {
        String[] args = new String[] {"-d", output.getPath(), "-client", "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        File client = new File(output.getPath()
                               + "/org/apache/hello_world_soap12_http/GreeterClient.java");

        assertTrue("Client is not generated", client.exists());

        File server = new File(output.getPath()
                               + "/org/apache/hello_world_soap12_http/GreeterServer.java");
        assertTrue("Greeter_GreeterPort_Server.java is not generated", server.exists());
    }
    
    
    
    @Test
    public void testGenServerAndImpl() throws Exception {
        String[] args = new String[] {"-d", output.getPath(), "-impl", "-server",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);

        File server = new File(output.getPath()
                               + "/org/apache/hello_world_soap12_http/GreeterServer.java");
        assertTrue("GreeterServer.java is not generated", server.exists());
        
        
        File impl = new File(output.getPath()
                               + "/org/apache/hello_world_soap12_http/GreeterImpl.java");
        assertTrue("GreeterImpl.java is not generated", impl.exists());
    }
    
    @Test
    public void testGenWrapperBean() throws Exception {
        String[] args = new String[] {"-d", output.getPath(),
                                      "-wrapperbean",
                                      "-impl", "-server",
                                      "org.apache.cxf.tools.java2ws.fortest.Calculator"};
        JavaToWS.main(args);        
    }
    
    
    @Test
    public void testInvalidFlag() throws Exception {
        String[] args = new String[] {"-frontend", "tmp", "-wsdl", "-o", output.getPath() + "/tmp.wsdl",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        assertTrue("invalid frontend flag should be detected",
                   getStdErr().indexOf("is not a valid frontend,") > -1);

        File wsdlFile = new File(output.getPath() + "/tmp.wsdl");
        assertTrue("wsdl is not generated", wsdlFile.exists());
    }

    @Test
    public void testInvalidFlag2() throws Exception {
        String[] args = new String[] {"-frontend", "simple", "-wrapperbean", "-wsdl",
                                      "-o", output.getPath() + "/tmp.wsdl",
                                      "org.apache.hello_world_soap12_http.Greeter"};
        JavaToWS.main(args);
        assertTrue("wrapperbean flag error should be detected",
                   getStdErr().indexOf("Wrapperbean only needs to be generated for jaxws front end") > -1);
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
