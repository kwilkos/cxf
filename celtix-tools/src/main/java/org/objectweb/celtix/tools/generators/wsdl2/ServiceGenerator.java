package org.objectweb.celtix.tools.generators.wsdl2;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
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
        return false;
    }
    
    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }
        
        Map<String, JavaServiceClass> serviceClasses = javaModel.getServiceClasses();
        
        Iterator ite = serviceClasses.values().iterator();
        
        while (ite.hasNext()) {
            
            JavaServiceClass js = (JavaServiceClass)ite.next();
            String location = (String)env.get(ToolConstants.CFG_WSDLURL);
            URL url = null;
            try {
                url = ProcessorUtil.getWSDLURL(location);
            } catch (Exception e) {
                throw new ToolException("Can not get WSDL location from: " + location, e);
            }

            clearAttributes();
            setAttributes("service", js);
            setAttributes("wsdlLocation", url.toString());
            setCommonAttributes();

            doWrite(SERVICE_TEMPLATE, parseOutputName(js.getPackageName(), js.getName()));
        }
    }
}
