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
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.PropertiesResolver;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;
import org.springframework.core.io.UrlResource;

public class ExtensionManagerImpl implements ExtensionManager {

    public static final String EXTENSIONMANAGER_PROPERTY_NAME = "extensionManager";
    
    private final ClassLoader loader;
    private ResourceInjector injector;
    private Map<String, Collection<Extension>> deferred;
    private Map<Class, Object> activated;

    public ExtensionManagerImpl(String resource, ClassLoader cl, Map<String, Object> properties) {
        loader = cl;

        properties.put(EXTENSIONMANAGER_PROPERTY_NAME, this);

        ResourceResolver resolver = new PropertiesResolver(properties);
        ResourceManager resourceManager = new DefaultResourceManager(resolver);
        injector = new ResourceInjector(resourceManager);

        deferred = new HashMap<String, Collection<Extension>>();
        activated = new HashMap<Class, Object>();

        try {
            load(resource);
        } catch (IOException ex) {
            throw new ExtensionException(ex);
        }
    }

    public <T> T getExtension(Class<T> extensionType) {
        Object obj = activated.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return null;
    }

    public void activateViaNS(String namespaceURI) {
        Collection<Extension> extensions = deferred.get(namespaceURI);
        if (null == extensions) {
            return;
        }
        for (Extension e : extensions) {
            loadAndRegister(e);
        }
        extensions.clear();
        deferred.remove(namespaceURI);
    }

    final void load(String resource) throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resource);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            UrlResource urlRes = new UrlResource(url);
            InputStream is = urlRes.getInputStream();
            loadFragment(is);       
        }
        
    }

    final void loadFragment(InputStream is) {
        List<Extension> extensions = new ExtensionFragmentParser().getExtensions(is);
        for (Extension e : extensions) {
            processExtension(e);
        }
    }

    final void processExtension(Extension e) {

        if (!e.isDeferred()) {
            loadAndRegister(e);
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
    }
    
    final void loadAndRegister(Extension e) {
        Object obj = e.load(loader);
        injector.inject(obj);
        if (null != e.getInterfaceName()) {
            activated.put(e.loadInterface(loader), obj);
        }
    }

}
