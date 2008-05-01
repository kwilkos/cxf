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
package org.apache.cxf.jaxws.interceptors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBElement;

import org.apache.cxf.common.util.WeakIdentityHashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class WrapperHelperCompiler {
    private static final Map<Class<?>, TypeHelperClassLoader> LOADER_MAP 
        = new WeakIdentityHashMap<Class<?>, TypeHelperClassLoader>();
    private static final Map<Class<?>, String> PRIMITIVE_MAP = new HashMap<Class<?>, String>();
    private static final Map<Class<?>, String> NONPRIMITIVE_MAP = new HashMap<Class<?>, String>();
    
    private static boolean oldASM;
    
    static {
        PRIMITIVE_MAP.put(Byte.TYPE, "B");
        PRIMITIVE_MAP.put(Boolean.TYPE, "Z");
        PRIMITIVE_MAP.put(Long.TYPE, "J");
        PRIMITIVE_MAP.put(Integer.TYPE, "I");
        PRIMITIVE_MAP.put(Short.TYPE, "S");
        PRIMITIVE_MAP.put(Character.TYPE, "C");
        PRIMITIVE_MAP.put(Float.TYPE, "F");
        PRIMITIVE_MAP.put(Double.TYPE, "D");

        NONPRIMITIVE_MAP.put(Byte.TYPE, Byte.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Boolean.TYPE, Boolean.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Long.TYPE, Long.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Integer.TYPE, Integer.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Short.TYPE, Short.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Character.TYPE, Character.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Float.TYPE, Float.class.getName().replaceAll("\\.", "/"));
        NONPRIMITIVE_MAP.put(Double.TYPE, Double.class.getName().replaceAll("\\.", "/"));
    }    
    
    final Class<?> wrapperType;
    final Method setMethods[];
    final Method getMethods[];
    final Method jaxbMethods[];
    final Field fields[];
    final Object objectFactory;
    final ClassWriter cw;
    
    private WrapperHelperCompiler(Class<?> wrapperType,
                                  Method setMethods[],
                                  Method getMethods[],
                                  Method jaxbMethods[],
                                  Field fields[],
                                  Object objectFactory) {
        this.wrapperType = wrapperType;
        this.setMethods = setMethods;
        this.getMethods = getMethods;
        this.jaxbMethods = jaxbMethods;
        this.fields = fields;
        this.objectFactory = objectFactory;
        

        ClassWriter newCw = null;
        if (!oldASM) {
            Class<ClassWriter> cls = ClassWriter.class;
            try {
                //ASM 1.5.x/2.x
                Constructor<ClassWriter> cons = cls.getConstructor(new Class<?>[] {Boolean.TYPE});
                
                try {
                    //got constructor, now check if it's 1.x which is very different from 2.x and 3.x 
                    cls.getMethod("newConstInt", new Class<?>[] {Integer.TYPE});               
                    //newConstInt was removed in 2.x, if we get this far, we're using 1.5.x,
                    //set to null so we don't attempt to use it.
                    newCw = null;    
                    oldASM = true;
                } catch (Throwable t) {
                    newCw = cons.newInstance(new Object[] {Boolean.TRUE});
                }
                
            } catch (Throwable e) {
                //ASM 3.x
                try {
                    Constructor<ClassWriter> cons = cls.getConstructor(new Class<?>[] {Integer.TYPE});
                    int i = cls.getField("COMPUTE_MAXS").getInt(null);
                    i |= cls.getField("COMPUTE_FRAMES").getInt(null);
                    newCw = cons.newInstance(new Object[] {Integer.valueOf(i)});
                } catch (Throwable e1) {
                    //ignore
                }
                
            }
        }
        cw = newCw;
    }

    static WrapperHelper compileWrapperHelper(Class<?> wrapperType,
                                              Method setMethods[],
                                              Method getMethods[],
                                              Method jaxbMethods[],
                                              Field fields[],
                                              Object objectFactory) {
        try {
            return new WrapperHelperCompiler(wrapperType,
                                        setMethods,
                                        getMethods,
                                        jaxbMethods,
                                        fields,
                                        objectFactory).compile();
        } catch (Throwable t) {
            //Some error - probably a bad version of ASM or similar
            return null;
        }
    }

    public WrapperHelper compile() {
        if (cw == null) {
            return null;
        }
        int count = 1;
        String newClassName = wrapperType.getName() + "_WrapperTypeHelper" + count;
        newClassName = newClassName.replaceAll("\\$", ".");
        newClassName = periodToSlashes(newClassName);

        Class<?> cls = findClass(newClassName.replace('/', '.'), wrapperType);
        while (cls != null) {
            try {
                WrapperHelper helper = WrapperHelper.class.cast(cls.newInstance());
                if (!helper.getSignature().equals(computeSignature())) {
                    count++;
                    newClassName = wrapperType.getName() + "_WrapperTypeHelper" + count;
                    newClassName = newClassName.replaceAll("\\$", ".");
                    newClassName = periodToSlashes(newClassName);
                    cls = findClass(newClassName.replace('/', '.'), wrapperType);
                } else {
                    return helper;
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        
        
        cw.visit(Opcodes.V1_5,
                 Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                 newClassName,
                 null,
                 periodToSlashes(WrapperHelper.class.getName()),
                 null);
        
        addConstructor(newClassName, cw, objectFactory == null ? null : objectFactory.getClass());
        boolean b = addSignature();
        if (b) {
            addCreateWrapperObject(newClassName,
                                   objectFactory == null ? null : objectFactory.getClass());
        }
        if (b) {
            b = addGetWrapperParts(newClassName, wrapperType,
                           getMethods, fields, cw);
        }
                                                                          
        try {
            if (b) {
                cw.visitEnd();
                byte bt[] = cw.toByteArray();
                Class<?> cl = new TypeHelperClassLoader(wrapperType.getClassLoader())
                    .defineClass(newClassName.replaceAll("/", "."), bt);
                                 
                Object o = cl.newInstance();
                return WrapperHelper.class.cast(o);
            }
        } catch (Throwable e) {
            //ignore, we'll just fall down to reflection based
        }
        return null;
    }
    

    private String computeSignature() {
        StringBuilder b = new StringBuilder();
        b.append(setMethods.length).append(':');
        for (int x = 0; x < setMethods.length; x++) {
            if (getMethods[x] == null) {
                b.append("null,");
            } else {
                b.append(getMethods[x].getName()).append('/');
                b.append(getMethods[x].getReturnType().getName()).append(',');                
            }
        }
        return b.toString();
    }
    
    private boolean addSignature() {
        String sig = computeSignature();
        if (sig == null) {
            return false;
        }
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "getSignature", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLdcInsn(sig);
        mv.visitInsn(Opcodes.ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return true;
    }

    private static void addConstructor(String newClassName, ClassWriter cw,  Class<?> objectFactory) {
        
        if (objectFactory != null) {
            String ofName = "L" + periodToSlashes(objectFactory.getName()) + ";";
            FieldVisitor fv = cw.visitField(0, "factory",
                                            ofName,
                                            null, null);
            fv.visitEnd();
        }
        
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                           periodToSlashes(WrapperHelper.class.getName()),
                           "<init>",
                           "()V");
        if (objectFactory != null) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.NEW, periodToSlashes(objectFactory.getName()));
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                               periodToSlashes(objectFactory.getName()),
                               "<init>", "()V");
            mv.visitFieldInsn(Opcodes.PUTFIELD, periodToSlashes(newClassName),
                              "factory", "L" + periodToSlashes(objectFactory.getName()) + ";");
        } 
        
        mv.visitInsn(Opcodes.RETURN);
    
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "L" + newClassName + ";", null, l0, l1, 0);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private boolean addCreateWrapperObject(String newClassName,
                                           Class<?> objectFactoryClass) {
        
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "createWrapperObject",
                                          "(Ljava/util/List;)Ljava/lang/Object;",
                                          "(Ljava/util/List<*>;)Ljava/lang/Object;",
                                          new String[] {
                                              "org/apache/cxf/interceptor/Fault"
                                          });
        mv.visitCode();
        Label lBegin = new Label();
        mv.visitLabel(lBegin);
        
        mv.visitTypeInsn(Opcodes.NEW, periodToSlashes(wrapperType.getName()));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, periodToSlashes(wrapperType.getName()),
                           "<init>", "()V");
        mv.visitVarInsn(Opcodes.ASTORE, 2);
    
        for (int x = 0; x < setMethods.length; x++) {
            if (getMethods[x] == null) { 
                if (setMethods[x] == null
                    && fields[x] == null) {
                    //null placeholder, just skip it
                    continue;
                } else {
                    return false;
                }
            }
            Class<?> tp = getMethods[x].getReturnType();
            mv.visitVarInsn(Opcodes.ALOAD, 2);            
            
            if (List.class.isAssignableFrom(tp)) {
                doCollection(mv, x);
            } else { 
                if (JAXBElement.class.isAssignableFrom(tp)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, periodToSlashes(newClassName),
                                      "factory",
                                      "L" + periodToSlashes(objectFactoryClass.getName()) + ";");
                }
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitIntInsn(Opcodes.BIPUSH, x);
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");
                
                if (tp.isPrimitive()) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, NONPRIMITIVE_MAP.get(tp));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, NONPRIMITIVE_MAP.get(tp), 
                                       tp.getName() + "Value", "()" + PRIMITIVE_MAP.get(tp));
                } else if (JAXBElement.class.isAssignableFrom(tp)) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST,
                                     periodToSlashes(jaxbMethods[x].getParameterTypes()[0].getName()));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, periodToSlashes(objectFactoryClass.getName()),
                                       jaxbMethods[x].getName(),
                                       getMethodSignature(jaxbMethods[x]));
                } else if (tp.isArray()) { 
                    mv.visitTypeInsn(Opcodes.CHECKCAST, getClassCode(tp));
                } else {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, periodToSlashes(tp.getName()));
                }
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                   periodToSlashes(wrapperType.getName()),
                                   setMethods[x].getName(), "(" + getClassCode(tp) + ")V");
            }
        }
        
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitInsn(Opcodes.ARETURN);
    
        Label lEnd = new Label();
        mv.visitLabel(lEnd);
        mv.visitLocalVariable("this", "L" + newClassName + ";", null, lBegin, lEnd, 0);
        mv.visitLocalVariable("lst", "Ljava/util/List;", "Ljava/util/List<*>;", lBegin, lEnd, 1);
        mv.visitLocalVariable("ok", "L" + periodToSlashes(wrapperType.getName()) + ";",
                              null, lBegin, lEnd, 2);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return true;
    }
    
    private void doCollection(MethodVisitor mv, int x) {
        //List aVal = obj.getA();
        //List newA = (List)lst.get(99);
        //if (aVal == null) {
        //    obj.setA(newA);
        //} else {
        //    aVal.addAll(newA);
        //}
        
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                           periodToSlashes(wrapperType.getName()),
                           getMethods[x].getName(),
                           getMethodSignature(getMethods[x]));
        mv.visitVarInsn(Opcodes.ASTORE, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitIntInsn(Opcodes.BIPUSH, x);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
                           "get", "(I)Ljava/lang/Object;");
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/util/List");
        mv.visitVarInsn(Opcodes.ASTORE, 4);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        Label nonNullLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, nonNullLabel);

        if (setMethods[x] == null) {
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(getMethods[x].getName() + " returned null and there isn't a set method.");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                               "java/lang/RuntimeException",
                               "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(Opcodes.ATHROW);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitTypeInsn(Opcodes.CHECKCAST,
                             getMethods[x].getReturnType().getName().replace('.', '/'));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                               periodToSlashes(wrapperType.getName()),
                               setMethods[x].getName(),
                               getMethodSignature(setMethods[x]));
        }
        Label jumpOverLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, jumpOverLabel);
        mv.visitLabel(nonNullLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                           "java/util/List", "addAll", "(Ljava/util/Collection;)Z");
        mv.visitInsn(Opcodes.POP);
        mv.visitLabel(jumpOverLabel);

    }
    
    private static boolean addGetWrapperParts(String newClassName,
                                           Class<?> wrapperClass,
                                           Method getMethods[],
                                           Field fields[],
                                           ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "getWrapperParts",
                                          "(Ljava/lang/Object;)Ljava/util/List;",
                                          "(Ljava/lang/Object;)Ljava/util/List<Ljava/lang/Object;>;", 
                                          new String[] {
                                              "org/apache/cxf/interceptor/Fault" 
                                          });
        mv.visitCode();
        Label lBegin = new Label();
        mv.visitLabel(lBegin);
               
        //the ret List
        mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
        mv.visitVarInsn(Opcodes.ASTORE, 2);
        
        // cast the Object to the wrapperType type
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, periodToSlashes(wrapperClass.getName()));
        mv.visitVarInsn(Opcodes.ASTORE, 3);
        
        for (int x = 0; x < getMethods.length; x++) {
            Method method = getMethods[x];
            if (method == null && fields[x] != null) {
                //fallback to reflection mode
                return false;
            }
            
            if (method == null) {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
                                   "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(Opcodes.POP);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                                   periodToSlashes(wrapperClass.getName()), 
                                   method.getName(), 
                                   getMethodSignature(method));
                if (method.getReturnType().isPrimitive()) {
                    //wrap into Object type
                    createObjectWrapper(mv, method.getReturnType());
                }
                if (JAXBElement.class.isAssignableFrom(method.getReturnType())) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                       "javax/xml/bind/JAXBElement",
                                       "getValue", "()Ljava/lang/Object;");
                }
                
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(Opcodes.POP);
            }
        }
        
        //return the list
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitInsn(Opcodes.ARETURN);
        
        
        Label lEnd = new Label();
        mv.visitLabel(lEnd);
        mv.visitLocalVariable("this", "L" + newClassName + ";", null, lBegin, lEnd, 0);
        mv.visitLocalVariable("o", "Ljava/lang/Object;", null, lBegin, lEnd, 1);
        mv.visitLocalVariable("ret", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Object;>;",
                              lBegin, lEnd, 2);
        mv.visitLocalVariable("ok", "L" + periodToSlashes(wrapperClass.getName()) + ";",
                              null, lBegin, lEnd, 3);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return true;
    }
    
    
    private static String getMethodSignature(Method m) {
        StringBuffer buf = new StringBuffer("(");
        for (Class<?> cl : m.getParameterTypes()) {
            buf.append(getClassCode(cl));
        }
        buf.append(")");
        buf.append(getClassCode(m.getReturnType()));
        
        return buf.toString();
    }
    private static String getClassCode(Class<?> cl) {
        if (cl == Void.TYPE) {
            return "V";
        }
        if (cl.isPrimitive()) {
            return PRIMITIVE_MAP.get(cl);
        }
        if (cl.isArray()) {
            return "[" + getClassCode(cl.getComponentType());
        }
        return "L" + periodToSlashes(cl.getName()) + ";";
    }
    private static void createObjectWrapper(MethodVisitor mv, Class<?> cl) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, NONPRIMITIVE_MAP.get(cl),
                           "valueOf", "(" + PRIMITIVE_MAP.get(cl) + ")L" 
                           + NONPRIMITIVE_MAP.get(cl) + ";");
    }
    
    private static String periodToSlashes(String s) {
        char ch[] = s.toCharArray();
        for (int x = 0; x < ch.length; x++) {
            if (ch[x] == '.') {
                ch[x] = '/';
            }
        }
        return new String(ch);
    }


    public Class<?> loadClass(String className, Class clz , byte[] bytes) { 
        TypeHelperClassLoader loader = getTypeHelperClassLoader(clz);
        return loader.defineClass(className, bytes);
    }
    public Class<?> findClass(String className, Class clz) { 
        TypeHelperClassLoader loader = getTypeHelperClassLoader(clz);
        return loader.lookupDefinedClass(className);
    }

    private static synchronized TypeHelperClassLoader getTypeHelperClassLoader(Class<?> l) {
        TypeHelperClassLoader ret = LOADER_MAP.get(l);
        if (ret == null) {
            ret = new TypeHelperClassLoader(l.getClassLoader());
            LOADER_MAP.put(l, ret);
        }
        return ret;
    }

    public static class TypeHelperClassLoader extends ClassLoader {
        Map<String, Class<?>> defined = new ConcurrentHashMap<String, Class<?>>();

        TypeHelperClassLoader(ClassLoader parent) {
            super(parent);
        }
        public Class<?> lookupDefinedClass(String name) {
            return defined.get(name);
        }

        public Class<?> defineClass(String name, byte bytes[]) {
            if (name.endsWith("package-info")) {
                Package p = super.getPackage(name.substring(0, name.length() - 13));
                if (p == null) {
                    definePackage(name.substring(0, name.length() - 13).replace('/', '.'),
                                    null,
                                    null,
                                    null, 
                                    null,
                                    null,
                                    null,
                                    null);
                }
            }

            Class<?> ret = super.defineClass(name.replace('/', '.'), bytes, 0, bytes.length);
            defined.put(name, ret);
            return ret;
        }
    }

}
