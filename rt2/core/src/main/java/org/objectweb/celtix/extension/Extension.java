package org.objectweb.celtix.extension;

import java.util.ArrayList;
import java.util.Collection;


public class Extension {

    private String className;
    private boolean deferred;
    private String name;
    private Collection<String> namespaces = new ArrayList<String>();
    
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
    
    String getName() {
        return name;
    }
    
    void setName(String n) {
        name = n;
    }
    
    Collection<String> getNamespaces() {
        return namespaces;
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
