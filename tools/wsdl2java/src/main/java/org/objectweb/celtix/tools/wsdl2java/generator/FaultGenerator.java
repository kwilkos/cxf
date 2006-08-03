package org.objectweb.celtix.tools.wsdl2java.generator;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaExceptionClass;
import org.objectweb.celtix.tools.common.model.JavaField;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.util.ProcessorUtil;

public class FaultGenerator extends AbstractGenerator {

    private static final String FAULT_TEMPLATE = TEMPLATE_BASE + "/fault.vm";

    public FaultGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        super(jmodel, env);
        this.name = ToolConstants.FAULT_GENERATOR;
    }


    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_GEN_CLIENT) 
            || env.optionSet(ToolConstants.CFG_GEN_SERVER)) {
            return true;
        }
        return false;
    }

    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, JavaExceptionClass> exceptionClasses = javaModel
                .getExceptionClasses();
        for (Iterator iter = exceptionClasses.keySet().iterator(); iter
                .hasNext();) {
            String expClassName = (String)iter.next();
            JavaExceptionClass expClz = 
                exceptionClasses.get(expClassName);

            clearAttributes();
            setAttributes("expClass", expClz);
            for (JavaField jf : expClz.getFields()) {
                setAttributes("paraName", ProcessorUtil.mangleNameToVariableName(jf.getName()));
            }
            setCommonAttributes();
            doWrite(FAULT_TEMPLATE, parseOutputName(expClz.getPackageName(),
                    expClz.getName()));
        }
    }
}
