package org.objectweb.celtix.tools.processors.java2.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jws.WebService;

import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class ClassProcessor {

    Class seiClass;
    WSDLModel model;
    Map<Class, Boolean> webMethodClasses = new HashMap<Class, Boolean>();

    public ClassProcessor(Class clz) {
        seiClass = clz;
    }

    public void process(WSDLModel wmodel) throws Exception {
        model = wmodel;

        populateWSDLInfo(seiClass);

    }

    private void populateWSDLInfo(Class clazz) throws Exception {
        WebService webService = AnnotationUtil.getPrivClassAnnotation(clazz, WebService.class);
        if (webService == null) {
            throw new Exception("SEI Class :No Webservice Annotation");

        }
        if (webService.endpointInterface().length() > 0) {
            clazz = new AnnotationUtil().loadClass(webService.endpointInterface());
            webService = AnnotationUtil.getPrivClassAnnotation(clazz, WebService.class);
            if (webService == null) {
                throw new Exception("Endpoint Interface :No Webservice ");
            }
        }

        String portTypeName = clazz.getSimpleName() + "PORT";
        model.setPortyTypeName(portTypeName);
        String serviceName = clazz.getSimpleName() + "SERVICE";
        String packageName = "";
        if (clazz.getPackage() != null) {
            packageName = clazz.getPackage().getName();
        }
        model.setPackageName(packageName);

        if (webService.serviceName().length() > 0) {
            serviceName = webService.serviceName();
        }
        model.setServiceName(serviceName);
        String targetNamespace = getNamespace(packageName);
        if (webService.targetNamespace().length() > 0) {
            targetNamespace = webService.targetNamespace();
        } else if (targetNamespace == null) {
            throw new Exception("Class Nopackage");
        }
        model.setTargetNameSpace(targetNamespace);
        String wsdlLocation = webService.wsdlLocation();
        model.setWsdllocation(wsdlLocation);
    }

    private String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i = tokenizer.countTokens() - 1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuffer namespace = new StringBuffer("http://");
        String dot = "";
        for (int i = 0; i < tokens.length; i++) {
            if (i == 1) {
                dot = ".";
            }
            namespace.append(dot + tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

}
