package org.objectweb.celtix.tools.processors.java2;

import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.generators.java2.WSDLGenerator;
import org.objectweb.celtix.tools.processors.java2.internal.ClassProcessor;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class JavaToWSDLProcessor implements Processor {

    private WSDLModel model;
    private ProcessorEnvironment penv;
    private Class seiClass;

    public void process() throws Exception {
        model = new WSDLModel();
 
        init();
        buildModel(model, getSEIClass());

        WSDLGenerator generator = new WSDLGenerator(model);
        generator.generate();
    }

    public void buildModel(WSDLModel wmodel, Class clazz) throws Exception {
        ClassProcessor classproc = new ClassProcessor(clazz);
        classproc.process(wmodel);
        printModel();
    }

    public void setEnvironment(ProcessorEnvironment env) {
        this.penv = env;
    }

    public ProcessorEnvironment getEnvironment() {
        return this.penv;
    }

    protected void init() {
        seiClass = new AnnotationUtil().loadClass((String)penv.get(ToolConstants.CFG_CLASSNAME));
    }

    protected Class getSEIClass() {
        return seiClass;
    }

    public WSDLModel getModel() {
        return this.model;
    }

    public void printModel() {
        echo("---------Print WSDLModel---------");
        echo("** PortType Name ** : " + model.getPortyTypeName());
        echo("** Service  Name ** : " + model.getServiceName());
        echo("** TNS      Name ** : " + model.getTargetNameSpace());
        echo("** WSDL Location ** : " + model.getWsdllocation());
    }

    public void echo(String tmp) {
        System.out.println(tmp);
    }
}
