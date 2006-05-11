package org.objectweb.celtix.bus.ws.rm.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.celtix.bus.configuration.wsrm.StoreInitParamType;
import org.objectweb.celtix.bus.configuration.wsrm.StoreType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

public class RMStoreFactory {
    
    protected static RMStore theStore;
    private static final Logger LOG = LogUtils.getL7dLogger(RMStoreFactory.class);
    
    
    public RMStore getStore(Configuration c) {
        
        StoreType s = c.getObject(StoreType.class, "store");
        if (null == s) {
            return null;
        }
        
        if (null == theStore) {
            createStore(s);
            initStore(s);
        }
        return theStore;
    }
    
    protected void createStore(StoreType s) {
        createStore(s, RMStoreFactory.class.getClassLoader());
    }
    
    protected void createStore(StoreType s, ClassLoader l) {
        String storeClassName = s.getStoreClass();
        assert null != storeClassName;
        Class<? extends RMStore> storeClass;
        try {
            storeClass = Class.forName(storeClassName, true, l).asSubclass(RMStore.class);
            theStore =  storeClass.newInstance();
        } catch (Exception ex) {
            throw new RMStoreException(new Message("RMSTORE_CREATION_EXC", LOG), ex);
        }         
    }
    
    protected void initStore(StoreType s) {
        Map<String, String> params = new HashMap<String, String>();
        for (StoreInitParamType sip : s.getInitParam()) {
            String key = sip.getParamName();
            String value = sip.getParamValue();
            if (null != key && null != value) {
                params.put(key, value);
            }
        }
        theStore.init(params);
    }
    
    
}
