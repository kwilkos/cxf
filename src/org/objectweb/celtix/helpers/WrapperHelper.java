package org.objectweb.celtix.helpers;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public final class WrapperHelper {

    private WrapperHelper() {
        //complete
    }


    public static void setWrappedPart(String name, Object wrapperType, Object part) 
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (part instanceof List) {
            setWrappedListProperty(name, wrapperType, part);
        } else {
            
            Method elMethods[] = wrapperType.getClass().getMethods();
            for (Method method : elMethods) {
                if (method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(part.getClass())) {
                    method.invoke(wrapperType, part);
                }
            }
        }
    }

    private static void setWrappedListProperty(String name, Object wrapperType, Object part) 
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String accessorName = "get" + name;
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

}
