package org.objectweb.celtix.tools.utils;

import java.util.HashMap;
import java.util.Map;

public final class ClassCollectorUtil {

    private static ClassCollectorUtil collector;
    
    private final Map<String, String> seiClassNames = new HashMap<String, String>();
    private final Map<String, String> typesClassNames = new HashMap<String, String>();
    private final Map<String, String> exceptionClassNames = new HashMap<String, String>();
        
    private ClassCollectorUtil() {
    }
        
    public static ClassCollectorUtil getInstance() {
        if (collector == null) {
            collector = new ClassCollectorUtil();
        }
        return collector;
    }

    public boolean containSeiClass(String packagename, String type) {
        return seiClassNames.containsKey(key(packagename, type));
    }

    public boolean containTypesClass(String packagename, String type) {
        return typesClassNames.containsKey(key(packagename, type));
    }
    
    public boolean containExceptionClass(String packagename, String type) {
        return exceptionClassNames.containsKey(key(packagename, type));
    }

    public void addSeiClassName(String packagename, String type, String fullClassName) {
        seiClassNames.put(key(packagename, type), fullClassName);
    }

    public void addTypesClassName(String packagename, String type, String fullClassName) {
        typesClassNames.put(key(packagename, type), fullClassName);
    }

    public void addExceptionClassName(String packagename, String type, String fullClassName) {
        exceptionClassNames.put(key(packagename, type), fullClassName);
    }

    public String getTypesFullClassName(String packagename, String type) {
        return typesClassNames.get(key(packagename, type));
    }
    
    private String key(String packagename, String type) {
        return packagename + "#" + type;
    }
}
