package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaExceptionClass;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class FaultGenerator extends AbstractGenerator {

    private static final String FAULT_TEMPLATE = TEMPLATE_BASE + "/fault.vm";
    private JavaModel javaModel;
        
    public FaultGenerator() {
        this.name = "wsdl2.fault.generator";
    }

    public FaultGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        this();
        javaModel = jmodel;
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_TYPES)
            || env.optionSet(ToolConstants.CFG_INTERFACE)) {
            return true;
        }
        return false;
    }
    
    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, JavaExceptionClass> exceptionClasses = javaModel.getExceptionClasses();
        for (Iterator iter = exceptionClasses.keySet().iterator(); iter.hasNext();) {
            String expClassName = (String) iter.next();
            JavaExceptionClass expClz = exceptionClasses.get(expClassName);

            clearAttributes();
            setAttributes("expClass", expClz);
            setCommonAttributes();

            doWrite(FAULT_TEMPLATE, parseOutputName(expClz.getPackageName(), expClz.getName()));
        }
    }
}
