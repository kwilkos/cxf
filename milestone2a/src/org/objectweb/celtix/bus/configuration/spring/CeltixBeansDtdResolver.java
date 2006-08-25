package org.objectweb.celtix.bus.configuration.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationException;

public class CeltixBeansDtdResolver implements EntityResolver {

    private static final String DTD_SYSTEM_ID = 
        "http://celtix.objectweb.org/configuration/spring/celtix-spring-beans.dtd";
    private static final String DTD_FILE = "celtix-spring-beans.dtd";
    private static final Logger LOG = LogUtils.getL7dLogger(BeanGenerator.class);

    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
        if (systemId != null && systemId.equals(DTD_SYSTEM_ID)) {
            InputStream is = getClass().getResourceAsStream(getDtdFile());
            if (is == null) {
                throw new ConfigurationException(new Message("COULD_NOT_RESOLVE_BEANS_DTD", LOG,
                                                             DTD_SYSTEM_ID));
            }
            InputSource source = new InputSource(is);
            source.setPublicId(publicId);
            source.setSystemId(systemId);
            return source;
        }
        // Use the default behavior -> download from website or wherever.
        return null;
    }
    
    protected String getDtdFile() {
        return DTD_FILE;
    }
}
