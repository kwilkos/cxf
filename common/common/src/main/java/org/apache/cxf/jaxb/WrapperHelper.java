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

import javax.xml.bind.annotation.XmlElement;


public final class WrapperHelper {

    private WrapperHelper() {
        //complete
    }


    public static void setWrappedPart(String partName, Object wrapperType, Object part) 
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        
        if (part instanceof List) {
            setWrappedListProperty(partName, wrapperType, part);
        } else {
            String fieldName = partName;
            if (JAXBUtils.isJavaKeyword(partName)) {
                fieldName = JAXBUtils.nameToIdentifier(
                                partName, 
                               JAXBUtils.IdentifierType.VARIABLE);
            }
            
            XmlElement el = null;
            for (Field field : wrapperType.getClass().getDeclaredFields()) {
              
                if (field.getName().equals(fieldName)) {
                    //JAXB Type get XmlElement Annotation
                    el = field.getAnnotation(XmlElement.class);
                   // assert el != null;
                } 
            }
            
            if (part == null) {
                if (el != null && !el.nillable()) {
                    throw new IllegalArgumentException("null value for field not permitted.");
                }
                return;
            }

            String modifier = 
                JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.SETTER);            
            
            boolean setInvoked = false;
            for (Method method : wrapperType.getClass().getMethods()) {
                if (method.getParameterTypes() != null 
                    && method.getParameterTypes().length == 1
                    && modifier.equals(method.getName())) {

                    method.invoke(wrapperType, part);
                    setInvoked = true;
                    break;
                }
            }
            
            if (!setInvoked) {
                throw new IllegalArgumentException("Could not find a modifier method on Wrapper Type for " 
                                                   + partName);
            }
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
            //JAXB Exception to get the Boolean property
            accessor = accessor.replaceFirst("get", "is");
        }
        
        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0
                && accessor.equals(method.getName())) {

                return method.invoke(wrapperType);
            }
        }
        return null;
    }

    public static Object getWrappedPart(String partName, Object wrapperType, String elementType)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);

        if ("boolean".equals(elementType.toLowerCase())) {
            // JAXB Exception to get the Boolean property
            accessor = accessor.replaceFirst("get", "is");
        }

        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0 && accessor.equals(method.getName())) {

                return method.invoke(wrapperType);
            }
        }
        return null;
    }

    

    public static Object getWrappedPart(String partName, Object wrapperType)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);

        // TODO: There must be a way to determine the class by inspecting wrapperType
        // if (partClazz.equals(boolean.class) ||
        // partClazz.equals(Boolean.class)) {
        // //JAXB Exception to get the Boolean property
        // accessor = accessor.replaceFirst("get", "is");
        //        }
        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0
                && accessor.equals(method.getName())) {
                return method.invoke(wrapperType);
            }
        }
        return null;
    }
}
