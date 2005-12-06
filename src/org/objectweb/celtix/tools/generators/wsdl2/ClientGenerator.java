package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.Iterator;
import java.util.Map;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class ClientGenerator extends AbstractGenerator {

    private static final String CLT_TEMPLATE = TEMPLATE_BASE + "/client.vm";
    private JavaModel javaModel;

    public ClientGenerator() {
        this.name = "wsdl2.client.generator";
    }

    public ClientGenerator(JavaModel jmodel, ProcessorEnvironment env) {
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

        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String)iter.next();
            JavaInterface intf = interfaces.get(interfaceName);

            // get the ref service name
            String serviceName = "";
            Iterator it = javaModel.getServiceClasses().values().iterator();
            while (it.hasNext()) {
                JavaServiceClass js = (JavaServiceClass)it.next();
                Iterator i = js.getPorts().iterator();
                while (i.hasNext()) {
                    JavaPort jp = (JavaPort)i.next();
                    if (jp.getPortType() == interfaceName) {                  
                        serviceName = js.getName();
                        break;
                    }
                }
                if (!"".equals(serviceName)) {
                    break;
                }
            }

            clearAttributes();
            setAttributes("intf", intf);
            setAttributes("serviceName", serviceName);
            setCommonAttributes();

            doWrite(CLT_TEMPLATE, parseOutputName(intf.getPackageName(), "Client"));
        }
    }
}
