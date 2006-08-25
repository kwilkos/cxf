package org.objectweb.celtix.jca.core.classloader;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


public class Handler extends URLStreamHandler {
    public URLConnection openConnection(URL someUrl) {
        return new HandlerUrlConnection(someUrl);
    }

    class HandlerUrlConnection extends URLConnection {
        public HandlerUrlConnection(URL someUrl) {
            super(someUrl);
        }

        public void connect() {
        }

        public InputStream getInputStream() throws java.io.IOException {
            byte bytes[] = PlugInClassLoaderHelper.getResourceAsBytes(url.getPath());

            if (bytes != null) {
                return new java.io.ByteArrayInputStream(bytes);
            } else {
                throw new java.io.IOException(url.getPath() + " not found");
            }
        }
    }
}
