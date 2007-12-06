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
import java.util.List;

import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.cxf.common.util.StringUtils;
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
    private List<Class> wrapperBeanList = new java.util.concurrent.CopyOnWriteArrayList<Class>();
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
        Annotation[][] paramAnno = 
            (Annotation[][])mpi.getProperty(ReflectionServiceFactoryBean.METHOD_PARAM_ANNOTATIONS);
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
    
    public List<Class> genearte() {
        for (OperationInfo opInfo : interfaceInfo.getOperations()) {
            if (opInfo.isUnwrappedCapable()) {
                Method method = (Method)opInfo.getProperty(ReflectionServiceFactoryBean.METHOD);
                MessageInfo messageInfo = opInfo.getUnwrappedOperation().getInput();
                createWrapperClass(messageInfo, method, true);
                messageInfo = opInfo.getUnwrappedOperation().getOutput();
                if (messageInfo != null) {
                    createWrapperClass(messageInfo, method, false);
                }
                
            }
        }
        return wrapperBeanList;
    }

    private void createWrapperClass(MessageInfo messageInfo, Method method, boolean isRequest) {

        QName wrapperElement = messageInfo.getName();
        
        ClassWriter cw = createClassWriter();
        String className = getPackageName(method) + ".jaxws." + StringUtils.capitalize(method.getName());
        if (!isRequest) {
            className = className + "Response";
        }
        String classFileName = periodToSlashes(className);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                 classFileName, null, "java/lang/Object",
                 null);

        // cw.visitSource("AddNumbers.java", null);

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

        
        
        for (MessagePartInfo mpi : messageInfo.getMessageParts()) {
            generateMessagePart(cw, mpi, method, classFileName);
        }

        cw.visitEnd();
        Class<?> clz = loadClass(className, method.getDeclaringClass(), cw.toByteArray());
        messageInfo.getMessagePart(0).setTypeClass(clz);
        wrapperBeanList.add(clz);

    }
    
    private void generateMessagePart(ClassWriter cw, MessagePartInfo mpi, Method method, String className) {
        String classFileName = periodToSlashes(className);
        final Class[] paramClasses = method.getParameterTypes();
        int idx = mpi.getIndex();
        String name = mpi.getName().getLocalPart();
        Class clz = paramClasses[idx];
        String classCode = getClassCode(clz);
        FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, name, classCode, null, null);
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
                av0.visit("value", ((XmlJavaTypeAdapter)ann).value());
                av0.visit("type", ((XmlJavaTypeAdapter)ann).type());
                av0.visitEnd();
            } else if (ann instanceof XmlAttachmentRef) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/adapters/XmlAttachmentRef;", true);
                av0.visitEnd();
            } else if (ann instanceof XmlList) {
                av0 = fv.visitAnnotation("Ljavax/xml/bind/annotation/adapters/XmlList;", true);
                av0.visitEnd();
            }
        }

        fv.visitEnd();

        String methodName = StringUtils.capitalize(name);
        
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                                          "get" + methodName, "()" + classCode , null, null);
        mv.visitCode();
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, classFileName, name, classCode);
        mv.visitInsn(Opcodes.ARETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("this", classCode, null, l2, l3, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "set" + methodName, "(" + classCode + ")V", null, null);
        mv.visitCode();
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, className, name, classCode);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitInsn(Opcodes.RETURN);
        Label l6 = new Label();
        mv.visitLabel(l6);
    
        mv.visitLocalVariable("this", "L" + classFileName + ";", null, l4, l6, 0);
        mv.visitLocalVariable(name, classCode, null, l4, l6, 1);
        mv.visitMaxs(2, 2);

        mv.visitEnd();
    
    }
    
    
    private static class TypeHelperClassLoader extends ClassLoader {
        TypeHelperClassLoader(ClassLoader parent) {
            super(parent);
        }
        public Class<?> defineClass(String name, byte bytes[]) {
            return super.defineClass(name, bytes, 0, bytes.length);
        }
    }
    
}
