package org.objectweb.celtix.common.i18n;

import java.util.ResourceBundle;


/**
 * A container for static utility methods related to resource bundle
 * naming conventons.
 */
public final class BundleUtils {
    /**
     * The default resource bundle naming convention for class is a.b.c is a.b.Messages
     */
    private static final String MESSAGE_BUNDLE = ".Messages";

    /**
     * Prevents instantiation.
     */
    private BundleUtils() {
    }

    /**
     * Encapsulates the logic related to naming the default resource bundle
     * for a class. 
     *
     * @param cls the Class requiring the bundle
     * @return an appropriate ResourceBundle name
     */
    public static String getBundleName(Class<?> cls) {
        return cls.getPackage().getName() + MESSAGE_BUNDLE;
    }
    
    /**
     * Encapsulates the logic related to naming the resource bundle
     * with the given relative name for a class. 
     *
     * @param cls the Class requiring the bundle
     * @return an appropriate ResourceBundle name
     */
    public static String getBundleName(Class<?> cls, String name) {
        return cls.getPackage().getName() + "." + name;
    }

    /**
     * Encapsulates the logic related to locating the default resource bundle
     * for a class. 
     *
     * @param cls the Class requiring the bundle
     * @return an appropriate ResourceBundle
     */
    public static ResourceBundle getBundle(Class<?> cls) {
        return ResourceBundle.getBundle(getBundleName(cls));
    }
    
    /**
     * Encapsulates the logic related to locating the resource bundle with the given 
     * relative name for a class.
     *
     * @param cls the Class requiring the bundle
     * @param name the name of the resource
     * @return an appropriate ResourceBundle
     */
    public static ResourceBundle getBundle(Class<?> cls, String name) {
        return ResourceBundle.getBundle(getBundleName(cls, name));
    }
}
