package org.apache.cxf.tools.java2wsdl.processor;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;

import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.WSDLModel;

import org.apache.cxf.tools.java2wsdl.generator.WSDLGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.ClassProcessor;
import org.apache.cxf.tools.util.AnnotationUtil;


public class JavaToWSDLProcessor implements Processor {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    private WSDLModel model;
    private ProcessorEnvironment penv;
    private Class seiClass;

    public void process() throws ToolException {
        try {
            model = new WSDLModel();
        } catch (Exception e) {
            Message msg = new Message("FAIL_TO_BUILD_WSDLMODEL", LOG);
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg);
        }

        init();
        buildModel(model, getSEIClass());
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
        if (penv.get(ToolConstants.CFG_CLASSPATH) != null) {
            String newCp = (String)penv.get(ToolConstants.CFG_CLASSPATH);
            String classpath = System.getProperty("java.class.path");
            System.setProperty("java.class.path", newCp + File.pathSeparator + classpath);
        }
        seiClass = AnnotationUtil.loadClass((String)penv.get(ToolConstants.CFG_CLASSNAME), seiClass == null
            ? JavaToWSDLProcessor.class.getClassLoader() : getSEIClass().getClassLoader());
    }

    protected Class getSEIClass() {
        return seiClass;
    }

    public WSDLModel getModel() {
        return this.model;
    }

}
