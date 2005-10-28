package org.objectweb.celtix.helpers;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.objectweb.celtix.bus.jaxb.JAXBUtils;

public final class WrapperHelper {

    private WrapperHelper() {
        //complete
    }


    public static void setWrappedPart(String partName, Object wrapperType, Object part) 
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (part instanceof List) {
            setWrappedListProperty(partName, wrapperType, part);
        } else {
            String accessor = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.SETTER);
            
            for (Method method : wrapperType.getClass().getMethods()) {
                if (method.getParameterTypes() != null 
                    && method.getParameterTypes().length == 1
                    && accessor.equals(method.getName())) {
                    
                    Class<?> clazz = method.getParameterTypes()[0];
                    if (method.getParameterTypes()[0].isPrimitive()) {
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
                    if (clazz.isAssignableFrom(part.getClass())) {
                        method.invoke(wrapperType, part);
                    }
                }
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
                return;
            }
        }
    }
    
    public static Object getWrappedPart(String partName, Object wrapperType, Class<?> partClazz) 
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String modifier = JAXBUtils.nameToIdentifier(partName, JAXBUtils.IdentifierType.GETTER);
        
        for (Method method : wrapperType.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0
                && modifier.equals(method.getName())
                && method.getReturnType().isAssignableFrom(partClazz)) {
                return method.invoke(wrapperType);
            }
        }
        return null;
    }
}
