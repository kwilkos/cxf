package org.objectweb.celtix.tools.processors.java2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.model.WSDLException;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.common.model.WSDLParameter;

import org.objectweb.celtix.tools.processors.java2.JavaToWSDLProcessor;

import org.objectweb.celtix.tools.utils.AnnotationUtil;


public class DocWrapperMethodProcessor {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    private WSDLModel model;

    public DocWrapperMethodProcessor(WSDLModel wmodel) {
        model = wmodel;
    }

    public void process(JavaMethod javaMethod, Method method) {
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(true);
        setMethodUse(javaMethod, method);

        // process request
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        String reqClassName = "";
        String reqName = method.getName();
        String reqNS = model.getTargetNameSpace();
        if (reqWrapper != null && !reqWrapper.className().equals("")) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper.className() : reqClassName;
            reqName = reqWrapper.localName().length() > 0 ? reqWrapper.localName() : reqName;
            reqNS = reqWrapper.targetNamespace().length() > 0 ? reqWrapper.targetNamespace() : reqNS;
        } else {
            reqClassName = model.getPackageName() + ".jaxws." + AnnotationUtil.capitalize(method.getName());
        }

        Class reqClass = null;
        try {
            reqClass = AnnotationUtil.loadClass(reqClassName, this.getClass().getClassLoader());
        } catch (Exception e) {
            Message msg = new Message("LOAD_CLASS_ERROR", LOG, reqClassName);
            throw new ToolException(msg, e);
        }
        QName reqQN = new QName(reqNS, reqName);
        TypeReference typeRef = new TypeReference(reqQN, reqClass, new Annotation[0]);
        WSDLParameter request = new WSDLParameter(reqName, typeRef, JavaType.Style.IN);
        request.setTargetNamespace(reqNS);
        javaMethod.addRequest(request);


        if (!isOneWayMethod(method)) {
            // process response
            ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
            String resClassName = "";
            // rule 3.5 suffix -"Response"
            String resName = method.getName() + "Response";
            String resNS = model.getTargetNameSpace();
            if (reqWrapper != null && !reqWrapper.className().equals("")) {
                resClassName = resWrapper.className();
                resName = resWrapper.localName().length() > 0 ? resWrapper.localName() : resName;
                resNS = resWrapper.targetNamespace().length() > 0 ? resWrapper.targetNamespace() : resNS;
            } else {
                resClassName = model.getPackageName() + ".jaxws."
                    + AnnotationUtil.capitalize(method.getName())
                               + "Response";
            }
            Class resClass = null;
            QName resQN = new QName(resNS, resName);
            try {
                resClass = AnnotationUtil
                    .loadClass(resClassName, method.getDeclaringClass().getClassLoader());
            } catch (Exception e) {
                Message msg = new Message("LOAD_CLASS_ERROR", LOG, resClassName);
                throw new ToolException(msg, e);
            }
            typeRef = new TypeReference(resQN, resClass, new Annotation[0]);
            WSDLParameter response = new WSDLParameter(resName, typeRef, JavaType.Style.OUT);
            response.setTargetNamespace(resNS);
            javaMethod.addResponse(response);
            WebResult webResult = method.getAnnotation(WebResult.class);
            JavaParameter returnParameter = getReturnParameter(webResult, method);
            if (returnParameter != null) {
                response.addChildren(returnParameter);
            }
        }
        List<JavaParameter> paras = processWebPara(method);
        for (JavaParameter jp : paras) {
            if (jp.getStyle() == JavaType.Style.IN) {
                request.addChildren(jp);
            } else {
                request.addChildren(jp);
            }
        }

