package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.io.InputStream;
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
import org.objectweb.celtix.configuration.types.StringListType;

public class AbstractConfigurationImpl implements Configuration {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractConfigurationImpl.class);
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AbstractConfigurationImpl.class);
    private Configurator configurator;
    private ConfigurationMetadata model;
    private Object id;
    private List<ConfigurationProvider> providers;

    public AbstractConfigurationImpl(Class cl, String resource, Object instanceId, Configuration parent) {
        configurator = new ConfiguratorImpl(this, (AbstractConfigurationImpl)parent);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        InputStream is = null;
        if (null != cl && resource != null) {
            is = cl.getResourceAsStream(resource);
            if (is == null) {
                throw new ConfigurationException(new Message("METADATA_RESOURCE_EXC", BUNDLE, resource));
            }
        }

        if (null != is) {
            try {
                model = builder.build(is);
            } catch (IOException ex) {
                throw new ConfigurationException(new Message("METADATA_RESOURCE_EXC", BUNDLE, resource), ex);
            }
        } else {
            model = new ConfigurationMetadataImpl();
        }
        id = instanceId;

        providers = new Vector<ConfigurationProvider>();

        DefaultConfigurationProviderFactory factory = DefaultConfigurationProviderFactory.getInstance();
        ConfigurationProvider defaultProvider = factory.createDefaultProvider(this);

        if (null != defaultProvider) {
            providers.add(defaultProvider);
        }
    }

    public AbstractConfigurationImpl(Class cl, String resource, Object instanceId) {
        this(cl, resource, instanceId, null);
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

    public List<String> getStringList(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((StringListType)obj).getItem();
        } catch (ClassCastException ex) {
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

    protected final Configurator getConfigurator() {
        return configurator;
    }

}
