package org.objectweb.celtix.tools.processors.java2;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.generators.java2.WSDLGenerator;
import org.objectweb.celtix.tools.processors.java2.internal.ClassProcessor;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

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
