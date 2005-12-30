package org.objectweb.celtix.tools.processors.java2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    private void processDocWrapped(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(true);

        // process request
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        String reqClassName = "";
        String reqName = method.getName();
        String reqNS = model.getTargetNameSpace();
        if (reqWrapper != null) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper.className() : reqClassName;
            reqName = reqWrapper.localName().length() > 0 ? reqWrapper.localName() : reqName;
            reqNS = reqWrapper.targetNamespace().length() > 0 ? reqWrapper.targetNamespace() : reqNS;
        } else {
            reqClassName = model.getPackageName() + AnnotationUtil.capitalize(method.getName());
        }
        AnnotationUtil util = new AnnotationUtil();

        Class reqClass = null;
        try {
            reqClass = util.loadClass(reqClassName);
        } catch (Exception e) {
            throw new ToolException("Can Not Load class " + reqClassName, e);
        }
        QName reqQN = new QName(reqNS, reqName);
        TypeReference typeRef = new TypeReference(reqQN, reqClass, new Annotation[0]);
        WSDLWrapperParameter reqWrapperPara = new WSDLWrapperParameter(reqName, typeRef, JavaType.Style.IN);
        reqWrapperPara.setTargetNamespace(reqNS);
        javaMethod.addObjectParameter(reqWrapperPara);

        // process response
        ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
        String resClassName = "";
        // rule 3.5 suffix -"Response"
        String resName = method.getName() + "Response";
        String resNS = model.getTargetNameSpace();
        if (resWrapper != null) {
            resClassName = resWrapper.className();
            resName = resWrapper.localName().length() > 0 ? resWrapper.localName() : resName;
            resNS = resWrapper.targetNamespace().length() > 0 ? resWrapper.targetNamespace() : resNS;
        } else {
            resClassName = model.getPackageName() + AnnotationUtil.capitalize(method.getName()) + "Response";
        }
        Class resClass = null;
        QName resQN = new QName(resNS, resName);
        WSDLWrapperParameter resWrapperPara = null;
        if (!isOneWayMethod(method)) {
            try {
                resClass = util.loadClass(resClassName);
            } catch (Exception e) {
                throw new ToolException("Can Not Load Class " + resClass.getName(), e);
            }
            typeRef = new TypeReference(resQN, resClass, new Annotation[0]);
            resWrapperPara = new WSDLWrapperParameter(resName, typeRef, JavaType.Style.OUT);
            resWrapperPara.setTargetNamespace(resNS);
            javaMethod.addObjectParameter(resWrapperPara);
            WebResult webResult = method.getAnnotation(WebResult.class);
            JavaParameter returnParameter = getReturnParameter(webResult, method);
            if (returnParameter != null) {
                /*
                 * if (webResult.header()) {
                 * javaMethod.addObjectParameter(returnParameter); } else {
                 */
                resWrapperPara.addWrapperChild(returnParameter);
            }
        }
        List typeList = processWebPara(method);
        Iterator ite = typeList.iterator();
        while (ite.hasNext()) {
            TypeReference typeref = (TypeReference)ite.next();
            JavaParameter jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
            reqWrapperPara.addWrapperChild(jp);
        }

        processExceptions(javaMethod, method);
    }

    @SuppressWarnings("unchecked")
    private void processRPC(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.RPC);
        javaMethod.setWrapperStyle(true);
        String targetNS = model.getTargetNameSpace();
        QName reqQN = new QName(targetNS, method.getName());
        TypeReference typeRef = new TypeReference(reqQN, this.getClass(), new Annotation[0]);
        WSDLWrapperParameter requestWrapper = new WSDLWrapperParameter(method.getName(), typeRef,
                                                                       JavaType.Style.IN);
        requestWrapper.setTargetNamespace(targetNS);
        javaMethod.addObjectParameter(requestWrapper);

        boolean isOneway = method.isAnnotationPresent(Oneway.class);
        if (!isOneway) {
            QName resQN = new QName(targetNS, method.getName() + "Response");
            typeRef = new TypeReference(resQN, this.getClass(), new Annotation[0]);
            WSDLWrapperParameter responseWrapper = new WSDLWrapperParameter(method.getName() + "Response",
                                                                            typeRef, JavaType.Style.OUT);
            responseWrapper.setTargetNamespace(targetNS);
            javaMethod.addObjectParameter(responseWrapper);

            Class returnType = method.getReturnType();
            String resultName = "Return";
            String resultTNS = targetNS;
            WebResult webResult = method.getAnnotation(WebResult.class);
            if (webResult != null) {
                resultName = webResult.name().length() > 0 && webResult.partName().length() > 0 ? webResult
                    .partName() : resultName;
                resultName = webResult.name().length() > 0 ? webResult.name() : resultName;
                resultName = webResult.partName().length() > 0 ? webResult.partName() : resultName;
                resultTNS = webResult.targetNamespace().length() > 0
                    ? webResult.targetNamespace() : resultTNS;

            }
            QName resultQName = new QName(resultTNS, resultName);
            if (returnType != null && (!returnType.getName().equals("void"))) {
                Annotation[] rann = method.getAnnotations();
                typeRef = new TypeReference(resultQName, returnType, rann);
                JavaParameter returnParameter = new JavaParameter(resultName, typeRef, JavaType.Style.OUT);
                responseWrapper.addWrapperChild(returnParameter);
            }
        }
        // get WebParam

        List typeList = processWebPara(method);
        Iterator ite = typeList.iterator();
        while (ite.hasNext()) {
            TypeReference typeref = (TypeReference)ite.next();
            JavaParameter jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
            requestWrapper.addWrapperChild(jp);
        }
        processExceptions(javaMethod, method);
    }

    @SuppressWarnings("unchecked")
    private void processDocBare(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(false);

        // process webresult annotation
        String resultName = method.getName() + "Response";
        String resultTNS = model.getTargetNameSpace();
        String resultPartName = null;

        WebResult webResult = method.getAnnotation(WebResult.class);

        if (webResult != null) {
            resultName = webResult.name().length() > 0 && webResult.partName().length() > 0 ? webResult
                .partName() : resultName;
            resultName = webResult.name().length() > 0 ? webResult.name() : resultName;
            resultName = webResult.partName().length() > 0 ? webResult.partName() : resultName;
            resultTNS = webResult.targetNamespace().length() > 0 ? webResult.targetNamespace() : resultTNS;
            resultPartName = webResult.partName().length() > 0 ? webResult.partName() : resultPartName;

        }

        // get return type class
        Class returnType = method.getReturnType();

        if (returnType != null && !returnType.getName().equals("void")) {

            Annotation[] anns = method.getAnnotations();

            QName responseQN = new QName(resultTNS, resultName);
            TypeReference typeref = new TypeReference(responseQN, returnType, anns);
            JavaParameter jp = new JavaParameter(resultName, typeref, JavaType.Style.OUT);
            jp.setPartName(resultPartName);
            WSDLWrapperParameter resWrapper = new WSDLWrapperParameter();
            resWrapper.setName(method.getName() + "Response");
            resWrapper.addWrapperChild(jp);
            javaMethod.addObjectParameter(resWrapper);
        }

        // processWebparam
        List typeList = processWebPara(method);
        WSDLWrapperParameter reqWrapper = new WSDLWrapperParameter();
        reqWrapper.setName(method.getName() + "Request");
        Iterator ite = typeList.iterator();
        while (ite.hasNext()) {
            TypeReference typeref = (TypeReference)ite.next();
            JavaParameter jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
            reqWrapper.addWrapperChild(jp);
        }
        javaMethod.addObjectParameter(reqWrapper);
        processExceptions(javaMethod, method);
    }

    private JavaParameter getReturnParameter(WebResult webResult, Method method) {
        String resultName = "Return";
        String resultTNS = model.getTargetNameSpace();
        JavaParameter jpara = null;
        QName resultQName = null;
        if (webResult != null) {
            resultName = webResult.name().length() > 0 && webResult.partName().length() > 0 ? webResult
                .partName() : resultName;
            resultName = webResult.name().length() > 0 ? webResult.name() : resultName;
            resultName = webResult.partName().length() > 0 ? webResult.partName() : resultName;
            resultTNS = webResult.targetNamespace().length() > 0 ? webResult.targetNamespace() : resultTNS;
        }
        resultQName = new QName(resultTNS, resultName);
        Class returnType = method.getReturnType();
        if (resultQName != null && !isOneWayMethod(method) && (returnType != null)
            && (!returnType.getName().equals("void"))) {
            Annotation[] annotations = method.getAnnotations();
            if (resultQName.getLocalPart() != null) {
                TypeReference rTypeReference = new TypeReference(resultQName, returnType, annotations);
                jpara = new JavaParameter();
                jpara.setName(method.getName() + "Response");
                jpara.setTypeReference(rTypeReference);
                jpara.setStyle(JavaType.Style.OUT);
            }
        }
        return jpara;
    }

    @SuppressWarnings("unchecked")
    private List<TypeReference> processWebPara(Method method) {
        // processWebparam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] parameterGenTypes = method.getGenericParameterTypes();
        Annotation[][] paraAnns = AnnotationUtil.getPrivParameterAnnotations(method);
        List<TypeReference> typeList = new ArrayList<TypeReference>();
        int i = 0;
        for (Class clazzType : parameterTypes) {
            String paraName = "arg" + i;
            String paraTNS = model.getTargetNameSpace();
            Class clazz = clazzType;
            if (isHoder(clazzType)) {
                clazz = getHoldedClass(clazzType, parameterGenTypes[i]);
            }
            for (Annotation anno : paraAnns[i]) {
                if (anno.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)anno;
                    paraName = webParam.name().length() > 0 && webParam.partName().length() > 0
                        ? paraName = webParam.partName() : paraName;
                    paraName = webParam.name().length() > 0 ? webParam.name() : paraName;
                    paraName = webParam.partName().length() > 0 ? webParam.partName() : paraName;
                    paraTNS = webParam.targetNamespace().length() > 0
                        ? paraTNS = webParam.targetNamespace() : paraTNS;

                    QName requestQN = new QName(paraTNS, paraName);
                    TypeReference typeref = new TypeReference(requestQN, clazz, paraAnns[i]);
                    typeList.add(typeref);

                }

            }
            i++;
        }

        return typeList;
    }

    @SuppressWarnings("unchecked")
    private void processExceptions(JavaMethod jmethod, Method method) {
        for (Type exception : method.getGenericExceptionTypes()) {
            if (RemoteException.class.isAssignableFrom((Class)exception)) {
                continue;
            }
            Annotation[] anns = null;
            Class exClass = (Class)exception;
            String exNameSpace = model.getTargetNameSpace();
            String exName = ((Class)exception).getSimpleName();
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
                WebFault wf = (WebFault)exClass.getAnnotation(WebFault.class);
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
            clazz = new AnnotationUtil().loadClass(webService.endpointInterface());
            webService = AnnotationUtil.getPrivClassAnnotation(clazz, WebService.class);
            if (webService == null) {
                throw new ToolException("Endpoint Interface :No Webservice ");
            }
        }

        String portTypeName = clazz.getSimpleName() + "PortType";
        model.setPortyTypeName(portTypeName);
        String serviceName = clazz.getSimpleName() + "Service";
        String packageName = "";
        if (clazz.getPackage() != null) {
            packageName = clazz.getPackage().getName();
        }
        model.setPackageName(packageName);

        if (webService.serviceName().length() > 0) {
            serviceName = webService.serviceName();
        }
        model.setServiceName(serviceName);
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

    private boolean isHoder(Class cType) {
        return Holder.class.isAssignableFrom(cType);
        // set the actual type argument of Holder in the TypeReference
    }

    private Class getHoldedClass(Class holderClazz, Type type) {
        Class realClass = Navigator.REFLECTION.erasure(((ParameterizedType)type).getActualTypeArguments()[0]);

        return realClass;
    }

    private boolean isOneWayMethod(Method method) {
        return method.isAnnotationPresent(Oneway.class);
    }

}
