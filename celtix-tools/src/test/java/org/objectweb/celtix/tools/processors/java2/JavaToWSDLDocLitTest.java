package org.objectweb.celtix.tools.processors.java2;

import java.io.File;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.utils.WSDLParserUtil;

public class JavaToWSDLDocLitTest extends ProcessorTestBase {

    JavaToWSDLProcessor processor = new JavaToWSDLProcessor();
    
    private String tns = "org.objectweb.doc_lit";
    private String serviceName = "celtixService";
    
    
    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_doc_lit.Greeter");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }

    public void testProcess() throws Exception {
        processor.setEnvironment(env);
        processor.process();
        File wsdlFile = new File(output, "doc_lit.wsdl");
        assertTrue("Generate Wsdl Fail", wsdlFile.exists());
        
        Definition def = WSDLParserUtil.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);
        
        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Generate schema file Fail", schemaFile.exists());
        
    }
}
