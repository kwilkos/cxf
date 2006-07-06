package org.objectweb.celtix.jca.celtix;

import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.core.resourceadapter.AbstractManagedConnectionFactoryImpl;
import org.objectweb.celtix.jca.core.resourceadapter.AbstractManagedConnectionImpl;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;

public class ManagedConnectionFactoryImpl 
    extends AbstractManagedConnectionFactoryImpl
    implements CeltixManagedConnectionFactory {

    private static final Logger LOG = Logger.getLogger(ManagedConnectionFactoryImpl.class.getName());
    protected BusFactory busFactory;

    public ManagedConnectionFactoryImpl() {
        super();
        LOG.info("ManagedConnectionFactoryImpl constructed without props by appserver...");
    }

    public ManagedConnectionFactoryImpl(Properties props) {
        super(props);
        LOG.info("ManagedConnectionFactoryImpl constructed with props by appserver. props = " + props);
    }

    public void setLogLevel(String logLevel) {
        setProperty(LOG_LEVEL, logLevel);
    }

    public void setConfigurationDomain(String name) {
        setProperty(CONFIG_DOMAIN, name);
    }

    public void setConfigurationScope(String name) {
        setProperty(CONFIG_SCOPE, name);
    }
   
    public void setEJBServicePropertiesURL(String name) {
        setProperty(EJB_SERVICE_PROPERTIES_URL, name);
    }

    public void setMonitorEJBServiceProperties(Boolean monitor) {
        setProperty(MONITOR_EJB_SERVICE_PROPERTIES, monitor.toString());
    }

    public void setEJBServicePropertiesPollInterval(Integer pollInterval) {
        setProperty(MONITOR_POLL_INTERVAL, pollInterval.toString());
    }

    public void setJAASLoginConfigName(String name) {
        setProperty(JAAS_LOGIN_CONFIG, name);
    }

    public void setJAASLoginUserName(String name) {
        setProperty(JAAS_LOGIN_USER, name);
    }
    
    public void setJAASLoginPassword(String name) {
        setProperty(JAAS_LOGIN_PASSWORD, name);
    }

    public String getLogLevel() {
        return getPluginProps().getProperty(LOG_LEVEL);
    }
    
    public String getConfigurationDomain() {
        return getPluginProps().getProperty(CONFIG_DOMAIN, CONFIG_DOMAIN);
    }

    public String getConfigurationScope() {
        return getPluginProps().getProperty(CONFIG_SCOPE, CONFIG_SCOPE);
    }
    
    public String getEJBServicePropertiesURL() {
        return getPluginProps().getProperty(EJB_SERVICE_PROPERTIES_URL);
    }

    public Boolean getMonitorEJBServiceProperties() {
        return Boolean.valueOf(getPluginProps().getProperty(MONITOR_EJB_SERVICE_PROPERTIES));
    }

    public Integer getEJBServicePropertiesPollInterval() {
        return new Integer(getPluginProps().getProperty(MONITOR_POLL_INTERVAL, 
                                                        DEFAULT_MONITOR_POLL_INTERVAL));
    }

    public String getJAASLoginConfigName() {
        return getPluginProps().getProperty(JAAS_LOGIN_CONFIG);
    }
    
    public String getJAASLoginUserName() {
        return getPluginProps().getProperty(JAAS_LOGIN_USER);
    }
    
    public String getJAASLoginPassword() {
        return getPluginProps().getProperty(JAAS_LOGIN_PASSWORD);
    }


    

    public URL getEJBServicePropertiesURLInstance() throws ResourceException {
        return getPropsURL(getEJBServicePropertiesURL());
    }

    // compliance: WL9 checks
    // need to ensure multiple instances with same config properties are equal
    // multiple instances with same config do not make sense to me
   

    protected void validateReference(AbstractManagedConnectionImpl conn, javax.security.auth.Subject subj) {
    }

    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceAdapterInternalException("Non-Managed usage is not supported, "
                        + "use createConnectionFactory with a ConnectionManager argument");
    }

    public Object createConnectionFactory(ConnectionManager connMgr) throws ResourceException {
        LOG.info("connManager=" + connMgr);
        if (connMgr == null) {
            throw new ResourceAdapterInternalException("Non-Managed usage is not supported, " 
                        + "the ConnectionManager argument can not be null");
        }
        init(connMgr.getClass().getClassLoader());
        //busFactory.setAppserverClassLoader(connMgr.getClass().getClassLoader());
        LOG.fine("Setting AppServer classloader in busFactory. " + connMgr.getClass().getClassLoader());
        return new ConnectionFactoryImpl(this, connMgr);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connReqInfo) 
        throws ResourceException {
        LOG.info("create connection, subject=" + subject + " connReqInfo=" + connReqInfo);
        init(Thread.currentThread().getContextClassLoader());
        return (ManagedConnection)new ManagedConnectionImpl(this, connReqInfo, subject);
    }

    public void close() throws javax.resource.spi.ResourceAdapterInternalException {
        LOG.info("close, this=" + this);
    }

    protected synchronized void init(ClassLoader appserverClassLoader) throws ResourceException {
        if (busFactory == null) {
            busFactory = new BusFactory(this);
            busFactory.create(appserverClassLoader, getBootstrapContext());
        }
    }

    public Bus getBus() {
        return (busFactory != null) ? busFactory.getBus() : null;
    }

    protected Object getBootstrapContext() {
        return null;
    }

   
}





