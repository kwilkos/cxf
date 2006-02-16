package org.objectweb.celtix.tools.processors.java2.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.OperationType;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.utils.AnnotationUtil;
import org.objectweb.celtix.tools.utils.URIParserUtil;

public class ClassProcessor {

    Class seiClass;

    WSDLModel model;

    Map<Class, Boolean> useWebMethodClasses = new HashMap<Class, Boolean>();

    private final ProcessorEnvironment env;

    public ClassProcessor(Class clz, ProcessorEnvironment penv) {
        seiClass = clz;
        env = penv;
    }

    public void process(WSDLModel wmodel) {
        model = wmodel;
        populateWSDLInfo(seiClass);
        checkWebMethodUseClass(seiClass);
        for (Method method : seiClass.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class) || !isOperationToGen(method, seiClass)) {
                continue;
            }
            processMethod(wmodel, method);
        }
    }

    private void processMethod(WSDLModel wmodel, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return;
        }

        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        if (webMethod == null || (webMethod != null && webMethod.exclude())) {
            return;
        }

        JavaMethod javaMethod = new JavaMethod();

        // rule 3.5

        String operationName = method.getName();

        if (!method.getDeclaringClass().equals(seiClass)) {
            try {
                Method tmp = seiClass.getMethod(method.getName(), (Class[])method.getParameterTypes());
                operationName = tmp.getName();
            } catch (NoSuchMethodException e) {
                throw new ToolException(e.getMessage(), e);
            }
        }

        if (webMethod != null) {
            operationName = webMethod.operationName().length() > 0
                ? webMethod.operationName() : operationName;
        }

        javaMethod.setName(operationName);
        javaMethod.setSoapAction(webMethod.action());

        // To Do Asyn

        if (isOneWayMethod(method)) {
            javaMethod.setStyle(OperationType.ONE_WAY);
        } else {
            javaMethod.setStyle(OperationType.REQUEST_RESPONSE);
        }

        switch (getMethodType(method)) {
        case WSDLConstants.DOC_BARE:
            DocBareMethodProcessor docBareProcessor = new DocBareMethodProcessor(model);
            docBareProcessor.processDocBare(javaMethod, method);
            break;
        case WSDLConstants.DOC_WRAPPED:
            DocWrapperMethodProcessor docWrapperProcessor = new DocWrapperMethodProcessor(model);
            docWrapperProcessor.process(javaMethod, method);
            break;
        case WSDLConstants.RPC_WRAPPED:
            RPCMethodProcessor rpcMethodProcessor = new RPCMethodProcessor(model);
            rpcMethodProcessor.process(javaMethod, method);
            break;
        default:
            throw new ToolException("the method " + method.getName()
                                    + "SOAPBinding USE STYLE and ParameterStyle is not correct ");
        }
        wmodel.addJavaMethod(javaMethod);
    }

    private int getMethodType(Method method) {
        SOAPBinding binding = method.getAnnotation(SOAPBinding.class);
        int result = WSDLConstants.ERORR_STYLE_USE;
        if (binding != null) {
            if (binding.style() == SOAPBinding.Style.RPC) {
                result = WSDLConstants.RPC_WRAPPED;
            }
            if (binding.style() == SOAPBinding.Style.DOCUMENT
                && binding.parameterStyle() == SOAPBinding.ParameterStyle.WRAPPED) {
                result = WSDLConstants.DOC_WRAPPED;
            }
            if (binding.style() == SOAPBinding.Style.DOCUMENT
                && binding.parameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                result = WSDLConstants.DOC_BARE;
            }

        } else {
            if (model.isRPC() && model.isWrapped()) {
                result = WSDLConstants.RPC_WRAPPED;
            }
            if (model.isDocLit() && model.isWrapped()) {
                result = WSDLConstants.DOC_WRAPPED;
            }
            if (model.isDocLit() && !model.isWrapped()) {
                result = WSDLConstants.DOC_BARE;
            }
        }
        return result;
    }

    private boolean isOperationToGen(Method method, Class clazz) {
        if (clazz.isInterface()) {
            return true;
        }
        Class declareClass = method.getDeclaringClass();
        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        if (webMethod != null && !webMethod.exclude()) {
            return true;
        }
        if (AnnotationUtil.getPrivClassAnnotation(declareClass, WebService.class) != null
            && !useWebMethodClasses.get(declareClass)) {
            return true;
        }
        return false;

    }

    // for rule 3.3
    private void checkWebMethodUseClass(Class clz) {
        if (clz == null) {
            return;
        }
        if (clz.isInterface()) {
            useWebMethodClasses.put(clz, false);
        } else {
            WebMethod webMethod;
            boolean existWebMethod = false;
            for (Method method : clz.getMethods()) {
                if (!method.getDeclaringClass().equals(seiClass)) {
                    continue;
                }
                webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
                if (webMethod != null && !webMethod.exclude()) {
                    existWebMethod = true;
                    break;
                }
            }
            useWebMethodClasses.put(clz, existWebMethod);
        }
        checkWebMethodUseClass(clz.getSuperclass());
    }

    private void populateWSDLInfo(Class clazz) {   
        WebService webService = AnnotationUtil.getPrivClassAnnotation(clazz, WebService.class);
        if (webService == null) {
            throw new ToolException("SEI Class :No Webservice Annotation");

        }
        if (webService.endpointInterface().length() > 0) {
            clazz = AnnotationUtil.loadClass(webService.endpointInterface(), clazz.getClassLoader());
            webService = AnnotationUtil.getPrivClassAnnotation(clazz, WebService.class);
            if (webService == null) {
                throw new ToolException("Endpoint Interface :No Webservice ");
            }
        }

        String portTypeName = clazz.getSimpleName() + "PortType";
        if (webService.name().length() > 0) {
            portTypeName = webService.name();
        }

        model.setPortTypeName(portTypeName);

        String portName = clazz.getSimpleName() + "Port";

        if (webService.portName().length() > 0) {
            portName = webService.portName();
        } else if (webService.name().length() > 0) {
            portName = webService.name() + "Port";

        }
        model.setPortName(portName);

        String serviceName = clazz.getSimpleName() + "Service";
        if (webService.serviceName().length() > 0) {
            serviceName = webService.serviceName();
        }
        model.setServiceName(serviceName);

        String packageName = "";
        if (clazz.getPackage() != null) {
            packageName = clazz.getPackage().getName();
        }
        model.setPackageName(packageName);

        String targetNamespace = URIParserUtil.getNamespace(packageName);
        if (env.optionSet(ToolConstants.CFG_TNS)) {
            targetNamespace = (String)env.get(ToolConstants.CFG_TNS);
        } else if (webService.targetNamespace().length() > 0) {
            targetNamespace = webService.targetNamespace();
        } else if (targetNamespace == null) {
            throw new ToolException("Class No Package");
        }
        model.setTargetNameSpace(targetNamespace);
        String wsdlLocation = webService.wsdlLocation();
        model.setWsdllocation(wsdlLocation);

        javax.jws.soap.SOAPBinding soapBinding = AnnotationUtil
            .getPrivClassAnnotation(clazz, javax.jws.soap.SOAPBinding.class);
        if (soapBinding != null) {
            model.setStyle(soapBinding.style());
            model.setUse(soapBinding.use());
            model.setPrameterStyle(soapBinding.parameterStyle());
        }
        
        
    }

  

    private boolean isOneWayMethod(Method method) {
        return method.isAnnotationPresent(Oneway.class);
    }

}
