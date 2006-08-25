package org.objectweb.celtix.configuration.impl;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.configuration.Configurator;

public class AbstractConfigurationImpl implements Configuration {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractConfigurationImpl.class);
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AbstractConfigurationImpl.class);
    private Configurator configurator;
    private ConfigurationMetadata model;
    private String id;
    private List<ConfigurationProvider> providers;

    public AbstractConfigurationImpl(ConfigurationMetadata m, String instanceId, Configuration parent) {
        model = m;
        id = instanceId;
        configurator = new ConfiguratorImpl(this, parent instanceof AbstractConfigurationImpl 
                                            ? (AbstractConfigurationImpl)parent 
                                            : null);

        providers = new Vector<ConfigurationProvider>();
        
        // temporary:
        
        providers.add(new InMemoryProvider());

        DefaultConfigurationProviderFactory factory = DefaultConfigurationProviderFactory.getInstance();
        ConfigurationProvider defaultProvider = factory.createDefaultProvider(this);

        if (null != defaultProvider) {
            providers.add(defaultProvider);
        }
    }
   
    public Object getId() {
        return id;
    }

    public Configuration getParent() {
        if (null != configurator.getHook()) {
            return configurator.getHook().getConfiguration();
        }
        return null;
    }

    public Configuration getChild(String namespaceURI, Object childId) {
        for (Configurator c : configurator.getClients()) {
            if (namespaceURI.equals(c.getConfiguration().getModel().getNamespaceURI()) 
                && childId.equals(c.getConfiguration().getId())) {
                return c.getConfiguration();
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Could not find child configuration with id: " + childId);
        }
        return null;
    }

    public ConfigurationMetadata getModel() {
        return model;
    }

    public List<ConfigurationProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<ConfigurationProvider> p) {
        providers = p;
    }

    public <T> T getObject(Class<T> cls, String name) {
        Object obj = getObject(name);
        return cls.cast(obj);
    }
    
    public Object getObject(String name) {

        ConfigurationItemMetadata definition = model.getDefinition(name);
        if (null == definition) {
            throw new ConfigurationException(new Message("ITEM_NOT_DEFINED_EXC", BUNDLE, name));
        }

        Configuration holder = this;
        while (null != holder) {
            Object obj = getLocal(holder, name);
            if (null != obj) {
                return obj;
            }
            holder = holder.getParent();
        }
        return definition.getDefaultValue();
    }
    
    /**
     * Check if property is defined and validate the value.
     * Then try all providers in turn until one is found that accepts the change.
     */
    public boolean setObject(String name, Object value) {
        ConfigurationItemMetadata definition = model.getDefinition(name);
        if (null == definition) {
            throw new ConfigurationException(new Message("ITEM_NOT_DEFINED_EXC", BUNDLE, name));
        }
        // TODO: use model to validate value
       
        // TODO: check if property can be modified at all:
        // if it is static, report an error, if it is process or bus accept the change but log a
        // warning informing the user that the change will take effect only after next bus
        // or process restart
 
        // try all registered providers in turn to find one that accepts the change
        boolean accepted = false;

        for (ConfigurationProvider provider : providers) {
            if (provider.setObject(name, value)) {
                accepted = true;
                break;
            }
        }

        // TODO: event listeners
        if (accepted) {
            //notify listeners
        }
        return false;
    }

    public boolean getBoolean(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Boolean)obj).booleanValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public short getShort(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Short)obj).shortValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public int getInt(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Integer)obj).intValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public float getFloat(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Float)obj).floatValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public double getDouble(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Double)obj).doubleValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public long getLong(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((Long)obj).longValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    public String getString(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        if (!(obj instanceof String)) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
        return (String)obj;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            Method method = obj.getClass().getMethod("getItem", new Class[0]);
            obj = method.invoke(obj, new Object[0]);
            
            return (List<String>)obj;
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        } catch (NoSuchMethodException e) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        } catch (IllegalAccessException e) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        } catch (InvocationTargetException e) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }
    }

    protected Object getLocal(Configuration c, String name) {
        if (null == providers) {
            return null;
        }
        Object obj;
        for (ConfigurationProvider provider : providers) {
            obj = provider.getObject(name);
            if (null != obj) {
                return obj;
            }
        }
        return null;
    }

    public final Configurator getConfigurator() {
        return configurator;
    }
}
