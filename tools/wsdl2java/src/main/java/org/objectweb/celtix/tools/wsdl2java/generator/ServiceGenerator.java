package org.objectweb.celtix.tools.wsdl2java.generator;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.util.ProcessorUtil;

public class ServiceGenerator extends AbstractGenerator {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractGenerator.class);
    private static final String SERVICE_TEMPLATE = TEMPLATE_BASE + "/service.vm";

    public ServiceGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        super(jmodel, env);
        this.name = ToolConstants.SERVICE_GENERATOR;
    }

    public boolean passthrough() {  
        if (env.optionSet(ToolConstants.CFG_GEN_SERVER)) {
            return true;
        }
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
                Message message = new Message("FAIL_TO_GET_WSDL", LOG, location);
                throw new ToolException(message, e);
            }

            clearAttributes();
            
            setAttributes("service", js);
            setAttributes("wsdlLocation", url.toString());
            setCommonAttributes();

            doWrite(SERVICE_TEMPLATE, parseOutputName(js.getPackageName(), js.getName()));
        }
    }
}
