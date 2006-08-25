package org.objectweb.celtix.jca.core.resourceadapter;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jca.core.logging.LoggerHelper;



public class ResourceBean implements Serializable {

    public static final String DEFAULT_VALUE_STRING = "DEFAULT";
    public static final String LOG_LEVEL = "log.level";
    public static final String CONFIG_DOMAIN = "celtix";
    public static final String CONFIG_SCOPE = "j2ee";
    public static final String DEFAULT_MONITOR_POLL_INTERVAL = "120";    
    public static final String EJB_SERVICE_PROPERTIES_URL = "ejb.service.properties.url";
    public static final String MONITOR_EJB_SERVICE_PROPERTIES = "monitor.ejb.service.properties";
    public static final String MONITOR_POLL_INTERVAL = "monitor.poll.interval";   
    private static final long serialVersionUID = -9186743162164946039L;

    static {
        // first use of log, default init if necessary
        LoggerHelper.init();
    }

    private static final Logger LOG = LogUtils.getL7dLogger(ResourceBean.class);

    private Properties pluginProps;

    public ResourceBean() {
        pluginProps = new Properties();
    }

    public ResourceBean(Properties props) {
        pluginProps = props;
    }

    public void setDisableConsoleLogging(boolean disable) {
        if (disable) {
            LoggerHelper.disableConsoleLogging();
        }
    }    
   
    public Properties getPluginProps() {
        return pluginProps;
    }

    public void setProperty(String propName, String propValue) {
        if (!DEFAULT_VALUE_STRING.equals(propValue)) {
            LOG.log(Level.CONFIG, "SETTING_PROPERTY", new Object[] {propName, propValue});
            getPluginProps().setProperty(propName, propValue);
        }
        if (LOG_LEVEL.equals(propName)) {
            LoggerHelper.setLogLevel(propValue);
        }
    }

    
    protected URL getPropsURL(String propsUrl) throws ResourceException {
        URL ret = null;
        if (propsUrl != null) {
            ret = createURL(propsUrl, "Unable to construct URL from URL string, value=" + propsUrl);
        }
        return ret;
    }

    protected URL createURL(String spec, String msg) throws ResourceAdapterInternalException {
        try {
            return new URL(spec);
        } catch (MalformedURLException mue) {
            throw new ResourceAdapterInternalException(msg, mue);
        }
    }

    public void validateURLString(String spec, String msg) throws ResourceAdapterInternalException {
        URL url = null;
        try {
            url = createURL(spec, msg);
            url.openStream();
            LOG.config("Validated url=" + url);
        } catch (IOException ioe) {
            throw new ResourceAdapterInternalException(msg, ioe);
        }
    }
    

}
