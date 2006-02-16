package org.objectweb.celtix.tools.generators.java2;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.WSDLModel;

public class WSDLGenerator {
    private final WSDLModel wmodel;

    private final ProcessorEnvironment env;

    private final Definition definition;

    private WSDLFactory wsdlFactory;

    private String wsdlFile;

    private String portTypeName;

    public WSDLGenerator(WSDLModel model, ProcessorEnvironment penv) {
        wmodel = model;
        env = penv;
        definition = model.getDefinition();

        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (javax.wsdl.WSDLException e) {
            throw new ToolException("Generate definition error ", e);
        }
    }

    public void generate() {
        preGenerate();
        TypesGenerator typeGen = new TypesGenerator(wmodel , env);
        typeGen.generate();
        MessagePortTypeGenerator messagePortTypeGen = new MessagePortTypeGenerator(wmodel);
        messagePortTypeGen.generate();
        BindingGenerator bindingGen = new BindingGenerator(wmodel);
        bindingGen.generate();
        ServiceGenerator serviceGen = new ServiceGenerator(wmodel);
        serviceGen.generate();
        writeDefinition();

    }

    private void preGenerate() {
        Object obj = env.get(ToolConstants.CFG_OUTPUTFILE);
        wsdlFile = obj == null ? "./" + wmodel.getServiceName() + ".wsdl" : (String)obj;
        obj = env.get(ToolConstants.CFG_TNS);
        String targetNameSpace;
        targetNameSpace = obj == null ? wmodel.getTargetNameSpace() : (String)obj;
        wmodel.setTargetNameSpace(targetNameSpace);
        obj = env.get(ToolConstants.CFG_PORTTYPE);
        portTypeName = obj == null ? wmodel.getPortName() : (String)obj;
        wmodel.setPortName(portTypeName);

    }

    private boolean writeDefinition() {

        WSDLWriter writer = wsdlFactory.newWSDLWriter();

        java.io.File file = new java.io.File(wsdlFile);
        java.io.OutputStream outstream = null;
        try {
            outstream = new java.io.FileOutputStream(file);
        } catch (java.io.FileNotFoundException e) {
            throw new ToolException("Create WSDL file error", e);
        }

        try {
            writer.writeWSDL(this.definition, outstream);

        } catch (javax.wsdl.WSDLException e) {
            throw new ToolException("Generate definition error ", e);
        }
        return true;
    }

    
 

}
