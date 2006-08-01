package org.objectweb.celtix.jca.core.classloader;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


public class PlugInClassLoader extends SecureClassLoader {
    private static final Logger LOG =  Logger.getLogger(PlugInClassLoader.class.getName());
    private static final String FILE_COLON = "file:";
    private static final String ZIP_COLON = "zip:";
    private static final String URL_SCHEME_COLON = "classloader:";
    private static final String JARS_PROPS_FILE = "jars.properties";
    private static final String FILTERS_PROPS_FILE = "filters.properties";
    private String jarUrls[] = new String[0];
    private final ProtectionDomain protectionDomain;

    public PlugInClassLoader(ClassLoader p) throws IOException {
        super(new FireWallClassLoader(p, getFilterList(p)));
        protectionDomain = getClass().getProtectionDomain();
        jarUrls = loadUrls(p);
        processJarUrls(jarUrls);
    }

    private void processJarUrls(String urls[]) {
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].startsWith(ZIP_COLON)) {
                urls[i] = FILE_COLON + urls[i].substring(ZIP_COLON.length());
                LOG.fine("process the url:" + urls[i]);                
            }
        }
    }

    private static String[] getFilterList(ClassLoader parent) throws IOException {
        Properties filtersProps = getProperties(parent, FILTERS_PROPS_FILE);

        return (String[])filtersProps.keySet().toArray(new String[] {});
    }

    private static Properties getProperties(ClassLoader parent, String propsFileName) throws IOException {
        InputStream in = parent.getResourceAsStream(propsFileName);

        if (null == in) {
            in = PlugInClassLoader.class.getResourceAsStream(propsFileName);

            if (null == in) {
                String msg = "Internal rar classloader failed to locate configuration resource: "
                        + propsFileName;
                IOException ioe = new IOException(msg);

                LOG.warning(ioe.toString());
                throw ioe;
            }
        }

        Properties props = new Properties();

        props.load(in);
        LOG.fine("Contents: " + propsFileName + props);

        return props;
    }

    private String[] loadUrls(ClassLoader parent) throws IOException {
        List<String> urlList = new ArrayList<String>();

        Properties props = getProperties(parent, JARS_PROPS_FILE);

        LOG.fine(props.toString());

        Enumeration keys = props.keys();

        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            URL url = parent.getResource(key);

            if (url != null) {
                LOG.fine(url.toString());
                urlList.add(url.toString());
            }
        }

        return (String[])urlList.toArray(new String[] {});
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");

        byte bytes[] = null;

        for (int i = 0; i < jarUrls.length; i++) {
            String fullpath = jarUrls[i] + "!/" + path;

            try {
                bytes = PlugInClassLoaderHelper.getResourceAsBytes(fullpath);

                if (bytes != null) {
                    break;
                }
            } catch (IOException ex) {
                // we should find everything we look for but if we don't our
                // parent can when we throw cnf below
                LOG.fine("findClass: " + name + ": " + ex.toString());
            }
        }

        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length, protectionDomain);
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    protected URL findResource(String name) {
        LOG.fine("findResource: " + name);
        for (int i = 0; i < jarUrls.length; i++) {
            String fullpath = jarUrls[i] + "!/" + name;

            if (PlugInClassLoaderHelper.hasResource(fullpath)) {
                return genURL(fullpath);
            }
        }

        return null;
    }

    protected URL genURL(String path) {
        URL url = null;
        String urlString = URL_SCHEME_COLON + path;

        try {
            url = new URL(null, urlString, new Handler());
        } catch (MalformedURLException mue) {
            LOG.warning(mue.toString());
        }

        return url;
    }
}
