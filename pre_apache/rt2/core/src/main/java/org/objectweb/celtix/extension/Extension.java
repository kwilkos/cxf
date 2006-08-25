package org.objectweb.celtix.extension;

import java.util.ArrayList;
import java.util.Collection;


public class Extension {

    private String className;
    private String interfaceName;
    private boolean deferred;
    private Collection<String> namespaces = new ArrayList<String>();
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("class: ");
        buf.append(className);
        buf.append(", interface: ");
        buf.append(interfaceName);
        buf.append(", deferred: ");
        buf.append(deferred ? "true" : "false");
        buf.append(", namespaces: (");
        int n = 0;
        for (String ns : namespaces) {
            if (n > 0) {
                buf.append(", ");
            }
            buf.append(ns);
            n++;
        }
        buf.append(")");
        return buf.toString();        
    }
    
    String getClassname() {
        return className;
    }
    
    void setClassname(String i) {
        className = i;
    }
       
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String i) {
        interfaceName = i;
    }

    boolean isDeferred() {
        return deferred;
    }
    
    void setDeferred(boolean d) {
        deferred = d;
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
    
    Class loadInterface(ClassLoader cl) {
        Class<?> cls = null;
        try {
            cls = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException ex) {
            throw new ExtensionException(ex);
        }
        return cls;
    }
    
    
}
