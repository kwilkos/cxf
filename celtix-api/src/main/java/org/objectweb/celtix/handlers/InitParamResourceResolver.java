package org.objectweb.celtix.handlers;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.resource.ResourceResolver;

public class InitParamResourceResolver implements ResourceResolver {

    private static final Logger LOG = LogUtils.getL7dLogger(InitParamResourceResolver.class);
    
    Map<String, String> params;
    
    public InitParamResourceResolver(Map<String, String> map) {
        params = map;
    }
    
    
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        
        String value = params.get(resourceName);
        return convertToType(value, resourceType);
    }

    public InputStream getAsStream(String name) {
        // returning these as a stream does not make much sense
        return null;
    }


    /**
     * Convert the string representation of value to type T
     */
    private <T> T convertToType(String value, Class<T> type) {
        
        /*
        char, byte, short, long, float, double, boolean
        */
        T ret = null;
        if (String.class.equals(type)) {
            ret = type.cast(value);
        } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
            ret =  type.cast(new Integer(value));
        } else if (Byte.class.equals(type) || Byte.TYPE.equals(type)) {
            ret = type.cast(new Byte(value));
        } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
            ret = type.cast(new Short(value));
        } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
            ret =  type.cast(new Long(value));
        } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
            ret = type.cast(new Float(value));
        } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
            ret = type.cast(new Double(value));
        } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
            ret = type.cast(Boolean.valueOf(value));
        } else if (Character.class.equals(type) || Character.TYPE.equals(type)) {
            ret = type.cast(value.charAt(0));
        } else {
            LOG.severe("do not know how to treat type: " + type);
        } 
        return ret;
    }
}
