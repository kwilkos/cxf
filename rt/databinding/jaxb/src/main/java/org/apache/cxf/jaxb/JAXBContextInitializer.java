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

package org.apache.cxf.jaxb;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;

/**
 * Walks the service model and sets up the classes for the context.
 */
class JAXBContextInitializer extends ServiceModelVisitor {

    private Set<Class<?>> classes;
    private Set<String> packages;

    public JAXBContextInitializer(ServiceInfo serviceInfo, Set<Class<?>> classes) {
        super(serviceInfo);
        this.classes = classes;
        this.packages = new HashSet<String>();
    }

    @Override
    public void begin(MessagePartInfo part) {
        Class<?> clazz = part.getTypeClass();
        if (clazz == null) {
            return;
        }

        boolean isFromWrapper = part.getMessageInfo().getOperation().isUnwrapped();
        if (isFromWrapper 
            && clazz.isArray() 
            && !Byte.TYPE.equals(clazz.getComponentType())) {
            clazz = clazz.getComponentType();
        }
        
        Type genericType = (Type) part.getProperty("generic.type");
        if (genericType != null) {
            boolean isList = Collection.class.isAssignableFrom(clazz);
            if (isFromWrapper) {
                if (genericType instanceof Class
                    && ((Class)genericType).isArray()) {
                    Class cl2 = (Class)genericType;
                    if (cl2.isArray()
                        && !Byte.TYPE.equals(cl2.getComponentType())) {
                        genericType = cl2.getComponentType();
                    }
                    addType(genericType);                
                } else if (!isList) {
                    addType(genericType);                
                }
            } else {
                addType(genericType);                
            }
            
            if (isList 
                && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                if (pt.getActualTypeArguments().length > 0 
                    && pt.getActualTypeArguments()[0] instanceof Class) {
            
                    Class<? extends Object> arrayCls = 
                        Array.newInstance((Class) pt.getActualTypeArguments()[0], 0).getClass();
                    clazz = arrayCls;
                    part.setTypeClass(clazz);
                    if (isFromWrapper) {
                        addType(clazz.getComponentType());
                    }
                } else if (pt.getActualTypeArguments().length > 0 
                    && pt.getActualTypeArguments()[0] instanceof GenericArrayType) {
                    GenericArrayType gat = (GenericArrayType)pt.getActualTypeArguments()[0];
                    gat.getGenericComponentType();
                    Class<? extends Object> arrayCls = 
                        Array.newInstance((Class) gat.getGenericComponentType(), 0).getClass();
                    clazz = Array.newInstance(arrayCls, 0).getClass();
                    part.setTypeClass(clazz);
                    if (isFromWrapper) {
                        addType(clazz.getComponentType());
                    }                    
                }
            }
            if (isFromWrapper && isList) {
                clazz = null;
            }
        }
        if (clazz != null) {
            addClass(clazz);
        }
    }
    
    private void addType(Type cls) {
        if (cls instanceof Class) {
            addClass((Class)cls);
        } else if (cls instanceof ParameterizedType) {
            for (Type t2 : ((ParameterizedType)cls).getActualTypeArguments()) {
                addType(t2);
            }
        } else if (cls instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)cls;
            Class ct = (Class) gt.getGenericComponentType();
            ct = Array.newInstance(ct, 0).getClass();
            
            addClass(ct);
        }
    }
    

    private  void addClass(Class<?> cls) {
        if (cls.isArray() && cls.getComponentType().isPrimitive()) {
            return;
        }
        if (Exception.class.isAssignableFrom(cls)) {
            for (Field f : cls.getDeclaredFields()) {
                addClass(f.getType());
            }
            addClass(String.class);
        } else {
            cls = JAXBUtils.getValidClass(cls);
            if (null != cls) {
                if (classes.contains(cls)) {
                    return;
                }
                if (cls.isEnum()) {
                    // The object factory stuff doesn't work for enums
                    classes.add(cls);
                }
                classes.add(cls);
                walkReferences(cls);

                String pname = PackageUtils.getPackageName(cls);
                if (!packages.contains(pname)) {
                    packages.add(pname);
                    String name = pname + ".ObjectFactory";
                    try {
                        Class ocls = Class.forName(name, false, cls.getClassLoader());
                        if (!classes.contains(ocls)) {
                            classes.add(ocls);
                        }
                    } catch (ClassNotFoundException ex) {
                        // cannot add factory, just add the class
                    }
                }
            }
        }
    }

    private void walkReferences(Class<?> cls) { 
        if (cls.getName().startsWith("java.") 
            || cls.getName().startsWith("javax.")) { 
            return; 
        } 
        //walk the public fields/methods to try and find all the classes. JAXB will only load the 
        //EXACT classes in the fields/methods if they are in a different package. Thus, 
        //subclasses won't be found and the xsi:type stuff won't work at all. 
        //We'll grab the public field/method types and then add the ObjectFactory stuff 
        //as well as look for jaxb.index files in those packages. 

        Field fields[] = cls.getFields(); 
        for (Field f : fields) { 
            if (f.getAnnotation(XmlTransient.class) == null
                && !Modifier.isStatic(f.getModifiers())) { 
                addType(f.getGenericType()); 
            } 
        } 
        Method methods[] = cls.getMethods(); 
        for (Method m : methods) { 
            if (m.getAnnotation(XmlTransient.class) == null
                && !Modifier.isStatic(m.getModifiers())) { 
                addType(m.getGenericReturnType()); 
                for (Type t : m.getGenericParameterTypes()) { 
                    addType(t); 
                } 
            } 
        } 
    } 
}
