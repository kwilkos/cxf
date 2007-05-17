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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;

public final class WrapperHelper {

    private WrapperHelper() {
        // complete
    }

    public static void setWrappedPart(String partName, Object wrapperType, Object part)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (part instanceof List) {
            setWrappedListProperty(partName, wrapperType, part);
        } else {
            String fieldName = partName;
            if (JAXBUtils.isJavaKeyword(partName)) {
                fieldName = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.VARIABLE);
            }


            if (part == null) {
                XmlElement el = null;
                Field elField = null;
                for (Field field : wrapperType.getClass().getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        elField = field;
                        el = elField.getAnnotation(XmlElement.class);
                        break;
                    }
                }
                if (el != null 
                    && !el.nillable() 
                    && elField.getType().isPrimitive()) {
                    throw new IllegalArgumentException("null value for field not permitted.");
                }
                return;
            }

            String modifier = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.SETTER);
            String modifier2 = modifier;
            if ("return".equals(partName)) {
                //some versions of jaxb map "return" to "set_return" instead of "setReturn"
                modifier2 = "set_return";
            }

            boolean setInvoked = false;
            for (Method method : wrapperType.getClass().getMethods()) {
                if (method.getParameterTypes() != null && method.getParameterTypes().length == 1
                    && (modifier.equals(method.getName())
                        || modifier2.equals(method.getName()))) {
                    if ("javax.xml.bind.JAXBElement".equals(method.getParameterTypes()[0].getName())) {
                        if (!setJAXBElementValueIntoWrapType(method, wrapperType, part)) {
                            throw new RuntimeException("Failed to set the part value (" + part 
                                + ") to wrapper type (" + wrapperType.getClass() + ")");
                        }
                    } else {
                        method.invoke(wrapperType, part);
                    }
                    setInvoked = true;
                    break;
                }
            }
            if (!setInvoked) {
                XmlElement el = null;
                Field elField = null;
                for (Field field : wrapperType.getClass().getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        elField = field;
                        el = elField.getAnnotation(XmlElement.class);
                        break;
                    }
                }
                // JAXB Type get XmlElement Annotation
                if (elField != null 
                    && el != null
                    && partName.equals(el.name())) {
                    elField.setAccessible(true);
                    elField.set(wrapperType, part);
                    setInvoked = true;
                }
            }
            if (!setInvoked) {
                throw new IllegalArgumentException("Could not find a modifier method on Wrapper Type for "
                                                   + partName);
            }
        }
    }

    private static boolean setJAXBElementValueIntoWrapType(Method method, Object wrapType, Object value) {
        String typeClassName = wrapType.getClass().getCanonicalName();
        String objectFactoryClassName = typeClassName.substring(0, typeClassName.lastIndexOf('.'))
                                        + ".ObjectFactory";
        try {
            Object objectFactory = wrapType.getClass().getClassLoader().loadClass(objectFactoryClassName)
                .newInstance();
            String methodName = "create" + wrapType.getClass().getSimpleName()
                                + method.getName().substring(3);
            Method objectFactoryMethod = objectFactory.getClass().getMethod(methodName, value.getClass());
            if (objectFactoryMethod != null) {
                JAXBElement je = (JAXBElement)objectFactoryMethod.invoke(objectFactory, value);
                method.invoke(wrapType, je);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private static void setWrappedListProperty(String partName, Object wrapperType, Object part)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String accessorName = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);
        for (Method method : wrapperType.getClass().getMethods()) {
            if (accessorName.equals(method.getName()) 
                && List.class.isAssignableFrom(method.getReturnType())) {
                Object ret = method.invoke(wrapperType);
                Method addAll = ret.getClass().getMethod("addAll", Collection.class);
                addAll.invoke(ret, part);
                break;
            }
        }
    }

    public static Object getWrappedPart(String partName, Object wrapperType, Class<?> partClazz)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);

        if (partClazz.equals(boolean.class) || partClazz.equals(Boolean.class)) {
            // JAXB Exception to get the Boolean property
            accessor = accessor.replaceFirst("get", "is");
        }

        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0 && accessor.equals(method.getName())) {

                return getValue(method, wrapperType);
            }
        }
        return null;
    }

    public static Object getWrappedPart(String partName, Object wrapperType, String elementType)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);
        Method method = null;
        NoSuchMethodException nsex = null;
        try {
            method = wrapperType.getClass().getMethod(accessor, new Class[0]); 
        } catch (NoSuchMethodException ex) {
            //ignore for now
            nsex = (NoSuchMethodException)ex.fillInStackTrace();
        }

        Field elField = null;
        if (method == null
            && elementType != null
            && "boolean".equals(elementType.toLowerCase())) {
            
            elField = getElField(partName, wrapperType);
                
            if (elField == null
                || (!Collection.class.isAssignableFrom(elField.getType())
                && !elField.getType().isArray())) {
    
                try {
                    method = wrapperType.getClass().getMethod(accessor.replaceFirst("get", "is"),
                                                              new Class[0]); 
                } catch (NoSuchMethodException ex) {
                    //ignore for now
                }            
            }
        }
        if (method == null 
            && "return".equals(partName)) {
            //RI generated code uses this
            try {
                method = wrapperType.getClass().getMethod("get_return", new Class[0]);
            } catch (NoSuchMethodException ex) {
                try {
                    method = wrapperType.getClass().getMethod("is_return",
                                                              new Class[0]);
                } catch (NoSuchMethodException ex2) {
                    //ignore for now
                } 
            }                
        }
        
        if (method != null) {
            return getValue(method, wrapperType);
        }
        if (elField == null) {
            elField = getElField(partName, wrapperType);
        }
        if (elField != null) {
            // JAXB Type get XmlElement Annotation
            XmlElement el = elField.getAnnotation(XmlElement.class);
            if (el != null
                && partName.equals(el.name())) {
                elField.setAccessible(true);
                return elField.get(wrapperType);
            }
        } else if (nsex != null) {
            throw nsex;
        }
        
        return null;
    }

    private static Field getElField(String partName, Object wrapperType) {
        String fieldName = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.VARIABLE);
        Field elField = null;
        for (Field field : wrapperType.getClass().getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                elField = field;
                break;
            }
        }        
        return elField;
    }
    
    public static Object getWrappedPart(String partName, Object wrapperType) throws IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);

        // TODO: There must be a way to determine the class by inspecting
        // wrapperType
        // if (partClazz.equals(boolean.class) ||
        // partClazz.equals(Boolean.class)) {
        // //JAXB Exception to get the Boolean property
        // accessor = accessor.replaceFirst("get", "is");
        // }
        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0 && accessor.equals(method.getName())) {
                return getValue(method, wrapperType);
            }
        }
        return null;
    }

    private static Object getValue(Method method, Object in) throws IllegalAccessException,
        InvocationTargetException {
        if ("javax.xml.bind.JAXBElement".equals(method.getReturnType().getCanonicalName())) {
            JAXBElement je = (JAXBElement)method.invoke(in);
            return je == null ? je : je.getValue();
        } else {
            return method.invoke(in);
        }
    }
}
