package org.objectweb.celtix.tools.processors.java2;

import java.io.File;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.objectweb.celtix.helpers.WSDLHelper;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.processors.wsdl2.WSDLToJavaProcessor;

public class JavaToWSDLProcessorTest extends ProcessorTestBase {

    private JavaToWSDLProcessor j2wProcessor;
    private WSDLToJavaProcessor wj2Processor;
    private String tns = "org.objectweb.asyn_lit";
    private String serviceName = "celtixService";
    private WSDLHelper wsdlHelper = new WSDLHelper();
    private File classFile;
    
    public void setUp() throws Exception {
        super.setUp();
        wj2Processor = new WSDLToJavaProcessor();
        j2wProcessor = new JavaToWSDLProcessor();
        classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                           + File.separatorChar);
    }
    
    public void tearDown() {
        super.tearDown();
        j2wProcessor = null;
        wj2Processor = null;
    }

    public void testAsyn() throws Exception {
        
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_async.wsdl"));
        wj2Processor.setEnvironment(env);
        wj2Processor.process();


        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/asyn.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_async_soap_http.GreeterAsync");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

        File wsdlFile = new File(output, "asyn.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Fail to generate schema file", schemaFile.exists());

    }

    public void testDocWrapparBare() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_doc_wrapped_bare.wsdl"));
        wj2Processor.setEnvironment(env);
        wj2Processor.process();


        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped_bare.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_doc_wrapped_bare.Greeter");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        
        File wsdlFile = new File(output, "doc_wrapped_bare.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());
        
        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);
        
        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Fail to generate schema file", schemaFile.exists());

    }
    
    
    public void testDocLitUseClassPathFlag() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_doc_lit.wsdl"));
        wj2Processor.setEnvironment(env);
        wj2Processor.process();
 
        System.setProperty("java.class.path", "");
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_doc_lit.Greeter");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_CLASSPATH, classFile.getCanonicalPath());
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "doc_lit.wsdl");
        assertTrue("Generate Wsdl Fail", wsdlFile.exists());
        
        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);
        
        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Generate schema file Fail", schemaFile.exists());
        
    }
    
    public void testRPCLit() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
        wj2Processor.setEnvironment(env);
        wj2Processor.process();
   
        env.put(ToolConstants.CFG_OUTPUTFILE,
                output.getPath() + "/rpc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_rpclit.GreeterRPCLit");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "rpc_lit.wsdl");
        assertTrue(wsdlFile.exists());
        
        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);
        
        
        File schemaFile = new File(output, "schema1.xsd");        
        assertTrue(schemaFile.exists());
        File schemaFile2 = new File(output, "schema2.xsd");        
        assertTrue(schemaFile2.exists());
    }
    
    
    private String getLocation(String wsdlFile) {
        return JavaToWSDLProcessorTest.class.getResource(wsdlFile).getFile();
    }

}
