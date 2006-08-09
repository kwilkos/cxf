package org.objectweb.celtix.extension;


public class Extension {

    private String className;
    private boolean deferred;
    private String keyName;
    
    String getClassName() {
        return className;
    }
    
    void setClassName(String c) {
        className = c;
    }
      
    boolean isDeferred() {
        return deferred;
    }
    
    void setDeferred(boolean d) {
        deferred = d;
    }
    
    String getKey() {
        return keyName;
    }
    
    void setKey(String k) {
        keyName = k;
    }
    
    Object load(ClassLoader cl) {
        Object obj = null;
        try {
            Class<?> cls = cl.loadClass(className);
            obj = cls.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ExtensionException(ex);
        } catch (IllegalAccessException ex) {
            throw new ExtensionException(ex);
        } catch (InstantiationException ex) {
            throw new ExtensionException(ex);
        }
        
        return obj;
    }
    
    
}
