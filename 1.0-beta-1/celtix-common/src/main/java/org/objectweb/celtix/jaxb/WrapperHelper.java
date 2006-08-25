package org.objectweb.celtix.jaxb;


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
                    assert el != null;
                } 
            }
            
            if (part == null) {
                if (!el.nillable()) {
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
                    
                    Class<?> clazz = method.getParameterTypes()[0];
                    if (method.getParameterTypes()[0].isPrimitive()) {
                        clazz = el.type();
                    }
                    
                    if (clazz.isAssignableFrom(part.getClass())) {
                        method.invoke(wrapperType, part);
                        setInvoked = true;
                        break;
                    }
                }
            }
            
            if (!setInvoked) {
                throw new IllegalArgumentException("Could not find a modifier method on Wrapper Type");
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
                
                Class<?> clazz = method.getReturnType();

                if (clazz.isPrimitive() && !partClazz.isPrimitive()) {
                    for (Field field : wrapperType.getClass().getDeclaredFields()) {
                        if (JAXBUtils.isJavaKeyword(partName)) {
                            partName = JAXBUtils.nameToIdentifier(
                                            partName, 
                                           JAXBUtils.IdentifierType.VARIABLE);
                        }
                        if (field.getName().equals(partName)) {
                            //JAXB Type get XmlElement Annotation
                            clazz = field.getAnnotation(XmlElement.class).type();
                        } 
                    }
                }
                //TODO Support For Nillable Attribute
                if (clazz.isAssignableFrom(partClazz)) {
                    return method.invoke(wrapperType);
                }
            }
        }
        return null;
    }
}
