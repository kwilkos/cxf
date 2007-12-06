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

package org.apache.cxf.jaxws.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;

public class ASMHelper {
    protected static final Map<Class<?>, String> PRIMITIVE_MAP = new HashMap<Class<?>, String>();
    protected static final Map<Class<?>, String> NONPRIMITIVE_MAP = new HashMap<Class<?>, String>();
    
    protected static boolean oldASM;
    
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
    
    protected static String getMethodSignature(Method m) {
        StringBuffer buf = new StringBuffer("(");
        for (Class<?> cl : m.getParameterTypes()) {
            buf.append(getClassCode(cl));
        }
        buf.append(")");
        buf.append(getClassCode(m.getReturnType()));
        
        return buf.toString();
    }
    
    protected static String periodToSlashes(String s) {
        char ch[] = s.toCharArray();
        for (int x = 0; x < ch.length; x++) {
            if (ch[x] == '.') {
                ch[x] = '/';
            }
        }
        return new String(ch);
    }
    
    
    public static String getClassCode(Class<?> cl) {
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
    
    
    public ClassWriter createClassWriter() {
        ClassWriter newCw = null;
        if (!oldASM) {
            Class<ClassWriter> cls = ClassWriter.class;
            try {
                // ASM 1.5.x/2.x
                Constructor<ClassWriter> cons = cls.getConstructor(new Class<?>[] {Boolean.TYPE});
                
                try {
                    // got constructor, now check if it's 1.x which is very
                    // different from 2.x and 3.x
                    cls.getMethod("newConstInt", new Class<?>[] {Integer.TYPE});               
                    // newConstInt was removed in 2.x, if we get this far, we're
                    // using 1.5.x,
                    // set to null so we don't attempt to use it.
                    newCw = null;    
                    oldASM = true;
                } catch (Throwable t) {
                    newCw = cons.newInstance(new Object[] {Boolean.TRUE});
                }
                
            } catch (Throwable e) {
                // ASM 3.x
                try {
                    Constructor<ClassWriter> cons = cls.getConstructor(new Class<?>[] {Integer.TYPE});
                    int i = cls.getField("COMPUTE_MAXS").getInt(null);
                    i |= cls.getField("COMPUTE_FRAMES").getInt(null);
                    newCw = cons.newInstance(new Object[] {Integer.valueOf(i)});
                } catch (Throwable e1) {
                    // ignore
                }
                
            }
        }
        return newCw;
    }
    
    
    public Class<?> loadClass(String className, Class clz , byte[] bytes) { 
        TypeHelperClassLoader loader = new TypeHelperClassLoader(clz.getClassLoader());
        return loader.defineClass(className, bytes);
    }
    
    public static class TypeHelperClassLoader extends ClassLoader {
        TypeHelperClassLoader(ClassLoader parent) {
            super(parent);
        }
        public Class<?> defineClass(String name, byte bytes[]) {
            return super.defineClass(name, bytes, 0, bytes.length);
        }
    }
    
}
