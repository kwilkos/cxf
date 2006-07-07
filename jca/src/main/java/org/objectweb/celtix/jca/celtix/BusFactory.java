package org.objectweb.celtix.jca.celtix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;
import org.objectweb.celtix.jca.core.resourceadapter.UriHandlerInit;

public class BusFactory {
    
    private static final Logger LOG = Logger.getLogger(BusFactory.class.getName());

    private Bus bus;
    private List servantsCache = new Vector();
    private InitialContext jndiContext;
    private ClassLoader appserverClassLoader;
    private ManagedConnectionFactoryImpl mcf;
    private Object raBootstrapContext;

    public BusFactory(ManagedConnectionFactoryImpl aMcf) {
        this.mcf = aMcf;
    }

    protected String[] getBusArgs() throws ResourceException {
        //TODO need to rewrite , there only setup the BUSID
        
        String celtixDomainsDir = mcf.getCeltixInstallDir();

        String orbDomainName = mcf.getConfigurationDomain();
        LOG.info("ORBDomainName=" + orbDomainName);

        String orbId = mcf.getConfigurationScope();
        LOG.info("ORBid=" + orbId);

        String busArgs[] = new String[6];
        busArgs[0] = "-ORBconfig_domains_dir";
        busArgs[1] = celtixDomainsDir;
        busArgs[2] = "-ORBdomain_name";
        busArgs[3] = orbDomainName;
        busArgs[4] = "-ORBid";
        busArgs[5] = orbId;
      

        return busArgs;
    }

    private Bus initBus(ClassLoader loader) throws ResourceException {
        try {
            Class busClazz = Class.forName(getBusClassName(), true, loader);
            Class types[] = {String[].class, Hashtable.class};
            Method method = busClazz.getMethod("init", types);
            bus = (Bus)method.invoke(null, new Object[] {getBusArgs()});
            LOG.info("initialize complete, bus=" + bus);
        } catch (Exception ex) {
            throw new ResourceAdapterInternalException("Failed to initialize artix runtime", ex);
        }

        return bus;
    }
    
