package org.objectweb.celtix.tools.processors.java2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.model.WSDLException;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.common.model.WSDLWrapperParameter;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class ClassProcessor {

    Class seiClass;
    WSDLModel model;
    Map<Class, Boolean> webMethodClasses = new HashMap<Class, Boolean>();

    public ClassProcessor(Class clz) {
        seiClass = clz;
    }

    public void process(WSDLModel wmodel) {
        model = wmodel;
        checkWebMethodUse(seiClass);
        populateWSDLInfo(seiClass);

        for (Method method : seiClass.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class) || !isWebMethod(method, seiClass)) {
                continue;
            }
            processMethod(wmodel, method);

        }

    }

    public void processMethod(WSDLModel wmodel, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return;
        }
        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        if (webMethod != null && webMethod.exclude()) {
            return;
        }
        boolean useWeb = false;
        if (useWeb && webMethod == null) {
            return;
        }
        Object implementor = null;

        JavaMethod javaMethod;
        Class implementorClass = (implementor != null) ? implementor.getClass() : seiClass;
        if (method.getDeclaringClass().equals(implementorClass)) {
            javaMethod = new JavaMethod();
            javaMethod.setName(method.getName());
        } else {
            try {
                Method tmp = implementorClass.getMethod(method.getName(), method.getParameterTypes());
                javaMethod = new JavaMethod();
                javaMethod.setName(tmp.getName());
            } catch (NoSuchMethodException e) {
                throw new ToolException(e.getMessage(), e);
            }
        }
        javaMethod.setStyle(OperationType.ONE_WAY);
        String operationName = method.getName();
        if (webMethod != null) {
            // String action = webMethod.action();
            operationName = webMethod.operationName().length() > 0
                ? webMethod.operationName() : operationName;
        }
        javaMethod.setName(operationName);
        SOAPBinding methodBinding = method.getAnnotation(SOAPBinding.class);
        if (methodBinding == null && !method.getDeclaringClass().equals(seiClass)
            && !method.getDeclaringClass().isInterface()) {
            methodBinding = method.getDeclaringClass().getAnnotation(SOAPBinding.class);
        }
        boolean isWrapped = true;
        SOAPBinding soapBinding = AnnotationUtil.getPrivClassAnnotation(seiClass, SOAPBinding.class);
        if (soapBinding == null || (soapBinding.style().equals(SOAPBinding.Style.DOCUMENT) && isWrapped)) {
            processDocWrappedMethod(method);
        }
    }

    private WSDLWrapperParameter processRequestWrapper(JavaMethod jmethod, Method method,
                                                       RequestWrapper reqWrapper) {
        String reqClassName = "";
        String reqName = method.getName();
        String reqNS = model.getTargetNameSpace();
        if (reqWrapper != null) {
            if (reqWrapper.className().length() > 0) {
                reqClassName = reqWrapper.className();
            }
            if (reqWrapper.localName().length() > 0) {
                reqName = reqWrapper.localName();
            }
            if (reqWrapper.targetNamespace().length() > 0) {
                reqNS = reqWrapper.targetNamespace();
            }
        } else {
            reqClassName = model.getPackageName() + AnnotationUtil.capitalize(method.getName());
        }
        AnnotationUtil util = new AnnotationUtil();
        Class reqClass = null;
        try {
            reqClass = util.loadClass(reqClassName);
        } catch (Exception e) {
            throw new ToolException("Can Not Load class " + reqClass.getName(), e);
        }
        QName reqElement = new QName(reqNS, reqName);
        TypeReference typeRef = new TypeReference(reqElement, reqClass, new Annotation[0]);
        WSDLWrapperParameter reqWrapperPara = new WSDLWrapperParameter();
        reqWrapperPara.setName(reqName);
        reqWrapperPara.setTypeReference(typeRef);
        reqWrapperPara.setStyle(JavaType.Style.IN);
        try {
            jmethod.addObjectParameter(reqWrapperPara);
        } catch (ToolException e1) {
            throw new ToolException(e1);
        }
        return reqWrapperPara;
    }

    private WSDLWrapperParameter processResponseWrapper(JavaMethod jmethod, Method method,
                                                        ResponseWrapper resWrapper) {

        String resClassName = "";
        String resName = method.getName() + "Response";
        String resNS = model.getTargetNameSpace();
        if (resWrapper != null) {
            resClassName = resWrapper.className();
            if (resWrapper.localName().length() > 0) {
                resName = resWrapper.localName();
            }
            if (resWrapper.targetNamespace().length() > 0) {
                resNS = resWrapper.targetNamespace();
            }
        } else {
            resClassName = model.getPackageName() + AnnotationUtil.capitalize(method.getName()) + "Response";
        }
        boolean isOneWay = method.isAnnotationPresent(Oneway.class);
        Class resClass = null;
        QName resElement = new QName(resNS, resName);
        WSDLWrapperParameter resWrapperPara = null;
        AnnotationUtil util = new AnnotationUtil();
        if (!isOneWay) {
            try {
                resClass = util.loadClass(resClassName);
            } catch (Exception e) {
                throw new ToolException("Can Not Load Class " + resClass.getName(), e);
            }
            TypeReference typeRef = new TypeReference(resElement, resClass, new Annotation[0]);
            resWrapperPara = new WSDLWrapperParameter();
            resWrapperPara.setName(resName);
            resWrapperPara.setTypeReference(typeRef);
            resWrapperPara.setStyle(JavaType.Style.OUT);
            jmethod.addObjectParameter(resWrapperPara);
        }
        return resWrapperPara;
    }

    private void processDocWrappedMethod(Method method) {
        JavaMethod jmethod = new JavaMethod();
        jmethod.setName(method.getName());
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        WSDLWrapperParameter reqWrapperPara = processRequestWrapper(jmethod, method, reqWrapper);
        ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
        WSDLWrapperParameter resWrapperPara = processResponseWrapper(jmethod, method, resWrapper);
        String resultName = null;
        String resultTNS = null;
        WebResult webResult = method.getAnnotation(WebResult.class);
        Class returnType = method.getReturnType();
        boolean isResultHeader = false;
        QName resultQName = null;
        if (webResult != null) {
            resultName = webResult.name();
            resultTNS = webResult.targetNamespace();
            isResultHeader = webResult.header();
            if (resultTNS.length() == 0 && webResult.header()) {
                resultTNS = model.getTargetNameSpace();
            }
            resultQName = new QName(resultTNS, resultName);
        }
        // To do: asyn method use
        boolean isOneWay = method.isAnnotationPresent(Oneway.class);
        if (!isOneWay && (returnType != null) && (!returnType.getName().equals("void"))) {
            // Class returnClazz = returnType;
            Annotation[] annotations = method.getAnnotations();
            if (resultQName.getLocalPart() != null) {
                TypeReference rTypeReference = new TypeReference(resultQName, returnType, annotations);
                JavaParameter jpara = new JavaParameter();
                jpara.setName(returnType.getName());
                jpara.setTypeReference(rTypeReference);
                jpara.setStyle(JavaType.Style.OUT);
                if (isResultHeader) {
                    jmethod.addObjectParameter(jpara);
                } else {
                    resWrapperPara.addWrapperChild(jpara);
                }
            }
        }
        processWebPara(jmethod, method, reqWrapperPara, resWrapperPara);
        processExceptions(jmethod, method);
        model.addJavaMethod(jmethod);
    }

    private void processWebPara(JavaMethod jmethod, Method method, WSDLWrapperParameter reqWrapperPara,
                                WSDLWrapperParameter resWrapperPara) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genParaTypes = method.getGenericParameterTypes();
        Annotation[][] paraAnnotations = AnnotationUtil.getPrivParameterAnnotations(method);
        boolean isOneWay = method.isAnnotationPresent(Oneway.class);
        int pos = 0;
        for (Class clazzType : parameterTypes) {
            // Parameter param = null;
            String paramName = "paramater" + pos;

            String paraNamespace = "";
            boolean isHeader = false;

            boolean isHolder = Holder.class.isAssignableFrom(clazzType);
            // TO DO : Holder
           /* if (isHolder && clazzType.getName().equals(Holder.class.getName())) {
                Type type1 = ((ParameterizedType)genParaTypes[i]).getActualTypeArguments()[0];
                // clazzType = type1.getClass();
            }*/

            WebParam.Mode paraMode = isHolder ? WebParam.Mode.INOUT : WebParam.Mode.IN;
            for (Annotation annotation : paraAnnotations[pos]) {
                if (annotation.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)annotation;
                    isHeader = webParam.header();
                    if (webParam.name().length() > 0) {
                        paramName = webParam.name();
                    }
                    if (webParam.targetNamespace().length() > 0) {
                        paraNamespace = webParam.targetNamespace();
                    }
                    if (isHeader) {
                        paraNamespace = model.getTargetNameSpace();
                    }
                    WebParam.Mode mode = webParam.mode();
                    if (isHolder && mode == javax.jws.WebParam.Mode.IN) {
                        mode = javax.jws.WebParam.Mode.INOUT;
                    }
                    break;
                }
            }

            QName paraQName = new QName(paraNamespace, paramName);
            TypeReference typeRef = new TypeReference(paraQName, clazzType, paraAnnotations[pos]);

            JavaParameter japarameter = new JavaParameter();
            japarameter.setName(paramName);
            japarameter.setTypeReference(typeRef);
            if (isHeader) {
                jmethod.addObjectParameter(japarameter);
            } else {
                if (!paraMode.equals(WebParam.Mode.OUT)) {
                    reqWrapperPara.addWrapperChild(japarameter);
                }
                if (!paraMode.equals(WebParam.Mode.IN)) {
                    if (isOneWay) {
                        throw new ToolException("One way operation has no input parameter");
                    }
                    resWrapperPara.addWrapperChild(japarameter);
                }

            }

            pos++;
        }
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

    protected boolean isWebMethod(Method method, Class clazz) {
        if (clazz.isInterface()) {
            return true;
        }
        Class declClass = method.getDeclaringClass();
        WebMethod webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        if (webMethod != null && !webMethod.exclude()) {
            return true;
        }
        if (AnnotationUtil.getPrivClassAnnotation(declClass, WebService.class) != null
            && !webMethodClasses.get(declClass)) {
            return true;
        }
        return false;
    }

    private void checkWebMethodUse(Class clazz) {
        if (clazz == null) {
            return;
        }
        if (clazz.isInterface()) {
            webMethodClasses.put(clazz, false);
        } else {
            WebMethod webMethod;
            boolean isWebMethod = false;
            for (Method method : clazz.getMethods()) {
                if (!method.getDeclaringClass().equals(clazz)) {
                    continue;
                }
                webMethod = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
                if (webMethod != null && !webMethod.exclude()) {
                    isWebMethod = true;
                    break;
                }
            }
            webMethodClasses.put(clazz, isWebMethod);
        }
        checkWebMethodUse(clazz.getSuperclass());
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
            throw new ToolException("Class No Package");
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
