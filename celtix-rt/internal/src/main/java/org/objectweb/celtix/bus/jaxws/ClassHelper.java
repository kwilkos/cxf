package org.objectweb.celtix.bus.jaxws;

public final class ClassHelper {

    private ClassHelper() {
        // empty
    }

    public static Class<?> forName(String className) throws ClassNotFoundException { 
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) { 
            loader = ClassHelper.class.getClassLoader();
        } 
        return Class.forName(className, true, loader);
    } 
}