        processExceptions(javaMethod, method);
    }

    private void setMethodUse(JavaMethod javaMethod, Method method) {
        SOAPBinding binding = method.getAnnotation(SOAPBinding.class);
        if (binding != null) {
            javaMethod.setSoapUse(binding.use());
        } else {
            javaMethod.setSoapUse(this.model.getUse());
        }
    }

    private List<JavaParameter> processWebPara(Method method) {
        // processWebparam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] parameterGenTypes = method.getGenericParameterTypes();
        Annotation[][] paraAnns = AnnotationUtil.getPrivParameterAnnotations(method);
        List<JavaParameter> paras = new ArrayList<JavaParameter>();
        int i = 0;
        JavaParameter jp = null;
        for (Class clazzType : parameterTypes) {
            String paraName = method.getName();
            String partName = "arg" + i;
            String paraTNS = model.getTargetNameSpace();
            Class clazz = clazzType;
            boolean holder = isHolder(clazzType);
            if (holder) {
                clazz = getHoldedClass(clazzType, parameterGenTypes[i]);
            }
            for (Annotation anno : paraAnns[i]) {
                if (anno.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)anno;
                    paraName = webParam.name().length() > 0 ? webParam.name() : paraName;
                    partName = webParam.partName().length() > 0 ? webParam.partName() : paraName;
                    paraTNS = webParam.targetNamespace().length() > 0
                        ? webParam.targetNamespace() : paraTNS;

                    QName requestQN = new QName(paraTNS, paraName);
                    TypeReference typeref = new TypeReference(requestQN, clazz, paraAnns[i]);

                    if (holder) {
                        if (webParam.mode() == WebParam.Mode.INOUT) {
                            jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref,
                                                   JavaType.Style.INOUT);
                        } else {
                            jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref,
                                                   JavaType.Style.OUT);
                        }
                    } else {
                        jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
                    }
                    jp.setName(paraName);
                    jp.setPartName(partName);
                    jp.setHeader(webParam.header());
                    jp.setTargetNamespace(paraTNS);
                }
            }
            if (paraAnns[i].length == 0) {
                TypeReference typeref = new TypeReference(new QName(paraTNS, paraName), clazz,
                                                          paraAnns[i]);
                jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
                jp.setPartName(partName);
                jp.setTargetNamespace(paraTNS);

            }
            paras.add(jp);
            i++;
        }

        return paras;
    }

    private void processExceptions(JavaMethod jmethod, Method method) {
        for (Type exception : method.getGenericExceptionTypes()) {
            if (RemoteException.class.isAssignableFrom((Class)exception)) {
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

            jmethod.addWSDLException(wsdlEx);

        }
    }

    private boolean isHolder(Class cType) {
        return Holder.class.isAssignableFrom(cType);
        // set the actual type argument of Holder in the TypeReference
    }

    private Class getHoldedClass(Class holderClazz, Type type) {
        ParameterizedType pt = (ParameterizedType)type;
        return getClass(pt.getActualTypeArguments()[0]);
    }

    private Class getClass(Type type) {
        if (type instanceof Class) {
            return (Class)type;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)type;
            Class compType = getClass(gt.getGenericComponentType());
            return java.lang.reflect.Array.newInstance(compType, 0).getClass();
        }
        return Object.class;
    }

    private boolean isOneWayMethod(Method method) {
        return method.isAnnotationPresent(Oneway.class);
    }

    private JavaParameter getReturnParameter(WebResult webResult, Method method) {
        boolean isHeader = false;
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
            isHeader = webResult.header();
        }
        resultQName = new QName(resultTNS, resultName);
        Class returnType = method.getReturnType();
        if (resultQName != null && !isOneWayMethod(method) && (returnType != null)
            && (!"void".equals(returnType.getName()))) {
            // Annotation[] annotations = method.getAnnotations();
            Annotation[] annotations = new Annotation[0];
            if (resultQName.getLocalPart() != null) {
                TypeReference rTypeReference = new TypeReference(resultQName, returnType, annotations);
                jpara = new JavaParameter();
                jpara.setName(method.getName() + "Response");
                jpara.setTypeReference(rTypeReference);
                jpara.setStyle(JavaType.Style.OUT);
                jpara.setHeader(isHeader);
            }
        }
        return jpara;
    }

}