    protected synchronized void init() throws ResourceException {
        LOG.info("initialising... the bus");
        new UriHandlerInit();

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            // ensure resourceadapter: url handler can be found by URLFactory
            Thread.currentThread().setContextClassLoader(cl);
            mcf.validateProperties();
            bus = initBus(cl);
            initialiseServants();
        } catch (Exception ex) {
            if (ex instanceof ResourceAdapterInternalException) {
                throw (ResourceException)ex;
            } else {
                throw new ResourceAdapterInternalException("Failed to initialize connector runtime", ex);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    
    void initialiseServants() throws ResourceException {
        if (isMonitorEJBServicePropertiesEnabled()) {
            LOG.info("ejb service properties update enabled. ");
            startPropertiesMonitorThread();
        } else {
            URL propsUrl = mcf.getEJBServicePropertiesURLInstance();
            if (propsUrl != null) {
                initialiseServantsFromProperties(loadProperties(propsUrl), false);
            }
        }
    }

    void initialiseServantsFromProperties(Properties ejbServants, boolean abortOnFailure)
        throws ResourceException {

        // Props format: jndi name = service string
        //
        // java:/ejbs/A={http:/a/b/b}SoapService@http://localost:wsdls/a.wsdl

        try {
            jndiContext = new InitialContext();
        } catch (NamingException ne) {
            throw new ResourceAdapterInternalException(
                      "Failed to construct InitialContext for EJBServant(s) jndi lookup, reason: "
                       + ne, ne);
        }

        deregisterServants(bus);

        LOG.info("Initialising EJB endpoints...");
        String loginConfigName = mcf.getJAASLoginConfigName();
        if (loginConfigName == null) {
            loginConfigName = "other";
        }
        Enumeration keys = ejbServants.keys();

        while (keys.hasMoreElements()) {
            String jndiName = (String)keys.nextElement();
            String serviceName = (String)ejbServants.getProperty(jndiName);
            LOG.info("Found ejb endpoint: jndi name=" + jndiName + ", wsdl service=" + serviceName);
            //TODO publish the service endpoint 
            /* try {
                
                //initialiseServant(jndiName, serviceName);
                //registerXAResource();
            } catch (ResourceException re) {
                LOG.warning("Error initialising servant with jndi name " 
                            + jndiName + " and service name "
                            + serviceName + " Exception:"
                            + re.getMessage());
                if (abortOnFailure) {
                    throw re;
                }

            }*/
        }
    }

    //TODO need to publish the Endpoint
    /*
    void initialiseServant(String jndiName, String serviceName) throws ResourceException {

        if ("".equals(serviceName)) {
            throw new ResourceAdapterInternalException(
                       "A WSDL service QName must be specified as the value of the EJB JNDI name key: "
                       + jndiName);
        } else {
            QName service = serviceQNameFromString(serviceName);
            String wsdlLocation = wsdlLocFromString(serviceName);
            if (wsdlLocation == null) {
                try {
                    wsdlLocation = bus.getServiceWSDL(service);
                    LOG.info("getServiceWSDL returns " + wsdlLocation + " for service: " + serviceName);
                } catch (BusException be) {
                    throw new ResourceAdapterInternalException(
                               "Service string value:"
                               + serviceName
                               + " for key="
                               + jndiName
                               + " is incomplete. You must specify wsdl location using the '@' notation, "
                               + "eg: jndiName={namespace url}ServiceName@WsdlURL or configure"
                               + " the relevant bus:" + "initial_contract in configuration",
                               be);
                }
            }
            mcf.validateURLString(wsdlLocation,
                                  "WSDL location specified using '@' notation"
                                  + " in service string is invalid, value="
                                  + wsdlLocation);
            String portName = portNameFromString(serviceName);
            try {
                CeltixConnectEJBServant servant;
                if (getBootstrapContext() == null) {
                    logger.info("No transaction inflow involved.");
                    servant = new CeltixConnectEJBServant(this, wsdlLocation, jndiName);
                } else {
                    logger.info("Transaction inflow involved.");
                    servant = new TransactionalArtixConnectEJBServant(this, wsdlLocation, jndiName);
                }

                if (portName != null) {
                    bus.registerServant(servant, service, portName);
                } else {
                    bus.registerServant(servant, service);
                }
                LOG.info("Registering ejb servant: " + servant + " for service: " + service
                            + (portName == null ? "" : ", with port: " + portName));

            } catch (Exception e) {
                throw new ResourceAdapterInternalException("Failed to register EJBServant for jnidName "
                                                           + jndiName, e);
            }

            synchronized (servantsCache) {
                servantsCache.add(service);
            }
        }
    }*/

    void startPropertiesMonitorThread() throws ResourceException {
        Integer pollIntervalInteger = mcf.getEJBServicePropertiesPollInterval();
        int pollInterval = pollIntervalInteger.intValue();
        LOG.info("ejb service properties poll interval is : " + pollInterval + " seconds");
        EJBServicePropertiesMonitorRunnable r = new EJBServicePropertiesMonitorRunnable(pollInterval);
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    boolean isMonitorEJBServicePropertiesEnabled() throws ResourceException {
        boolean retVal = false;

        if (mcf.getMonitorEJBServiceProperties().booleanValue()) {
            URL url = mcf.getEJBServicePropertiesURLInstance();
            if (url == null) {
                throw new ResourceAdapterInternalException(
                           "MonitorEJBServiceProperties property is set to true,"
                           + " but EJBServicePropertiesURL is not set. " 
                           + "Both properties must be set to enable monitoring.");
            }
            retVal = isFileURL(url);
        }

        return retVal;
    }

    boolean isFileURL(URL url) {
        return url != null && "file".equals(url.getProtocol());
    }

    protected void deregisterServants(Bus aBus) throws ResourceException {
        synchronized (servantsCache) {
            if (!servantsCache.isEmpty()) {
                QName service = null;
                Iterator servants = servantsCache.iterator();
                while (servants.hasNext()) {
                    service = (QName)(servants.next());
                    LOG.info("Deregistering ejb servant for service: " + service);
                    /*try {
                        //TODO need to remove the service endpoint
                        //bus.removeServant(service);
                    } catch (BusException be) {
                        LOG.warning("Failed to deregister ejb servant for service: " 
                                    + service + "Exception" + be);
                    }*/
                }
                servantsCache.clear();
            }
        }
    }

    Properties loadProperties(URL propsUrl) throws ResourceException {
        Properties props = null;
        InputStream istream = null;

        LOG.info("loadProperties, url=" + propsUrl);

        try {
            istream = propsUrl.openStream();
        } catch (IOException ioe) {
            throw new ResourceAdapterInternalException("Failed to openStream to URL, value=" + propsUrl
                                                       + ", reason:" + ioe, ioe);
        }

        try {
            props = new Properties();
            props.load(istream);
        } catch (IOException ioe) {
            props = null;
            throw new ResourceAdapterInternalException("Failed to load properties from " + propsUrl, ioe);
        } finally {
            try {
                istream.close();
            } catch (IOException ignored) {
                //do nothing here
            }
        }

        return props;
    }

    QName serviceQNameFromString(String qns) throws ResourceAdapterInternalException {
        String ns = null;
        String lp = null;

        // String re = "(\[(.*)\])?([^\@]+)(@?(.*))??";
        // String[] qna = qns.split("(\[?+[^\]]*\])([^@])@?+(.*)");

        try {
            StringTokenizer st = new StringTokenizer(qns, "{},@", true);
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if ("{".equals(t)) {
                    ns = st.nextToken();
                    st.nextToken();
                    // consume '}'
                } else if (",".equals(t)) {
                    st.nextToken();
                    // consume 'portName'
                } else if ("@".equals(t)) {
                    st.nextToken();
                    // consume 'wsdlLoc'
                } else {
                    lp = t;
                }
            }
        } catch (java.util.NoSuchElementException nsee) {
            throw new ResourceAdapterInternalException(
                       "Incomplete QName, string is not in expected format: "
                       + "[{namespace}]local part[@ wsdl location url]. value:"
                       + qns, nsee);
        }
        LOG.fine("QN=" + qns + ", ns=" + ns + ", lp=" + lp);
        return new QName(ns, lp);
    }

    String portNameFromString(String qns) throws ResourceAdapterInternalException {
        String portName = null;
        try {
            StringTokenizer st = new StringTokenizer(qns, ",@", true);
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (",".equals(t)) {
                    if (portName != null) {
                        throw new ResourceAdapterInternalException(
                                   "portName already set, string is not in expected format:"
                                   + " [{namespace}]serviceName[,portName][@ wsdl location url]. value:"
                                   + qns);
                    }

                    portName = st.nextToken();

                    if ("@".equals(portName)) {
                        throw new ResourceAdapterInternalException(
                                   "Empty portName, string is not in expected format: "
                                   + "[{namespace}]serviceName[,portName][@ wsdl location url]. value:"
                                   + qns);
                    }
                }
            }
        } catch (java.util.NoSuchElementException nsee) {
            throw new ResourceAdapterInternalException(
                       "Incomplete QName, string is not in expected format: "
                       + "[{namespace}]serviceName[,portName][@ wsdl location url]. value:"
                       + qns, nsee);
        }
        return portName;
    }

    String wsdlLocFromString(String qns) {
        String wloc = null;
        StringTokenizer st = new StringTokenizer(qns, "@", true);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if ("@".equals(t)) {
                wloc = st.nextToken();
            }
        }
        return wloc;
    }

    
    private String getBusClassName() {
        return System.getProperty("test.bus.class", "org.objectweb.celtix.Bus");
    }

