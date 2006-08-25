package org.objectweb.celtix.jca.core.classloader;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import org.objectweb.celtix.jca.jarloader.JarLoader;


public final class PlugInClassLoaderHelper {
    private static final Logger LOG = 
        Logger.getLogger(PlugInClassLoaderHelper.class.getName());
    private static Map<String, byte[]> nonClassesMap = new HashMap<String, byte[]>();
   

    private PlugInClassLoaderHelper() {
        // singleton
    }

    public static boolean hasResource(String name) {
        try {
            return getResourceAsBytes(name) != null;
        } catch (IOException ex) {
            LOG.fine("unexpected exception: " + ex);

            return false;
        }
    }
   
    public static byte[] getResourceAsBytes(String name) throws IOException {
        // check nonClassCache for properties etc..
        if (!name.endsWith(".class") && nonClassesMap.containsKey(name)) {
            return (byte[])(nonClassesMap.get(name));            
        }

        // first check file path directorys, then check jars
        if (!isJarReference(name)) {
            // try direct load of url
            try {
                return JarLoader.getBytesFromInputStream(new URL(name).openStream());
            } catch (java.net.MalformedURLException mue) {
                throw new IOException(mue.getMessage());
            }
        } else {
            // something with !/
            // check for a nested directory reference
            if (isNestedDirectoryReference(name)) {
                throw new IOException(
                        "Accessing contents of directories within jars is currently not supported");
            } else {
                String enclosingJar = name.substring(0, name.lastIndexOf("!/") + 2);
                String resourceName = name.substring(name.lastIndexOf("!/") + 2);
                Map jarMap = JarLoader.getJarContents(enclosingJar);

                if (null != jarMap && jarMap.containsKey(resourceName)) {
                    byte bytes[] = (byte[])jarMap.get(resourceName);

                    // this class will not be looked for again
                    // once it is loaded so to save memory we
                    // remove it form the map, if it is not a
                    // class we add it to the nonClasses cache,
                    // this is only true for in memory cache.
                    // REVISIT - this needs to be more specific,
                    // some classes Home|Remote interfaces are
                    // loaded multiple times - see remote class
                    // downloading for the moment disable this
                    // jarMap.remove(resourceName);
                    //
                    if (!name.endsWith(".class")) {
                        nonClassesMap.put(name, bytes);
                    }
                    return bytes;                    
                }
            }
        }

        // failed to locate the resource
        return null;
    }

    private static boolean isJarReference(String name) {
        return name.indexOf("!/") != -1;
    }

    private static boolean isNestedDirectoryReference(String path) {
        String nestedDir = path.substring(path.lastIndexOf("!/") + 2);

        return !"".equals(nestedDir) && nestedDir.endsWith("/");
    }
}
