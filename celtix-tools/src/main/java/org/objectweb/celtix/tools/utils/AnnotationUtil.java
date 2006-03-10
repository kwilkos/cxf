package org.objectweb.celtix.tools.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.jws.WebParam;

import org.objectweb.celtix.tools.common.ToolException;

public final class AnnotationUtil {
    private AnnotationUtil() {

    }

    public static <T extends Annotation> T getPrivClassAnnotation(final Class<?> clazz,
                                                                  final Class<T> anoClass) {
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            public T run() {
                return clazz.getAnnotation(anoClass);
            }
        });
    }

    public static <T extends Annotation> T getPrivMethodAnnotation(final Method method,
                                                                   final Class<T> anoClass) {
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            public T run() {
                return method.getAnnotation(anoClass);
            }
        });
    }

    public static Annotation[][] getPrivParameterAnnotations(final Method method) {
        return (Annotation[][])AccessController.doPrivileged(new PrivilegedAction<Annotation[][]>() {
            public Annotation[][] run() {
                return method.getParameterAnnotations();
            }
        });
    }

    public static synchronized URLClassLoader getClassLoader(ClassLoader parent) {
        URL[] urls = ProcessorUtil.pathToURLs(getClassPath());
        return new URLClassLoader(urls, parent);
    }

    public static synchronized Class loadClass(String className, ClassLoader parent) {
        Class clazz = null;
        URL[] urls = ProcessorUtil.pathToURLs(getClassPath());
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        try {
            clazz = classLoader.loadClass(className);
        } catch (Exception e) {
            throw new ToolException("Can not found class <" + className + "> in classpath");
        }
        return clazz;
    }

    private static String getClassPath() {
        ClassLoader loader = AnnotationUtil.class.getClassLoader();
        StringBuffer classpath = new StringBuffer(System.getProperty("java.class.path"));
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlloader = (URLClassLoader)loader;
            for (URL url : urlloader.getURLs()) {
                classpath.append(File.pathSeparatorChar);
                classpath.append(url.getFile());
            }
        }
        return classpath.toString();
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static WebParam getWebParam(Method method, String paraName) {

        Annotation[][] anno = getPrivParameterAnnotations(method);
        int count = method.getParameterTypes().length;
        for (int i = 0; i < count; i++) {
            for (Annotation ann : anno[i]) {
                if (ann.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)ann;
                    if (paraName.equals(webParam.name())) {
                        return webParam;
                    }
                }
            }
        }
        return null;

    }

}
