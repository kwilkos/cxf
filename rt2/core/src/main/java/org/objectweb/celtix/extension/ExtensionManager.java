package org.objectweb.celtix.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.celtix.common.injection.ResourceInjector;
import org.springframework.core.io.UrlResource;

public class ExtensionManager {

    private ClassLoader loader;
    private ResourceInjector injector;
    private Map<String, Object> active;
    private Map<String, Collection<Extension>> deferred;

    public ExtensionManager(String resource, ClassLoader cl, ResourceInjector i) {
        loader = cl;
        injector = i;
        active = new HashMap<String, Object>();
        deferred = new HashMap<String, Collection<Extension>>();

        try {
            load(resource);
        } catch (IOException ex) {
            throw new ExtensionException(ex);
        }
    }

    public Object get(String name) {
        return active.get(name);
    }

    public void activateViaNS(String namespaceURI) {
        Collection<Extension> extensions = deferred.get(namespaceURI);
        if (null == extensions) {
            return;
        }
        for (Extension e : extensions) {
            Object obj = e.load(loader);
            injector.inject(obj);
            active.put(e.getName(), obj);
        }
        extensions.clear();
        deferred.remove(namespaceURI);
    }

    final void load(String resource) throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resource);
        List<Object> loaded = new ArrayList<Object>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            UrlResource urlRes = new UrlResource(url);
            InputStream is = urlRes.getInputStream();
            List<Object> objs = loadFragment(is);
            if (null != objs && objs.size() > 0) {
                loaded.addAll(objs);
            }
        }
        for (Object obj : loaded) {
            injector.inject(obj);
        }
    }

    final List<Object> loadFragment(InputStream is) {
        List<Extension> extensions = new ExtensionFragmentParser().getExtensions(is);
        List<Object> objs = new ArrayList<Object>();
        for (Extension e : extensions) {
            objs.add(processExtension(e));
        }
        return objs;
    }

    final Object processExtension(Extension e) {

        if (!e.isDeferred()) {
            Object obj = e.load(loader);
            active.put(e.getName(), obj);
            return obj;
        }

        Collection<String> namespaces = e.getNamespaces();
        for (String ns : namespaces) {
            Collection<Extension> extensions = deferred.get(ns);
            if (null == extensions) {
                extensions = new ArrayList<Extension>();
                deferred.put(ns, extensions);
            }
            extensions.add(e);
        }
        return null;
    }

}
