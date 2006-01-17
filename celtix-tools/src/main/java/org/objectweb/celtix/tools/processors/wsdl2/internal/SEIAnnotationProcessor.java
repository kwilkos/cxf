package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;
import javax.jws.soap.SOAPBinding;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.utils.SOAPBindingUtil;

public class SEIAnnotationProcessor {

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
            serviceAnnotation.addArgument("name", intf.getWebServiceName());

            intf.addAnnotation(serviceAnnotation.toString());

            if (processBinding(intf)) {
                JavaAnnotation bindingAnnotation = new JavaAnnotation("SOAPBinding");
                String style = SOAPBindingUtil.getBindingAnnotation(intf.getSOAPStyle().toString());
                bindingAnnotation.addArgument("style", style, "");
                String use = SOAPBindingUtil.getBindingAnnotation(intf.getSOAPUse().toString());
                bindingAnnotation.addArgument("use", use, "");
                if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT) {
                    String parameterStyle = SOAPBindingUtil.getBindingAnnotation(intf.
                                                                                 getSOAPParameterStyle().
                                                                                 toString());
                    bindingAnnotation.addArgument("parameterStyle", parameterStyle, "");
                }
                intf.addAnnotation(bindingAnnotation.toString());
            }
        }        
    }

    private boolean processBinding(JavaInterface intf) {
        boolean isDOC = true;
        boolean isLiteral = true;
        boolean isWrapped = true;
        int count = 0;
        for (JavaMethod method : intf.getMethods()) {
            if (!method.isWrapperStyle()) {
                isWrapped = false;
                count++;
            }
            if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
                isDOC = false;
            }
            if (method.getSoapUse() == SOAPBinding.Use.ENCODED) {
                isLiteral = false;
            }
        }

        if (isDOC) {
            intf.setSOAPStyle(SOAPBinding.Style.DOCUMENT);
            if (isWrapped) {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.WRAPPED);
            } else {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.BARE);
            }
        } else {
            intf.setSOAPStyle(SOAPBinding.Style.RPC);
        }
        if (isLiteral) {
            intf.setSOAPUse(SOAPBinding.Use.LITERAL);
        } else {
            intf.setSOAPUse(SOAPBinding.Use.ENCODED);
        }

        if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT
            && count != 0
            && count != intf.getMethods().size()) {
            return false;
        }

        if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT
            && intf.getSOAPUse() == SOAPBinding.Use.LITERAL
            && intf.getSOAPParameterStyle() == SOAPBinding.ParameterStyle.WRAPPED) {
            return false;
        }
        return true;
    }
}
