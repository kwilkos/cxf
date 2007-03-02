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
package org.apache.cxf.tools.wsdlto.jaxws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import javax.jws.WebService;

import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.util.AnnotationUtil;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.JAXWSContainer;

public class CodeGenBugTest extends ProcessorTestBase {

    private JAXWSContainer processor;
    private ClassLoader classLoader;

    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        classLoader = AnnotationUtil.getClassLoader(Thread.currentThread().getContextClassLoader());
        env.put(ToolConstants.CFG_COMPILE, ToolConstants.CFG_COMPILE);
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");

        processor = new JAXWSContainer(null); 

    }

    public void tearDown() {
        super.tearDown();
        processor = null;
        env = null;
    }
    

    public void testBug305729() {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/bug305729/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute();

        assertNotNull("Process message with no part wsdl error", output);
    }


    public void testBug305773() {
        try {
            env.put(ToolConstants.CFG_COMPILE, "compile");
            env.put(ToolConstants.CFG_IMPL, ToolConstants.CFG_IMPL);
            env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
            env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/bug305773/hello_world.wsdl"));
            processor.setContext(env);
            processor.execute();
            Class clz = classLoader.loadClass("org.apache.hello_world_soap_http.GreeterImpl");

            WebService webServiceAnn = AnnotationUtil.getPrivClassAnnotation(clz, WebService.class);
            assertEquals("Greeter", webServiceAnn.name());
            assertFalse("Impl class should generate portName property value in webService annotation",
                        webServiceAnn.portName().equals(""));
            assertFalse("Impl class should generate serviceName property value in webService annotation",
                        webServiceAnn.serviceName().equals(""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   /*
    public void testHangingBug() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/bughanging/wsdl/wsrf.wsdl"));
        processor.setContext(env);
        processor.execute();
    }
    */
    public void testBug305700() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_CLIENT, ToolConstants.CFG_CLIENT);
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/bug305700/addNumbers.wsdl"));
        processor.setContext(env);
        processor.execute();
    }
    
    public void testNamespacePackageMapping1() throws Exception {
        env.put(ToolConstants.CFG_PACKAGENAME, "org.cxf");
        env.addNamespacePackageMap("http://apache.org/hello_world_soap_http/types", "org.apache.types");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute();

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File types = new File(apache, "types");
        assertTrue(types.exists());

        File[] files = apache.listFiles();
        assertEquals(1, files.length);
        files = types.listFiles();
        assertEquals(17, files.length);

        Class clz = classLoader.loadClass("org.cxf.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
        clz = classLoader.loadClass("org.apache.types.GreetMe");
    }

   

    public void testNamespacePackageMapping2() throws Exception {
        env.addNamespacePackageMap("http://apache.org/hello_world_soap_http", "org.apache");
        env.addNamespacePackageMap("http://apache.org/hello_world_soap_http/types", "org.apache.types");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute();

        File org = new File(output, "org");
        assertTrue("org directory is not found", org.exists());
        File apache = new File(org, "apache");
        assertTrue("apache directory is not found", apache.exists());
        File types = new File(apache, "types");
        assertTrue("types directory is not found", types.exists());

        Class clz = classLoader.loadClass("org.apache.types.GreetMe");
        assertTrue("Generate " + clz.getName() + "error", Modifier.isPublic(clz.getModifiers()));
        clz = classLoader.loadClass("org.apache.Greeter");
    }
      
    public void testNamespacePackageMapping3() throws Exception {
        env.put(ToolConstants.CFG_PACKAGENAME, "org.cxf");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute();
      
        File org = new File(output, "org");
        assertTrue(org.exists());

        File cxf = new File(org, "cxf");
        File[] files = cxf.listFiles();
        assertEquals(22, files.length);

        Class clz = classLoader.loadClass("org.cxf.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
    }
    
    public void testBug305772() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_ANT, ToolConstants.CFG_ANT);
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        // env.put(ToolConstants.CFG_CLIENT, ToolConstants.CFG_CLIENT);
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/bug305772/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute();
        File file = new File(output.getCanonicalPath(), "build.xml");
        FileInputStream fileinput = new FileInputStream(file);
        BufferedInputStream filebuffer = new BufferedInputStream(fileinput);
        byte[] buffer = new byte[(int)file.length()];
        filebuffer.read(buffer);
        String content = new String(buffer);
        assertTrue("wsdl location should be url style in build.xml", content.indexOf("param1=\"file:") > -1);

    }

    
    public void testBug305728HelloWorld() {
        try {
            String[] args = new String[] {"-compile", "-classdir", 
                                          output.getCanonicalPath() + "/classes",
                                          "-d", output.getCanonicalPath(),
                                          "-nexclude",
                                          "http://www.w3.org/2005/08/addressing"
                                              + "=org.apache.cxf.ws.addressing",
                                          getLocation("/wsdl2java_wsdl/bug305728/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            File file = new File(output.getCanonicalPath(),
                                 "org/apache/cxf/ws/addressing/EndpointReferenceType.java");

            assertFalse("Exclude java file should not be generated : " + file.getCanonicalPath(), file
                .exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/apache/cxf/ws/addressing/EndpointReferenceType.class");
            assertFalse("Exclude class should not be generated : " + file.getCanonicalPath(), file.exists());

            file = new File(output.getCanonicalPath(),
                            "org/w3/_2005/_08/addressing/EndpointReferenceType.java");
            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/w3/_2005/_08/addressing/EndpointReferenceType.class");
            assertFalse("Exclude class should not be generated : " + file.getCanonicalPath(), file.exists());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void testBug305728HelloWorld2() {
        try {
            String[] args = new String[] {"-compile", "-classdir", output.getCanonicalPath() + "/classes",
                                          "-d", output.getCanonicalPath(), "-nexclude",
                                          "http://apache.org/hello_world/types",
                                          getLocation("/wsdl2java_wsdl/bug305728/hello_world2.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(output.getCanonicalPath(), "org/apache/hello_world/types");
            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/apache/hello_world/types");

            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testExcludeNSWithPackageName() throws Exception {

        String[] args = new String[] {"-d", output.getCanonicalPath(), "-nexclude",
                                      "http://apache.org/test/types=com.iona", 
                                      "-nexclude", "http://apache.org/Invoice",
                                      getLocation("/wsdl2java_wsdl/hello_world_exclude.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        File com = new File(output, "com");
        assertFalse("Generated file has been excluded", com.exists());
        File iona = new File(com, "iona");
        assertFalse("Generated file has been excluded", iona.exists());

        File org = new File(output, "org");
        File apache = new File(org, "apache");
        File invoice = new File(apache, "Invoice");
        assertFalse("Generated file has been excluded", invoice.exists());

    }
   
    
    public void testExcludeNSWithoutPackageName() throws Exception {

        String[] args = new String[] {"-d", output.getCanonicalPath(), "-nexclude",
                                      "http://apache.org/test/types",
                                      getLocation("/wsdl2java_wsdl/hello_world_exclude.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        File com = new File(output, "test");
        assertFalse("Generated file has been excluded", com.exists());

    }
   
   
    public void testCommandLine() throws Exception {
        String[] args = new String[] {"-compile", "-d", output.getCanonicalPath(), "-classdir",
                                      output.getCanonicalPath() + "/classes", "-p", "org.cxf", "-p",
                                      "http://apache.org/hello_world_soap_http/types=org.apache.types",
                                      "-server", "-impl",
                                      getLocation("/wsdl2java_wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);

        Class clz = classLoader.loadClass("org.cxf.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
        clz = classLoader.loadClass("org.apache.types.GreetMe");
    }

  
    public void testDefaultLoadNSMappingOFF() throws Exception {
        String[] args = new String[] {"-dns", "false", "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl2java_wsdl/basic_callback.wsdl")};

        WSDLToJava.main(args);

        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File w3 = new File(org, "w3");
        assertTrue(w3.exists());
        File p2005 = new File(w3, "_2005");
        assertTrue(p2005.exists());
        File p08 = new File(p2005, "_08");
        assertTrue(p08.exists());
        File address = new File(p08, "addressing");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(11, files.length);
    }

    public void testDefaultLoadNSMappingON() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(),
                                      getLocation("/wsdl2java_wsdl/basic_callback.wsdl")};

        WSDLToJava.main(args);

        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File cxf = new File(apache, "cxf");
        assertTrue(cxf.exists());
        File ws = new File(cxf, "ws");
        assertTrue(ws.exists());
        File address = new File(ws, "addressing");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(11, files.length);
    }
    

    
   
    public void testBug305924ForNestedBinding() {
        try {
            String[] args = new String[] {"-all", "-compile", "-classdir",
                                          output.getCanonicalPath() + "/classes", "-d",
                                          output.getCanonicalPath(), "-b",
                                          getLocation("/wsdl2java_wsdl/bug305924/binding2.xml"),
                                          getLocation("/wsdl2java_wsdl/bug305924/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            Class clz = classLoader
                .loadClass("org.apache.hello_world_soap_http.types.CreateProcess$MyProcess");
            assertNotNull("Customization binding code should be generated", clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
   
    public void testBug305924ForExternalBinding() {
        try {
            String[] args = new String[] {"-all", "-compile", "-classdir",
                                          output.getCanonicalPath() + "/classes", "-d",
                                          output.getCanonicalPath(), "-b",
                                          getLocation("/wsdl2java_wsdl/bug305924/binding1.xml"),
                                          getLocation("/wsdl2java_wsdl/bug305924/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            Class clz = classLoader
                .loadClass("org.apache.hello_world_soap_http.types.CreateProcess$MyProcess");
            assertNotNull("Customization binding code should be generated", clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

   
    private String getLocation(String wsdlFile) {
        return this.getClass().getResource(wsdlFile).getFile();
    }
}
