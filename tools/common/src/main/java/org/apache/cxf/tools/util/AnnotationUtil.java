package org.apache.cxf.tools.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.WebResult;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;

public final class AnnotationUtil {
    private static final Logger LOG = LogUtils.getL7dLogger(AnnotationUtil.class);
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
            Message msg = new Message("FAIL_TO_LOAD_CLASS", LOG, className);
            throw new ToolException(msg, e);
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

    public static WebResult getWebResult(Method method) {

        Annotation ann = method.getAnnotation(WebResult.class);
        if (ann == null) {
            return null;
        } else {
            return (WebResult)ann;
        }
    }

}