    protected List getRegisteredServants() {
        return servantsCache;
    }

    public ClassLoader getAppserverClassLoader() {
        return appserverClassLoader;
    }

    public void setAppserverClassLoader(ClassLoader classLoader) {
        this.appserverClassLoader = classLoader;
    }

    public InitialContext getInitialContext() {
        return jndiContext;
    }

    public Object getBootstrapContext() {
        return raBootstrapContext;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus b) {
        bus = b;
    }

    public String getJAASLoginConfigName() {
        return mcf.getJAASLoginConfigName();
    }

    public String getJAASLoginUserName() {
        return mcf.getJAASLoginUserName();
    }

    // REVISIT - gtully - feb-08-05 - may want to hide plain text password
    public String getJAASLoginPassword() {
        return mcf.getJAASLoginPassword();
    }

    public void create(ClassLoader classLoader, Object context) throws ResourceException {
        this.appserverClassLoader = classLoader;
        this.raBootstrapContext = context;
        init();
    }

    class EJBServicePropertiesMonitorRunnable implements Runnable {
        private long previousModificationTime;
        private final int pollIntervalSeconds;
        private final File propsFile;
        private boolean continuing = true;

        EJBServicePropertiesMonitorRunnable(int pollInterval) throws ResourceException {
            pollIntervalSeconds = pollInterval;
            propsFile = new File(mcf.getEJBServicePropertiesURLInstance().getPath());
        }

        public void setContinue(boolean c) {
            this.continuing = c;
        }

        public void run() {
            do {
                try {
                    if (isPropertiesFileModified()) {
                        LOG.info("ejbServicePropertiesFile modified, initialising/updating servants");
                        initialiseServantsFromProperties(loadProperties(propsFile.toURI().toURL()), false);
                    }
                    Thread.sleep(pollIntervalSeconds * 1000);
                } catch (Exception e) {
                    LOG.info("MonitorThread: failed to initialiseServantsFromProperties "
                              + "with properties absolute path="
                              + propsFile.getAbsolutePath() + ", reason: " + e.toString());
                }
            } while (continuing);
        }

        protected boolean isPropertiesFileModified() throws ResourceException {
            boolean fileModified = false;
            if (propsFile.exists()) {
                long currentModificationTime = propsFile.lastModified();
                if (currentModificationTime > previousModificationTime) {
                    previousModificationTime = currentModificationTime;
                    fileModified = true;
                }
            }
            return fileModified;
        }
    }

    // for unit test
    protected void setBootstrapContext(Object ctx) {
        raBootstrapContext = ctx;
    }

    
}
