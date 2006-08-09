package org.objectweb.celtix.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.common.injection.ResourceInjector;
import org.springframework.core.io.UrlResource;

public class ExtensionManager {

    private ClassLoader loader;
    private ResourceInjector injector;

    private Map<String, Collection<Extension>> deferred;    

    public ExtensionManager(String resource, ClassLoader cl, ResourceInjector i) {
        loader = cl;
        injector = i;
        deferred = new HashMap<String, Collection<Extension>>();

        try {
            load(resource);
        } catch (IOException ex) {
            throw new ExtensionException(ex);
        }
    }
    

    public void activateViaNS(String namespaceURI) {
        Collection<Extension> extensions = deferred.get(namespaceURI);
        if (null == extensions) {
            return;
        }
        for (Extension e : extensions) {
            Object obj = e.load(loader);
            injector.inject(obj);
        }
        extensions.clear();
        deferred.remove(namespaceURI);
    }

    final void load(String resource) throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resource);
        Collection<Object> loaded = new ArrayList<Object>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            UrlResource urlRes = new UrlResource(url);
            InputStream is = urlRes.getInputStream();
            Object obj = loadFragment(is);
            if (null != obj) {
                loaded.add(obj);
            }
        }
        for (Object obj : loaded) {
            injector.inject(obj);
        }
    }

    final Object loadFragment(InputStream is) {
        Extension e = new ExtensionFragmentParser().getExtension(is);
        return processExtension(e);
    }
    
    final Object processExtension(Extension e) {
    
        if (!e.isDeferred()) {
            return e.load(loader);
        }

        String key = e.getKey();
        Collection<Extension> extensions = deferred.get(key);
        if (null == extensions) {
            extensions = new ArrayList<Extension>();
            deferred.put(key, extensions);
        }
        extensions.add(e);
        return null;
    }

}
