package org.objectweb.celtix.tools.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.objectweb.celtix.tools.common.ToolException;

public class AnnotationUtil {

    @SuppressWarnings("unchecked")
    public static <anoClass> anoClass getPrivClassAnnotation(final Class clazz, final Class anoClass) {
        return (anoClass)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return clazz.getAnnotation(anoClass);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static <anoClass> anoClass getPrivMethodAnnotation(final Method method, final Class anoClass) {
        return (anoClass)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return method.getAnnotation(anoClass);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static Annotation[][] getPrivParameterAnnotations(final Method method) {
        return (Annotation[][])AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return method.getParameterAnnotations();
            }
        });
    }

    public Class loadClass(String className) {
        Class clazz = null;
        URL[] urls = ProcessorUtil.pathToURLs(getClassPath());
        URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        try {
            clazz = classLoader.loadClass(className);
        } catch (Exception e) {
            throw new ToolException("Can not found class <" + className + "> in classpath");
        }
        return clazz;
    }

    private static String getClassPath() {
        return System.getProperty("java.class.path");
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

}
