package org.objectweb.celtix.bus.configuration;

import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItem;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.Configurator;
import org.objectweb.celtix.configuration.StringListType;

public abstract class AbstractConfigurationImpl implements Configuration {

    private Configurator configurator;
    private ConfigurationMetadata model;
    private Map<String, ConfigurationItem> items;

    public AbstractConfigurationImpl(URL url, Configuration parent) {
        configurator = new ConfiguratorImpl(this, parent);
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder();
        if (null != url) {
            model = builder.build(url);
        } else {
            model = new ConfigurationMetadataImpl();
        }
        items = new HashMap<String, ConfigurationItem>();
    }

    public AbstractConfigurationImpl(URL url) {
        this(url, null);
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
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NOT_DEFINED", name));
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
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        try {
            return ((Boolean)obj).booleanValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
    }

    public double getDouble(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        try {
            return ((Double)obj).doubleValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
        
    }

    public int getInteger(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        try {
            return ((BigInteger)obj).intValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
    }

    public long getLong(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        try {
            return ((Long)obj).longValue();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
    }

    public String getString(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        if (!(obj instanceof String)) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
        return (String)obj;
    }
    
    public List<String> getStringList(String name) {
        Object obj = getObject(name);
        if (null == obj) {
            throw new ConfigurationException(new ConfigurationMessage("ITEM_NO_VALUE", name));
        }
        try {
            return ((StringListType)obj).getItem();
        } catch (ClassCastException ex) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new ConfigurationMessage("ITEM_TYPE_MISMATCH", name, type));
        }
    }
    
    

}
