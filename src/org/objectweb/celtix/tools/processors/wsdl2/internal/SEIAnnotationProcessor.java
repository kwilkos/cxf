package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaPort;

public class SEIAnnotationProcessor {

    private static Map<String, String> bindingMap = new HashMap<String, String>();
    static {
        bindingMap.put("RPC", "SOAPBinding.Style.RPC");
        bindingMap.put("DOCUMENT", "SOAPBinding.Style.DOCUMENT");
        bindingMap.put("LITERAL", "SOAPBinding.Use.LITERAL");
        bindingMap.put("ENCODED", "SOAPBinding.Use.ENCODED");
        bindingMap.put("BARE", "SOAPBinding.ParameterStyle.Bare");
        bindingMap.put("WRAPPED", "SOAPBinding.ParameterStyle.WRAPPED");
    }

    public SEIAnnotationProcessor(ProcessorEnvironment penv) {
    }
    
    public void process(JavaModel javaModel) {
        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String) iter.next();
            JavaInterface intf = interfaces.get(interfaceName);
            
            JavaAnnotation serviceAnnotation = new JavaAnnotation("WebService");
            serviceAnnotation.addArgument("targetNamespace", intf.getNamespace());
            serviceAnnotation.addArgument("wsdlLocation", intf.getLocation());
            serviceAnnotation.addArgument("name", intf.getName());

            intf.addAnnotation(serviceAnnotation.toString());

            processBinding(intf);
            
            JavaAnnotation bindingAnnotation = new JavaAnnotation("SOAPBinding");
            bindingAnnotation.setToken("");
            bindingAnnotation.addArgument("style", getBindingAnnotation(intf.getSOAPStyle().toString()));
            bindingAnnotation.addArgument("use", getBindingAnnotation(intf.getSOAPUse().toString()));
            if (intf.getSOAPStyle() == JavaPort.SOAPStyle.DOCUMENT) {
                bindingAnnotation.addArgument("parameterStyle",
                                              getBindingAnnotation(intf.getSOAPParameterStyle().toString()));
            }
            intf.addAnnotation(bindingAnnotation.toString());
        }        
    }

    private void processBinding(JavaInterface intf) {
        boolean isDOC = true;
        boolean isLiteral = true;
        boolean isWrapped = true;
        for (JavaMethod method : intf.getMethods()) {
            if (!method.isWrapperStyle()) {
                isWrapped = false;
            }
            if (method.getSoapStyle() == JavaPort.SOAPStyle.RPC) {
                isDOC = false;
            }
            if (method.getSoapUse() == JavaPort.SOAPUse.ENCODED) {
                isLiteral = false;
            }
        }

        if (isDOC) {
            intf.setSOAPStyle(JavaPort.SOAPStyle.DOCUMENT);
            if (isWrapped) {
                intf.setSOAPParameterStyle(JavaPort.SOAPParameterStyle.WRAPPED);
            } else {
                intf.setSOAPParameterStyle(JavaPort.SOAPParameterStyle.BARE);
            }
        } else {
            intf.setSOAPStyle(JavaPort.SOAPStyle.RPC);
        }
        if (isLiteral) {
            intf.setSOAPUse(JavaPort.SOAPUse.LITERAL);
        } else {
            intf.setSOAPUse(JavaPort.SOAPUse.ENCODED);
        }
    }

    public String getBindingAnnotation(String key) {
        return bindingMap.get(key.toUpperCase());
    }
}
