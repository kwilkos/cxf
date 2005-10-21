package org.objectweb.celtix.bus.configuration;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItem;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.Configurator;
import org.objectweb.celtix.configuration.types.StringListType;

public abstract class AbstractConfigurationImpl implements Configuration {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AbstractConfigurationImpl.class);
    private Configurator configurator;
    private ConfigurationMetadata model;
    private Map<String, ConfigurationItem> items;

    public AbstractConfigurationImpl(InputStream is, Configuration parent) {
        configurator = new ConfiguratorImpl(this, parent);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        if (null != is) {
            model = builder.build(is);
        } else {
            model = new ConfigurationMetadataImpl();
        }
        items = new HashMap<String, ConfigurationItem>();
    }

    public AbstractConfigurationImpl(InputStream is) {
        this(is, null);
    }
    
    public Object getId() {
        return getClass().getName();
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    public ConfigurationMetadata getModel() {
        return model;
    }

    public ConfigurationItem getItem(String name) {
        return items.get(name);
    }

    public Object getObject(String name) {

        ConfigurationItemMetadata definition = model.getDefinition(name);
        if (null == definition) {
            throw new ConfigurationException(new Message("ITEM_NOT_DEFINED_EXC", BUNDLE, name));
        }

        Configuration holder = this;
        ConfigurationItem item = null;
        while (true) {
            item = holder.getItem(name);
            if (null != item) {
                break;
            }
            Configurator hook = holder.getConfigurator().getHook();
            if (null == hook) {
                break;
            }
            holder = hook.getConfiguration();
        }
        if (item == null) {
            return definition.getDefaultValue();
        } else {
            return item.getValue();
        }
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

    public int getInteger(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new Message("ITEM_NO_VALUE_EXC", BUNDLE, name));
        }
        try {
            return ((BigInteger)obj).intValue();
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
    
    

}
