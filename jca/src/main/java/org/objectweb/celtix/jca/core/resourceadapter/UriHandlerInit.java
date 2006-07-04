package org.objectweb.celtix.jca.core.resourceadapter;

import java.util.*;
import java.util.logging.Logger;
import org.objectweb.celtix.common.logging.LogUtils;

public class UriHandlerInit {
    private static final String PACKAGE_PREFIX = "org.objectweb.celtix.jca.core";
    private static final Logger LOG = LogUtils.getL7dLogger(UriHandlerInit.class);
  

    public UriHandlerInit() {
        initUriHandlers(PACKAGE_PREFIX);
    }

    public UriHandlerInit(String prefix) {
        initUriHandlers(prefix);
    }

    protected final void initUriHandlers(String prefix) {
        Properties properties = System.getProperties();
        String s = properties.getProperty("java.protocol.handler.pkgs");

        if (s == null) {
            s = prefix;
        } else {
            if (s.indexOf(prefix) == -1) {
                s = prefix + "|" + s;
            }
        }

        System.setProperty("java.protocol.handler.pkgs", s);
        properties.put("java.protocol.handler.pkgs", s);

        LOG.fine("java.protocol.handler.pkgs=" + s);
    }
}
