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

package org.apache.cxf.jaxws;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.JavaUtils;
import org.apache.cxf.jaxws.util.ASMHelper;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.tools.common.ToolConstants;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class WrapperClassGenerator extends ASMHelper {
    private Set<Class<?>> wrapperBeans = new HashSet<Class<?>>();
    private InterfaceInfo interfaceInfo;

    public WrapperClassGenerator(InterfaceInfo inf) {
        interfaceInfo = inf;

    }

    private String getPackageName(Method method) {
        Package pkg = method.getDeclaringClass().getPackage();
        if (pkg == null) {
            return ToolConstants.DEFAULT_PACKAGE_NAME;
        }
        return pkg.getName();

    }

    private Annotation[] getMethodParameterAnnotations(final MessagePartInfo mpi) {
        Annotation[][] paramAnno = (Annotation[][])mpi
            .getProperty(ReflectionServiceFactoryBean.METHOD_PARAM_ANNOTATIONS);
        int index = mpi.getIndex();
        if (paramAnno != null && index < paramAnno.length && index >= 0) {
            return paramAnno[index];
        }
        return null;
    }

    private List<Annotation> getJaxbAnnos(MessagePartInfo mpi) {
        List<Annotation> list = new java.util.concurrent.CopyOnWriteArrayList<Annotation>();
        Annotation[] anns = getMethodParameterAnnotations(mpi);
        if (anns != null) {
            for (Annotation anno : anns) {
                if (anno.annotationType() == XmlList.class || anno.annotationType() == XmlAttachmentRef.class
                    || anno.annotationType() == XmlJavaTypeAdapter.class) {
                    list.add(anno);
                }
            }
        }
        return list;
    }

    public Set<Class<?>> genearte() {
        for (OperationInfo opInfo : interfaceInfo.getOperations()) {
            if (opInfo.isUnwrappedCapable()
                && (opInfo.getUnwrappedOperation()
                    .getProperty(ReflectionServiceFactoryBean.WRAPPERGEN_NEEDED) != null)) {
                Method method = (Method)opInfo.getProperty(ReflectionServiceFactoryBean.METHOD);
                MessageInfo messageInfo = opInfo.getUnwrappedOperation().getInput();
                Class requestWrapperClass = createWrapperClass(messageInfo, method, true);
                opInfo.getInput().getMessageParts().get(0).setTypeClass(requestWrapperClass);
                messageInfo = opInfo.getUnwrappedOperation().getOutput();
                if (messageInfo != null) {
                    Class responseWrapperClass = createWrapperClass(messageInfo, method, false);
                    opInfo.getOutput().getMessageParts().get(0).setTypeClass(responseWrapperClass);
                }


            }
        }
        return wrapperBeans;
    }

    private Class<?> createWrapperClass(MessageInfo messageInfo, Method method, boolean isRequest) {

        QName wrapperElement = messageInfo.getName();

        ClassWriter cw = createClassWriter();
        String className = getPackageName(method) + ".jaxws." + StringUtils.capitalize(method.getName());
        if (!isRequest) {
            className = className + "Response";
        }
        String classFileName = periodToSlashes(className);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classFileName, null,
                 "java/lang/Object", null);

        AnnotationVisitor av0 = cw.visitAnnotation("Ljavax/xml/bind/annotation/XmlRootElement;", true);
        av0.visit("name", wrapperElement.getLocalPart());
        av0.visit("namespace", wrapperElement.getNamespaceURI());
        av0.visitEnd();

        av0 = cw.visitAnnotation("Ljavax/xml/bind/annotation/XmlAccessorType;", true);
        av0.visitEnum("value", "Ljavax/xml/bind/annotation/XmlAccessType;", "FIELD");
        av0.visitEnd();

        av0 = cw.visitAnnotation("Ljavax/xml/bind/annotation/XmlType;", true);
        av0.visit("name", wrapperElement.getLocalPart());
        av0.visit("namespace", wrapperElement.getNamespaceURI());
        av0.visitEnd();

        // add constructor
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label lbegin = new Label();
        mv.visitLabel(lbegin);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        Label lend = new Label();
        mv.visitLabel(lend);
        mv.visitLocalVariable("this", "L" + classFileName + ";", null, lbegin, lend, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        for (MessagePartInfo mpi : messageInfo.getMessageParts()) {
            generateMessagePart(cw, mpi, method, classFileName);
        }

        cw.visitEnd();

        Class<?> clz = loadClass(className, method.getDeclaringClass(), cw.toByteArray());
        messageInfo.getMessagePart(0).setTypeClass(clz);
        wrapperBeans.add(clz);
        return clz;

    }

    private void generateMessagePart(ClassWriter cw, MessagePartInfo mpi, Method method, String className) {
        String classFileName = periodToSlashes(className);
        String name = mpi.getName().getLocalPart();
        Class clz = mpi.getTypeClass();
        Object obj = mpi.getProperty(ReflectionServiceFactoryBean.RAW_CLASS);
        if (obj != null) {
            clz = (Class)obj;
        }
        Class genericTypeClass = null;
        Type genericType = (Type)mpi.getProperty(ReflectionServiceFactoryBean.GENERIC_TYPE);
        if (genericType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType)genericType;

            Type[] types = ptype.getActualTypeArguments();
            // TODO: more complex Parameterized type
            if (types.length > 0 && types[0] instanceof Class) {
                genericTypeClass = (Class)types[0];
            }
        }
        String classCode = getClassCode(clz);

        String filedDescriptor = null;
        if (genericTypeClass != null) {
            filedDescriptor = classCode.substring(0, classCode.lastIndexOf(";")) + "<"
                              + getClassCode(genericTypeClass) + ">;";
        }
        String fieldName = JavaUtils.isJavaKeyword(name) ? JavaUtils.makeNonJavaKeyword(name) : name;
        
        FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName, 
                                        classCode, filedDescriptor, null);
        AnnotationVisitor av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlElement;", true);
        av0.visit("name", name);
        av0.visit("namespace", "");
        av0.visitEnd();

        List<Annotation> jaxbAnnos = getJaxbAnnos(mpi);
        for (Annotation ann : jaxbAnnos) {
            if (ann instanceof XmlMimeType) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlMimeType;", true);
                av0.visit("value", ((XmlMimeType)ann).value());
                av0.visitEnd();
            } else if (ann instanceof XmlJavaTypeAdapter) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/adapters/XmlJavaTypeAdapter;", true);
                XmlJavaTypeAdapter adapter = (XmlJavaTypeAdapter)ann;
                if (adapter.value() != null) {
                    av0.visit("value", org.objectweb.asm.Type.getType(getClassCode(adapter.value())));
                }
                if (adapter.type() != javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT.class) {
                    av0.visit("type", org.objectweb.asm.Type.getType(getClassCode(adapter.type())));
                }
                av0.visitEnd();
            } else if (ann instanceof XmlAttachmentRef) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlAttachmentRef;", true);
                av0.visitEnd();
            } else if (ann instanceof XmlList) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlList;", true);
                av0.visitEnd();
            }

        }

        fv.visitEnd();

        String methodName = StringUtils.capitalize(name);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "get" + methodName, "()" + classCode, null,
                                          null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, classFileName, fieldName, classCode);
        mv.visitInsn(org.objectweb.asm.Type.getType(classCode).getOpcode(Opcodes.IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "set" + methodName, "(" + classCode + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        org.objectweb.asm.Type setType = org.objectweb.asm.Type.getType(classCode);
        mv.visitVarInsn(setType.getOpcode(Opcodes.ILOAD), 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, classCode);       
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }
}
