package org.objectweb.celtix.common.bundle;


/**
 * A container for static utility methods related to resource bundle
 * naming conventons.
 */
public final class BundleUtils {
    /**
     * The resource bundle naming convention for class is a.b.c is a.b.Messages
     */
    private static final String MESSAGE_BUNDLE = ".Messages";

    /**
     * Prevents instantiation.
     */
    private BundleUtils() {
    }

    /**
     * Encapsulates the logic related to naming a resource bundle. 
     *
     * @param cls the Class requiring the bundle
     * @return an appropriate ResourceBundle
     */
    public static String getBundleName(Class cls) {
        return cls.getPackage().getName() + MESSAGE_BUNDLE;
    }
}
