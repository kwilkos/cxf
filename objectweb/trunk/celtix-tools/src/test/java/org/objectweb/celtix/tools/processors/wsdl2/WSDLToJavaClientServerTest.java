package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToJavaClientServerTest extends ProcessorTestBase {
    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();


    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);       
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
    }

    public void tearDown() {
        super.tearDown();
        processor = null;

    }

    public void testGenClientOnly() throws Exception {

        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        env.put(ToolConstants.CFG_GEN_CLIENT, ToolConstants.CFG_GEN_CLIENT);
        processor.setEnvironment(env);
        processor.process();
        File file =  new File(output.getCanonicalPath() + "/org/objectweb/hello_world_soap_http/");
        File[] files = file.listFiles(new java.io.FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) {
                    return true;
                }
                return false;

            }
        });
        assertTrue("Should generate 3 files", files != null && files.length == 3);
            
    }

    public void testGenServerOnly() throws Exception {

        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        env.put(ToolConstants.CFG_GEN_SERVER, ToolConstants.CFG_GEN_SERVER);
        processor.setEnvironment(env);
        processor.process();
        File file =  new File(output.getCanonicalPath() + "/org/objectweb/hello_world_soap_http/");
        File[] files = file.listFiles(new java.io.FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) {
                    return true;
                }
                return false;

            }
        });
        assertTrue("Should generate 1 file", files != null && files.length == 1);

    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaClientServerTest.class.getResource(wsdlFile).getFile();
    }

}
