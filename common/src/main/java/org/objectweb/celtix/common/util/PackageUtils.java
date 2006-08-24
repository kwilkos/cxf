package org.apache.cxf.common.util;

public final class PackageUtils {
    
    private PackageUtils() {
        
    }
    
    static String getPackageName(String className) {
        int pos = className.lastIndexOf('.');
        if (pos != -1) {
            return className.substring(0, pos);
        } else {
            return "";
        }
    }
    
    public static String getPackageName(Class<?> clazz) {
        String className = clazz.getName();
        return getPackageName(className);
    }
    
    
}
