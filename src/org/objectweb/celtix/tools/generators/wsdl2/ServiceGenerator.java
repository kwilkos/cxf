package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.generators.AbstractGenerator;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class ServiceGenerator extends AbstractGenerator {

    private static final String SERVICE_TEMPLATE = TEMPLATE_BASE + "/service.vm";
    private JavaModel javaModel;


    public ServiceGenerator() {
        this.name = "wsdl2.service.generator";
    }

    public ServiceGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        this();
        javaModel = jmodel;
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_IMPL)
            || env.optionSet(ToolConstants.CFG_TYPES)
            || env.optionSet(ToolConstants.CFG_SERVER)
            || env.optionSet(ToolConstants.CFG_INTERFACE)) {
            return true;
        } 
        return false;
    }
    
    public void generate() throws Exception {
        if (passthrough()) {
            return;
        }
        if (javaModel == null) {
            throw new Exception("no java model is generated");
        }
        
        Map<String, JavaServiceClass> serviceClasses = javaModel.getServiceClasses();
        
        Iterator ite = serviceClasses.values().iterator();
        
        while (ite.hasNext()) {
            JavaServiceClass js = (JavaServiceClass)ite.next();
            String wsdlURL = (String)env.get(ToolConstants.CFG_WSDLURL);
            String loc = ProcessorUtil.getAbsolutePath(wsdlURL);            
            clearAttributes();
            setAttributes("service", js);
            setAttributes("wsdlLocation", loc);
            setCommonAttributes();            
            doWrite(SERVICE_TEMPLATE, 
                    parseOutputName(js.getPackageName(), 
                                    ProcessorUtil.mangleNameToClassName(js.getName())));           
        }
    }
}
