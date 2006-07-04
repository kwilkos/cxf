package org.objectweb.celtix.jca.core.resourceadapter;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;

/**
 * allow URI resources from the root of the RAR to be specified as properties
 */
public class Handler extends URLStreamHandler {
    private static final Logger LOG = LogUtils.getL7dLogger(Handler.class);

    public URLConnection openConnection(URL someUrl) {
        LOG.fine("URL=" + someUrl);

        return new URLConnection(someUrl) {
            public void connect() {
                LOG.fine("connect");
            }

            public InputStream getInputStream() throws java.io.IOException {
                LOG.fine("getInputStream, path=" + url.getPath());
                InputStream is = Handler.class.getClassLoader().getResourceAsStream(url.getPath());
                if (is == null) {
                    throw new java.io.IOException(url.getPath()
                                                  + " Not Found by getResourceAsStream(), ClassLoader="
                                                  + Handler.class.getClassLoader());
                }
                return is;
            }
        };
    }
}
