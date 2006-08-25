package org.objectweb.celtix.configuration.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.ConfigurationProvider;

public class ConfigurationImpl implements Configuration {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ConfigurationImpl.class);
    private ConfigurationMetadata model;
    private CompoundName id;
    private List<ConfigurationProvider> providers;

    public ConfigurationImpl(ConfigurationMetadata m, CompoundName i) {
        model = m;
        id = i;
        providers = new Vector<ConfigurationProvider>();
    }

    public CompoundName getId() {
        return id;
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
        Object obj = getLocal(name);
        return null == obj ? definition.getDefaultValue() : obj;

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

        // use model to validate value
        Object defaultValue = definition.getDefaultValue();
        if (defaultValue != null && !defaultValue.getClass().isAssignableFrom(value.getClass())) {
            QName type = model.getDefinition(name).getType();
            throw new ConfigurationException(new Message("ITEM_TYPE_MISMATCH_EXC", BUNDLE, name, type));
        }

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

        if (accepted) {
            propertyModified(name);
        }
        return accepted;
    }

    public boolean save() {
        boolean accepted = false;

        //TODO:We need to persist all beans into one config file.
        
        for (ConfigurationProvider provider : providers) {
            if (provider.save()) {
                accepted = true;
                break;
            }
        }
        return accepted;
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

    public boolean setBoolean(String name, boolean value) {
        return setObject(name, Boolean.valueOf(value));
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

    public boolean setShort(String name, short value) {
        return setObject(name, new Short(value));
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

    public boolean setInt(String name, int value) {
        return setObject(name, new Integer(value));
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

    public boolean setFloat(String name, float value) {
        return setObject(name, new Float(value));
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

    public boolean setDouble(String name, double value) {
        return setObject(name, new Double(value));
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

    public boolean setLong(String name, long value) {
        return setObject(name, new Long(value));
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

    public boolean setString(String name, String value) {
        return setObject(name, (String)value);
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

    protected Object getLocal(String name) {
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

    public void propertyModified(String name) {
        //Do nothing
    }
}
