package org.objectweb.celtix.jbi.se;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.WebService;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.ServiceConsumer;


public class WebServiceClassFinder {
    
    private static final Logger LOG = LogUtils.getL7dLogger(WebServiceClassFinder.class);
    private final String rootPath;
    private final ClassLoader parent;

    public WebServiceClassFinder(String argRootPath, ClassLoader loader) {
        if (argRootPath.endsWith(File.separator)) {
            argRootPath = argRootPath.substring(0, argRootPath.length() - 2);
        }
        rootPath = argRootPath;
        parent = loader;
    }

    public Collection<Class<?>> findServiceConsumerClasses() throws MalformedURLException {
        return find(new Matcher() {
            public boolean accept(Class<?> clz) {
                return ServiceConsumer.class.isAssignableFrom(clz)
                       && (clz.getModifiers() & Modifier.ABSTRACT) == 0;
            }
        });
    }

    public Collection<Class<?>> findWebServiceClasses() throws MalformedURLException {

        return find(new Matcher() {
            public boolean accept(Class<?> clz) {
                return clz.getAnnotation(WebService.class) != null
                       && (clz.getModifiers() & Modifier.ABSTRACT) == 0;
            }
        });
    }
    
    public Collection<Class<?>> findWebServiceInterface() throws MalformedURLException {

        return find(new Matcher() {
            public boolean accept(Class<?> clz) {
                return clz.getAnnotation(WebService.class) != null
                       && (clz.getModifiers() & Modifier.INTERFACE) == Modifier.INTERFACE;
            }
        });
    }

    private Collection<Class<?>> find(Matcher matcher) throws MalformedURLException {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        File root = new File(rootPath);
        URL[] urls = {root.toURL()};
        URLClassLoader loader = new URLClassLoader(urls, parent);

        find(root, loader, classes, matcher);
        return classes;
    }

    private void find(File dir, ClassLoader loader, Collection<Class<?>> classes, Matcher matcher) {

        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.toString().endsWith(".class")) {
                Class<?> clz = loadClass(loader, f);
                if (matcher.accept(clz)) {
                    classes.add(clz);
                }
            } else if (f.isDirectory()) {
                find(f, loader, classes, matcher);
            }
        }
    }

    private Class<?> loadClass(ClassLoader loader, File classFile) {

        String fileName = classFile.toString();
        String className = fileName.substring(rootPath.length());
        className = className.substring(0, className.length() - ".class".length())
            .replace(File.separatorChar, '.');
        if (className.startsWith(".")) {
            // ServiceMix and OpenESB are little different with rootPath, so here className may be begin
            // with "."
            className = className.substring(1, className.length());
        }
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            LOG.severe(new Message("FAILED.LOAD.CLASS", LOG) + className);
        }
        return null;
    }

    interface Matcher {
        boolean accept(Class<?> clz);
    }
}
