package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class ImplGenerator extends AbstractGenerator {

    private static final String IMPL_TEMPLATE = TEMPLATE_BASE + "/impl.vm";
    private JavaModel javaModel;
        
    public ImplGenerator() {
        this.name = "wsdl2.impl.generator";
    }

    public ImplGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        this();
        javaModel = jmodel;
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_TYPES)
            || env.optionSet(ToolConstants.CFG_CLIENT)
            || env.optionSet(ToolConstants.CFG_INTERFACE)) {
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

            doWrite(IMPL_TEMPLATE, parseOutputName(intf.getPackageName(), intf.getName() + "Impl"));
        }        
    }

}
