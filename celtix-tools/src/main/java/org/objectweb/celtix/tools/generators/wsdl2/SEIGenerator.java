package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class SEIGenerator extends AbstractGenerator {

    private static final String SEI_TEMPLATE = TEMPLATE_BASE + "/sei.vm";
    private JavaModel javaModel;
        
    public SEIGenerator() {
        this.name = "wsdl2.sei.generator.";
    }

    public SEIGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        this();
        javaModel = jmodel;
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_TYPES)) {
            return true;
        } 
        return false;
    }
    
    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String) iter.next();
            JavaInterface intf = interfaces.get(interfaceName);

            clearAttributes();
            setAttributes("intf", intf);
            setCommonAttributes();

            doWrite(SEI_TEMPLATE, parseOutputName(intf.getPackageName(), intf.getName()));
        }        
    }

}
