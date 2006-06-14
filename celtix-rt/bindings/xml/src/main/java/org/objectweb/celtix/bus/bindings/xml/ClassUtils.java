package org.objectweb.celtix.bus.bindings.xml;

import java.lang.reflect.Method;

public final class ClassUtils {
    private ClassUtils() {
        // Complete
    }

    public static Method getMethod(Class<?> clazz, String methodName) throws Exception {
        Method[] declMethods = clazz.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
