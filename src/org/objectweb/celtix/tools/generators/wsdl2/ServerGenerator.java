package org.objectweb.celtix.tools.generators.wsdl2;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class ServerGenerator extends AbstractGenerator {

    private static final String SRV_TEMPLATE = TEMPLATE_BASE + "/server.vm";
    private JavaModel javaModel;

    public ServerGenerator() {
        this.name = "wsdl2.server.generator";
    }

    public ServerGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        this();
        javaModel = jmodel;
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_IMPL)
            || env.optionSet(ToolConstants.CFG_TYPES)
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
            String interfaceName = (String)iter.next();
            JavaInterface intf = interfaces.get(interfaceName);
            String address = "";

            Iterator it = javaModel.getServiceClasses().values().iterator();
            while (it.hasNext()) {
                JavaServiceClass js = (JavaServiceClass)it.next();
                Iterator i = js.getPorts().iterator();
                while (i.hasNext()) {
                    JavaPort jp = (JavaPort)i.next();
                    if (jp.getPortType() == interfaceName) {
                        address = jp.getBindingAdress();
                        break;
                    }
                }
                if (!"".equals(address)) {
                    break;
                }
            }
            clearAttributes();
            setAttributes("intf", intf);
            setAttributes("address", address);
            setCommonAttributes();

            doWrite(SRV_TEMPLATE, parseOutputName(intf.getPackageName(), interfaceName + "Server"));
        }
    }

}
