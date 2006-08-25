package org.apache.cxf.binding;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.cxf.BusException;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.extension.ExtensionManager;

public final class BindingFactoryManagerImpl implements BindingFactoryManager {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(BindingFactoryManagerImpl.class);
    
    final Map<String, BindingFactory> bindingFactories;
    Properties factoryNamespaceMappings;
    
    @Resource
    ExtensionManager extensionManager;
   
    public BindingFactoryManagerImpl() throws BusException {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>();
    }
    
    BindingFactory loadBindingFactory(String className, String ...namespaceURIs) throws BusException {
        BindingFactory factory = null;
        try {
            Class<? extends BindingFactory> clazz = 
                Class.forName(className).asSubclass(BindingFactory.class);

            factory = clazz.newInstance();

            for (String namespace : namespaceURIs) {
                registerBindingFactory(namespace, factory);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new BusException(cnfe);
        } catch (InstantiationException ie) {
            throw new BusException(ie);
        } catch (IllegalAccessException iae) {
            throw new BusException(iae);
        }
        return factory;
    }
    
    public void registerBindingFactory(String name,
                                       BindingFactory factory) {
        bindingFactories.put(name, factory);
    }
    
    public void unregisterBindingFactory(String name) {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String namespace) throws BusException {
        BindingFactory factory = bindingFactories.get(namespace);
        if (null == factory) { 
            extensionManager.activateViaNS(namespace);            
            factory = bindingFactories.get(namespace);
        }
        if (null == factory) {
            throw new BusException(new Message("NO_BINDING_FACTORY_EXC", BUNDLE, namespace));
        }
        return factory;
    }
    
}
