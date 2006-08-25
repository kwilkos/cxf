package org.objectweb.celtix.configuration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class ConfigurationBuilderFactory {
    private static final String DEFAULT_CONFIGURATION_BUILDER_CLASSNAME = 
        "org.objectweb.celtix.bus.configuration.CeltixConfigurationBuilder";
    private static ConfigurationBuilder theBuilder;

    private ConfigurationBuilderFactory() {
    }
    
    public static ConfigurationBuilder getBuilder() {
        return getBuilder(null);
    }
    public static void clearBuilder() {
        theBuilder = null;
    }

    public static ConfigurationBuilder getBuilder(ClassLoader classLoader) {

        if (null == theBuilder) {
            String builderClass = getBuilderClass(classLoader);

            // create the builder

            theBuilder = createBuilder(builderClass, selectClassLoader(classLoader));
        }
        return theBuilder;
    }

    private static ConfigurationBuilder createBuilder(String className, ClassLoader classLoader) {

        Class<? extends ConfigurationBuilder> builderClass;
        try {
            builderClass = Class.forName(className, true, classLoader)
                .asSubclass(ConfigurationBuilder.class);
            return builderClass.newInstance();
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }

    private static ClassLoader selectClassLoader(ClassLoader classLoader) {
        ClassLoader ret = classLoader;
        if (null == classLoader) {
            ret = ConfigurationBuilderFactory.class.getClassLoader();
        }
        return ret;
    }

    private static String getBuilderClass(ClassLoader classLoader) throws ConfigurationException {

        String builderClass = null;

        // check system properties
        builderClass = System.getProperty(ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY);
        if (isValidBuilderClass(builderClass)) {
            return builderClass;
        }

        try {
            // next, check for the services stuff in the jar file
            String serviceId = "META-INF/services/"
                               + ConfigurationBuilder.CONFIGURATION_BUILDER_CLASS_PROPERTY;
            InputStream is = null;

            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            if (classLoader == null) {
                is = ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is = classLoader.getResourceAsStream(serviceId);
            }
            if (is != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                builderClass = rd.readLine();
                rd.close();
            }
            if (isValidBuilderClass(builderClass)) {
                return builderClass;
            }

            // otherwise use default
            builderClass = DEFAULT_CONFIGURATION_BUILDER_CLASSNAME;
            return builderClass;
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }

    private static boolean isValidBuilderClass(String builderClassName) {
        return builderClassName != null && !"".equals(builderClassName);
    }

}
