package org.objectweb.celtix.version;

import java.io.*;
import java.util.*;

public final class Version {

    private static String version;
    
    private static final String VERSION_BASE = "/org/objectweb/celtix/version/";

    private Version() {
        // utility class - never constructed
    }

    private static InputStream getResourceAsStream(String resource) {
        ClassLoader cl = Version.class.getClassLoader();
        InputStream ins = cl.getResourceAsStream(resource);
        if (ins == null && resource.startsWith("/")) {
            ins = cl.getResourceAsStream(resource.substring(1));
        }
        return ins;
    }
    
    private static synchronized void loadProperties() {
        if (version == null) {
            Properties p = new Properties();

            try {
                InputStream ins = getResourceAsStream(VERSION_BASE + "version.properties");

                p.load(ins);
                ins.close();
            } catch (IOException ex) {
                // ignore, will end up with defaults
            }

            version = p.getProperty("product.version");
        }
    }

    public static String getCurrentVersion() {
        loadProperties();
        return version;
    }


    /**
     * Returns version string as normally used in print, such as 3.2.4
     *
     */
    public static String getCompleteVersionString() {
        return getCurrentVersion();
    }
}
