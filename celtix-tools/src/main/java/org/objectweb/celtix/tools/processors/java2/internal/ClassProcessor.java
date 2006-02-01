package org.objectweb.celtix.tools.processors.java2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.OperationType;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.model.nav.Navigator;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.model.WSDLException;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.common.model.WSDLWrapperParameter;
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
            if (method.getDeclaringClass().equals(Object.class)
                    || !isOperationToGen(method, seiClass)) {
                continue;
            }
            processMethod(wmodel, method);
        }
    }

    private void processMethod(WSDLModel wmodel, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return;
        }

        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method,
                WebMethod.class);
        if (webMethod == null || (webMethod != null && webMethod.exclude())) {
            return;
        }

        JavaMethod javaMethod = new JavaMethod();

        // rule 3.5

        String operationName = method.getName();

        if (!method.getDeclaringClass().equals(seiClass)) {
            try {
                Method tmp = seiClass.getMethod(method.getName(),
                        (Class[]) method.getParameterTypes());
                operationName = tmp.getName();
            } catch (NoSuchMethodException e) {
                throw new ToolException(e.getMessage(), e);
            }
        }

        if (webMethod != null) {
            operationName = webMethod.operationName().length() > 0 ? webMethod
                    .operationName() : operationName;
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
            processDocBare(javaMethod, method);
            break;
        case WSDLConstants.DOC_WRAPPED:
            processDocWrapped(javaMethod, method);
            break;
        case WSDLConstants.RPC_WRAPPED:
            processRPC(javaMethod, method);
            break;
        default:
            throw new ToolException(
                    "the method "
                            + method.getName()
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

    private void setMethodUse(JavaMethod javaMethod, Method method) {
        SOAPBinding binding = method.getAnnotation(SOAPBinding.class);
        if (binding != null) {
            javaMethod.setSoapUse(binding.use());
        } else {
            javaMethod.setSoapUse(this.model.getUse());
        }
    }

    private void processDocWrapped(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(true);
        setMethodUse(javaMethod, method);

        // process request
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        String reqClassName = "";
        String reqName = method.getName();
        String reqNS = model.getTargetNameSpace();
        if (reqWrapper != null) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper
                    .className() : reqClassName;
            reqName = reqWrapper.localName().length() > 0 ? reqWrapper
                    .localName() : reqName;
            reqNS = reqWrapper.targetNamespace().length() > 0 ? reqWrapper
                    .targetNamespace() : reqNS;
        } else {
            reqClassName = model.getPackageName()
                    + AnnotationUtil.capitalize(method.getName());
        }

        Class reqClass = null;
        try {
            reqClass = AnnotationUtil.loadClass(reqClassName, method
                    .getDeclaringClass().getClassLoader());
        } catch (Exception e) {
            throw new ToolException("Can Not Load class " + reqClassName, e);
        }
        QName reqQN = new QName(reqNS, reqName);
        TypeReference typeRef = new TypeReference(reqQN, reqClass,
                new Annotation[0]);
        WSDLWrapperParameter reqWrapperPara = new WSDLWrapperParameter(reqName,
                typeRef, JavaType.Style.IN);
        reqWrapperPara.setTargetNamespace(reqNS);
        javaMethod.addWSDLWrapperParameter(reqWrapperPara);

        WSDLWrapperParameter resWrapperPara = null;
        if (!isOneWayMethod(method)) {
            // process response
            ResponseWrapper resWrapper = method
                    .getAnnotation(ResponseWrapper.class);
            String resClassName = "";
            // rule 3.5 suffix -"Response"
            String resName = method.getName() + "Response";
            String resNS = model.getTargetNameSpace();
            if (resWrapper != null) {
                resClassName = resWrapper.className();
                resName = resWrapper.localName().length() > 0 ? resWrapper
                        .localName() : resName;
                resNS = resWrapper.targetNamespace().length() > 0 ? resWrapper
                        .targetNamespace() : resNS;
            } else {
                resClassName = model.getPackageName()
                        + AnnotationUtil.capitalize(method.getName())
                        + "Response";
            }
            Class resClass = null;
            QName resQN = new QName(resNS, resName);
            try {
                resClass = AnnotationUtil.loadClass(resClassName, method
                        .getDeclaringClass().getClassLoader());
            } catch (Exception e) {
                throw new ToolException("Can Not Load Class " + resClassName, e);
            }
            typeRef = new TypeReference(resQN, resClass, new Annotation[0]);
            resWrapperPara = new WSDLWrapperParameter(resName, typeRef,
                    JavaType.Style.OUT);
            resWrapperPara.setTargetNamespace(resNS);
            javaMethod.addWSDLWrapperParameter(resWrapperPara);
            WebResult webResult = method.getAnnotation(WebResult.class);
            JavaParameter returnParameter = getReturnParameter(webResult,
                    method);
            if (returnParameter != null) {
                resWrapperPara.addWrapperChild(returnParameter);
            }
        }
        List<JavaParameter> paras = processWebPara(method);
        for (JavaParameter jp : paras) {

            reqWrapperPara.addWrapperChild(jp);
        }

        processExceptions(javaMethod, method);
    }

    private void processRPC(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.RPC);
        javaMethod.setWrapperStyle(true);
        setMethodUse(javaMethod, method);

        String targetNS = model.getTargetNameSpace();
        WSDLWrapperParameter requestWrapper = new WSDLWrapperParameter();
        requestWrapper.setName(method.getName());
        requestWrapper.setStyle(JavaType.Style.IN);

        requestWrapper.setTargetNamespace(targetNS);
        javaMethod.addWSDLWrapperParameter(requestWrapper);

        boolean isOneway = method.isAnnotationPresent(Oneway.class);
        if (!isOneway) {
            QName resQN = new QName(targetNS, method.getName() + "Response");
            TypeReference typeRef = new TypeReference(resQN, this.getClass(),
                    new Annotation[0]);
            WSDLWrapperParameter responseWrapper = new WSDLWrapperParameter();
            responseWrapper.setName(method.getName() + "Response");
            responseWrapper.setStyle(JavaType.Style.OUT);
            javaMethod.addWSDLWrapperParameter(responseWrapper);

            Class returnType = method.getReturnType();
            String resultName = method.getName() + "Response";
            String resultTNS = targetNS;
            String resultPartName = null;
            WebResult webResult = method.getAnnotation(WebResult.class);
            if (webResult != null) {
                resultName = webResult.name().length() > 0 ? webResult.name()
                        : resultName;
                resultPartName = webResult.partName().length() > 0 ? webResult
                        .partName() : resultName;
                resultTNS = webResult.targetNamespace().length() > 0 ? webResult
                        .targetNamespace()
                        : resultTNS;
            }
            QName resultQName = new QName(resultTNS, resultName);
            if (returnType != null && (!returnType.getName().equals("void"))) {
                // Annotation[] rann = method.getAnnotations();
                Annotation[] rann = new Annotation[0];
                typeRef = new TypeReference(resultQName, returnType, rann);
                JavaParameter returnParameter = new JavaParameter(resultName,
                        typeRef, JavaType.Style.OUT);
                returnParameter.setPartName(resultPartName);
                returnParameter.setTargetNamespace(resultTNS);
                responseWrapper.addWrapperChild(returnParameter);
            }
        }
        // get WebParam
        List<JavaParameter> paras = processWebPara(method);
        for (JavaParameter jp : paras) {
            requestWrapper.addWrapperChild(jp);
        }
        processExceptions(javaMethod, method);
    }

    private void processDocBare(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(false);
        setMethodUse(javaMethod, method);

        // process webresult annotation
        String resultName = method.getName() + "Response";
        String resultTNS = model.getTargetNameSpace();
        String resultPartName = null;

        WebResult webResult = method.getAnnotation(WebResult.class);

        if (webResult != null) {
            resultName = webResult.name().length() > 0 ? webResult.name()
                    : resultName;

            resultTNS = webResult.targetNamespace().length() > 0 ? webResult
                    .targetNamespace() : resultTNS;
            resultPartName = webResult.partName().length() > 0 ? webResult
                    .partName() : resultName;
        }

        // get return type class
        Class returnType = method.getReturnType();

        if (returnType != null && !returnType.getName().equals("void")) {

            QName resQN = new QName(resultTNS, resultName);
            TypeReference typeRef = new TypeReference(resQN, returnType,
                    new Annotation[0]);
            WSDLWrapperParameter responseWrapper = new WSDLWrapperParameter();
            responseWrapper.setStyle(JavaType.Style.OUT);
            responseWrapper.setTargetNamespace(resultTNS);
            JavaParameter jp = new JavaParameter(resultName, typeRef,
                    JavaType.Style.OUT);
            jp.setPartName(resultPartName);
            jp.setTargetNamespace(resultTNS);
            jp.setName(resultName);
            responseWrapper.addWrapperChild(jp);
            javaMethod.addWSDLWrapperParameter(responseWrapper);
        }

        // processWebparam
        WSDLWrapperParameter requestWrapper = new WSDLWrapperParameter();
        requestWrapper.setName(method.getName());
        requestWrapper.setStyle(JavaType.Style.IN);
        javaMethod.addWSDLWrapperParameter(requestWrapper);

        List<JavaParameter> paras = processWebPara(method);
        for (JavaParameter jp : paras) {
            requestWrapper.addWrapperChild(jp);
        }
        processExceptions(javaMethod, method);
    }

    private JavaParameter getReturnParameter(WebResult webResult, Method method) {
        boolean isHeader = false;
        String resultName = "Return";
        String resultTNS = model.getTargetNameSpace();
        JavaParameter jpara = null;
        QName resultQName = null;
        if (webResult != null) {
            resultName = webResult.name().length() > 0
                    && webResult.partName().length() > 0 ? webResult.partName()
                    : resultName;
            resultName = webResult.name().length() > 0 ? webResult.name()
                    : resultName;
            resultName = webResult.partName().length() > 0 ? webResult
                    .partName() : resultName;
            resultTNS = webResult.targetNamespace().length() > 0 ? webResult
                    .targetNamespace() : resultTNS;
            isHeader = webResult.header();
        }
        resultQName = new QName(resultTNS, resultName);
        Class returnType = method.getReturnType();
        if (resultQName != null && !isOneWayMethod(method)
                && (returnType != null)
                && (!returnType.getName().equals("void"))) {
            // Annotation[] annotations = method.getAnnotations();
            Annotation[] annotations = new Annotation[0];
            if (resultQName.getLocalPart() != null) {
                TypeReference rTypeReference = new TypeReference(resultQName,
                        returnType, annotations);
                jpara = new JavaParameter();
                jpara.setName(method.getName() + "Response");
                jpara.setTypeReference(rTypeReference);
                jpara.setStyle(JavaType.Style.OUT);
                jpara.setHeader(isHeader);
            }
        }
        return jpara;
    }

    private List<JavaParameter> processWebPara(Method method) {
        // processWebparam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] parameterGenTypes = method.getGenericParameterTypes();
        Annotation[][] paraAnns = AnnotationUtil
                .getPrivParameterAnnotations(method);
        List<JavaParameter> paras = new ArrayList<JavaParameter>();
        int i = 0;
        for (Class clazzType : parameterTypes) {
            String paraName = "arg" + i;
            String partName;
            String paraTNS = model.getTargetNameSpace();
            Class clazz = clazzType;
            if (isHoder(clazzType)) {
                clazz = getHoldedClass(clazzType, parameterGenTypes[i]);
            }
            for (Annotation anno : paraAnns[i]) {
                if (anno.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam) anno;
                    paraName = webParam.name().length() > 0 ? webParam.name()
                            : paraName;
                    partName = webParam.partName().length() > 0 ? webParam
                            .partName() : paraName;
                    paraTNS = webParam.targetNamespace().length() > 0 ? paraTNS = webParam
                            .targetNamespace()
                            : paraTNS;

                    QName requestQN = new QName(paraTNS, paraName);
                    TypeReference typeref = new TypeReference(requestQN, clazz,
                            paraAnns[i]);
                    JavaParameter jp = new JavaParameter(typeref.tagName
                            .getLocalPart(), typeref, JavaType.Style.IN);
                    jp.setPartName(partName);
                    jp.setHeader(webParam.header());
                    jp.setTargetNamespace(paraTNS);
                    paras.add(jp);
                }

            }
            i++;
        }

        return paras;
    }

    private void processExceptions(JavaMethod jmethod, Method method) {
        for (Type exception : method.getGenericExceptionTypes()) {
            if (RemoteException.class.isAssignableFrom((Class) exception)) {
                continue;
            }
            Annotation[] anns = null;
            Class<?> exClass = (Class<?>)exception;
            String exNameSpace = model.getTargetNameSpace();
            String exName = exClass.getSimpleName();
            Class exReturnType = null;
            Method faultInfo = null;
            try {
                faultInfo = exClass.getMethod("getFaultInfo", new Class[0]);
            } catch (SecurityException e) {
                throw new ToolException(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                throw new ToolException(e.getMessage(), e);
            }

            if (faultInfo != null) {
                WebFault wf = exClass.getAnnotation(WebFault.class);
                exReturnType = faultInfo.getReturnType();
                anns = faultInfo.getAnnotations();
                if (wf.targetNamespace().length() > 0) {
                    exNameSpace = wf.targetNamespace();
                }
                exName = wf.name();
            }

            QName exQName = new QName(exNameSpace, exName);
            TypeReference tf = new TypeReference(exQName, exReturnType, anns);
            WSDLException wsdlEx = new WSDLException(exClass, tf);

            try {
                jmethod.addWSDLException(wsdlEx);
            } catch (Exception e) {
                throw new ToolException("Exception Is Not Unique");
            }

        }
    }

    private boolean isOperationToGen(Method method, Class clazz) {
        if (clazz.isInterface()) {
            return true;
        }
        Class declareClass = method.getDeclaringClass();
        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method,
                WebMethod.class);
        if (webMethod != null && !webMethod.exclude()) {
            return true;
        }
        if (AnnotationUtil.getPrivClassAnnotation(declareClass,
                WebService.class) != null
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
                webMethod = AnnotationUtil.getPrivMethodAnnotation(method,
                        WebMethod.class);
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
        WebService webService = AnnotationUtil.getPrivClassAnnotation(clazz,
                WebService.class);
        if (webService == null) {
            throw new ToolException("SEI Class :No Webservice Annotation");

        }
        if (webService.endpointInterface().length() > 0) {
            clazz = AnnotationUtil.loadClass(webService.endpointInterface(),
                    clazz.getClassLoader());
            webService = AnnotationUtil.getPrivClassAnnotation(clazz,
                    WebService.class);
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
            targetNamespace = (String) env.get(ToolConstants.CFG_TNS);
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

    private boolean isHoder(Class cType) {
        return Holder.class.isAssignableFrom(cType);
        // set the actual type argument of Holder in the TypeReference
    }

    private Class getHoldedClass(Class holderClazz, Type type) {
        return Navigator.REFLECTION.erasure(((ParameterizedType) type)
                .getActualTypeArguments()[0]);
    }

    private boolean isOneWayMethod(Method method) {
        return method.isAnnotationPresent(Oneway.class);
    }

}
