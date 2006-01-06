package org.objectweb.celtix.tools.processors.java2;

import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.java2.WSDLGenerator;
import org.objectweb.celtix.tools.processors.java2.internal.ClassProcessor;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class JavaToWSDLProcessor implements Processor {

    private WSDLModel model;
    private ProcessorEnvironment penv;
    private Class seiClass;

    public void process() throws ToolException {
        try {
            model = new WSDLModel();
        } catch (Exception e) {
            throw new ToolException("Build WSDL Model Fail");
        }

        init();
        buildModel(model, getSEIClass());
        model.createJAXBContext();
        final WSDLGenerator generator = new WSDLGenerator(model, penv);
        generator.generate();
    }

    public void buildModel(WSDLModel wmodel, Class clazz) throws ToolException {
        final ClassProcessor classproc = new ClassProcessor(clazz, getEnvironment());
        classproc.process(wmodel);
    }

    public void setEnvironment(ProcessorEnvironment env) {
        this.penv = env;
    }

    public ProcessorEnvironment getEnvironment() {
        return this.penv;
    }

    protected void init() {
        seiClass = AnnotationUtil.loadClass((String)penv.get(ToolConstants.CFG_CLASSNAME));
    }

    protected Class getSEIClass() {
        return seiClass;
    }

    public WSDLModel getModel() {
        return this.model;
    }

}
