package org.objectweb.celtix.tools.wsdl2java.generator;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;

public class ImplGenerator extends AbstractGenerator {

    private static final String IMPL_TEMPLATE = TEMPLATE_BASE + "/impl.vm";

   
    public ImplGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        super(jmodel, env);
        this.name = ToolConstants.IMPL_GENERATOR;
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_IMPL)
                || env.optionSet(ToolConstants.CFG_ALL)) {
            return false;
        }
        return true;
    }

    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        Map<String, JavaServiceClass> services = javaModel.getServiceClasses();
        String service = "";
        String port = "";
        if (!services.values().isEmpty()) {
            JavaServiceClass javaservice = services.values().iterator().next();
            service = javaservice.getServiceName();
            if (javaservice.getPorts().size() != 0) {
                JavaPort jport = (JavaPort)javaservice.getPorts().get(0);
                port = jport.getPortName();
            }
        }
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String)iter.next();
            JavaInterface intf = interfaces.get(interfaceName);

            clearAttributes();
            setAttributes("intf", intf);
           
            setAttributes("service", service);
            setAttributes("port", port);
           
            setCommonAttributes();

            doWrite(IMPL_TEMPLATE, parseOutputName(intf.getPackageName(), intf.getName() + "Impl"));
        }        
    }

}
