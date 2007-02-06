/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.java2wsdl.processor.internal.jaxws;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebFault;

import com.sun.xml.bind.api.TypeReference;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaType;
import org.apache.cxf.tools.common.model.WSDLException;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.common.model.WSDLParameter;

import org.apache.cxf.tools.java2wsdl.processor.JavaToWSDLProcessor;

import org.apache.cxf.tools.util.AnnotationUtil;

public class DocBareMethodProcessor {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    private WSDLModel model;

    public DocBareMethodProcessor(WSDLModel wmodel) {
        model = wmodel;
    }

    public void processDocBare(JavaMethod javaMethod, Method method) {
        if (model.getStyle() != SOAPBinding.Style.DOCUMENT) {
            model.setStyle(SOAPBinding.Style.DOCUMENT);
        }
        boolean isHolder = false;
        javaMethod.setSoapStyle(SOAPBinding.Style.DOCUMENT);
        javaMethod.setWrapperStyle(false);
        setMethodUse(javaMethod, method);

        // process webresult annotation
        String resultName = method.getName() + "Response";
        String resultTNS = model.getTargetNameSpace();
        String resultPartName = null;

        WebResult webResult = method.getAnnotation(WebResult.class);
        boolean webResultHeader = false;
        if (webResult != null) {
            resultName = webResult.name().length() > 0 ? webResult.name() : resultName;
            webResultHeader = webResult.header();
            resultTNS = webResult.targetNamespace().length() > 0 ? webResult.targetNamespace() : resultTNS;
            resultPartName = webResult.partName().length() > 0 ? webResult.partName() : resultName;
        } else {
            //Default name is return
            resultPartName = "return";
        }

        // get return type class

        WSDLParameter response = new WSDLParameter();
        // process return
        Class returnType = method.getReturnType();
        if (returnType != null && !"void".equals(returnType.getName())) {
            QName resQN = new QName(resultTNS, resultName);
            TypeReference typeRef = new TypeReference(resQN, returnType, new Annotation[0]);
            response.setName(method.getName() + "Response");
            response.setStyle(JavaType.Style.OUT);
            response.setTargetNamespace(resultTNS);
            JavaParameter jp = new JavaParameter(resultName, typeRef, JavaType.Style.OUT);
            jp.setPartName(resultPartName);
            jp.setTargetNamespace(resultTNS);
            jp.setHeader(webResultHeader);
            response.addChildren(jp);
            javaMethod.addResponse(response);
        }

        // processWebparam
        WSDLParameter request = new WSDLParameter();
        request.setName(method.getName());
        request.setStyle(JavaType.Style.IN);
        javaMethod.addRequest(request);
        List<JavaParameter> paras = processWebPara(method);
        for (JavaParameter jp : paras) {
            // requestWrapper.addWrapperChild(jp);
            if (jp.getStyle() == JavaType.Style.IN) {
                request.addChildren(jp);
            }
            if (jp.getStyle() == JavaType.Style.INOUT) {
                request.addChildren(jp);
                response.addChildren(jp);
                isHolder = true;
            }
            if (jp.getStyle() == JavaType.Style.OUT) {
                isHolder = true;
                response.addChildren(jp);
            }
        }

        if ((returnType == null || "void".equals(returnType.getName())) && isHolder) {
            response.setName(method.getName() + "Response");
            response.setStyle(JavaType.Style.OUT);
            response.setTargetNamespace(resultTNS);
            javaMethod.addResponse(response);
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
        // DocumentBare_criteria defined jaxws spec 3.6.2.2
        // criteria 1 - it must have at most one in or in/out non_header
        boolean criteria1 = false;
        // criteria 2 - if it has a return type other than void
        // it must have no in/out or out non-header parameters
        boolean criteria2 = true;
        // criteria 3 - if it has a return type of void
        // it must have at most one in/out or out-header parameter
        boolean criteria3 = false;
        // for doc_bare criteria3
        int nonHeaderParamCount = 0;

        int i = 0;
        for (Class clazzType : parameterTypes) {
            String paraName = "arg" + i;            
            String paraTNS = model.getTargetNameSpace();
            Class clazz = clazzType;
            boolean holder = isHolder(clazzType);
            if (holder) {
                clazz = getHoldedClass(clazzType, parameterGenTypes[i]);
            }
            JavaParameter jp = null; 
            for (Annotation anno : paraAnns[i]) {
                if (anno.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)anno;

                    if (!webParam.header()
                        && (webParam.mode() == WebParam.Mode.IN || webParam.mode() == WebParam.Mode.INOUT)) {
                        criteria1 = true;
                    }

                    if (!method.getReturnType().getName().equalsIgnoreCase("void") && !webParam.header()
                        && (webParam.mode() == WebParam.Mode.OUT || webParam.mode() == WebParam.Mode.INOUT)) {
                        criteria2 = false;

                    }

                    if (method.getReturnType().getName().equalsIgnoreCase("void") && !webParam.header()
                        && (webParam.mode() == WebParam.Mode.OUT || webParam.mode() == WebParam.Mode.INOUT)) {
                        nonHeaderParamCount++;
                    }

                    paraName = webParam.name().length() > 0 ? webParam.name() : paraName;
                    String partName = webParam.partName().length() > 0 ? webParam.partName() : paraName;
                    paraTNS = webParam.targetNamespace().length() > 0
                        ? webParam.targetNamespace() : paraTNS;
                    TypeReference typeref = new TypeReference(new QName(paraTNS, method.getName() + i), 
                                                              clazz, paraAnns[i]);
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
                    jp.setPartName(partName);
                    jp.setHeader(webParam.header());
                    jp.setTargetNamespace(paraTNS);
                
                }
            }
            
            if (paraAnns[i].length == 0) {
                TypeReference typeref = new TypeReference(new QName(paraTNS, method.getName() + i), clazz, 
                                                          paraAnns[i]);             
                jp = new JavaParameter(typeref.tagName.getLocalPart(), typeref, JavaType.Style.IN);
                jp.setPartName(paraName);
                jp.setTargetNamespace(paraTNS);             
                criteria1 = true;
                criteria3 = true;
            }   
            
            if (!criteria1) {
                throw new ToolException(new Message("DOC_BARE_METHOD_CRITERIA1", LOG));
            }
            if (!criteria2) {
                throw new ToolException(new Message("DOC_BARE_METHOD_CRITERIA2", LOG));
            }
            criteria3 = nonHeaderParamCount <= 1 ? true : false;
            if (!criteria3) {
                throw new ToolException(new Message("DOC_BARE_METHOD_CRITERIA3", LOG));
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

}
