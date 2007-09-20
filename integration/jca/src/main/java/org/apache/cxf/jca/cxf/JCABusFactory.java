/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jca.cxf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.rmi.PortableRemoteObject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;
import org.apache.cxf.jca.core.resourceadapter.UriHandlerInit;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;


public class JCABusFactory {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JCABusFactory.class);

    private Bus bus;
    private BusFactory bf;
    private List<Server> servantsCache = new ArrayList<Server>();
    private InitialContext jndiContext;
    private ClassLoader appserverClassLoader;
    private ManagedConnectionFactoryImpl mcf;
    private Object raBootstrapContext;

    public JCABusFactory(ManagedConnectionFactoryImpl aMcf) {
        this.mcf = aMcf;
    }

    protected String[] getBusArgs() throws ResourceException {
        //There is only setting up the BUSID        
        String busId = mcf.getConfigurationScope();
        LOG.fine("BUSid=" + busId);

        String busArgs[] = new String[2];
        busArgs[0] = "-BUSid";
        busArgs[1] = busId;
        return busArgs;
    }

    protected Bus createCXFBus() throws ResourceException {
        try {
            bf = BusFactory.newInstance();
            bus = bf.createBus();
        } catch (Exception ex) {
            throw new ResourceAdapterInternalException("Failed to initialize cxf runtime", ex);
        }
        return bus;
    }    
    
    protected synchronized void init() throws ResourceException {
        LOG.info("Initializing the CXF BUS....");
        
        new UriHandlerInit();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = this.getClass().getClassLoader();

            // ensure resourceadapter: url handler can be found by URLFactory
            Thread.currentThread().setContextClassLoader(cl);
            
            //TODO Check for the managed connection factory properties
            //TODO We may need get the configuration file from properties 
            //mcf.validateProperties();     
            bus = createCXFBus();
            initializeServants();
        } catch (Exception ex) {
            if (ex instanceof ResourceAdapterInternalException) {
                throw (ResourceException)ex;
            } else {
                ex.printStackTrace();
                throw new ResourceAdapterInternalException("Failed to initialize connector runtime", ex);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    
    void initializeServants() throws ResourceException {
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
       
        Enumeration keys = ejbServants.keys();

        while (keys.hasMoreElements()) {
            String jndiName = (String)keys.nextElement();
            String serviceName = (String)ejbServants.getProperty(jndiName);
            LOG.fine("Found ejb endpoint: jndi name=" + jndiName + ", wsdl service=" + serviceName);
            
            try {
                initialiseServant(jndiName, serviceName);      
            } catch (ResourceException re) {
                LOG.warning("Error initialising servant with jndi name " 
                            + jndiName + " and service name "
                            + serviceName + " Exception:"
                            + re.getMessage());
                if (abortOnFailure) {
                    throw re;
                }
            }
        }
    }
    
    void initialiseServant(String jndiName, String serviceName) throws ResourceException {

        Server servant = null;
        EJBObject ejb = null;
        QName serviceQName = null;
        String nameSpace = "";
        String interfaceName = "";
        String packageName = "";
        Class interfaceClass = null;
        ClassLoader ejbClassLoader = null;
        ClassLoader currentThreadContextClassLoader = null;
        try {
            if ("".equals(serviceName)) {
                throw new ResourceAdapterInternalException(
                              "A WSDL service QName must be specified as the value of the EJB JNDI name key: "
                              + jndiName);
            } else {
                serviceQName = serviceQNameFromString(serviceName);

                serviceQNameFromString(serviceName);
                
                // Get ejbObject
                ejb = getEJBObject(jndiName); 
                ejbClassLoader = ejb.getClass().getClassLoader();

                //NOTE we can use the ejbClassLoader to load WSDL
                currentThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(ejbClassLoader);
                               
                Thread.currentThread().setContextClassLoader(currentThreadContextClassLoader);

                nameSpace = serviceQName.getNamespaceURI();
                packageName = PackageUtils.parsePackageName(nameSpace, null);
                
                // Get interface of ejbObject
                interfaceName = jndiName.substring(0, jndiName.length() - 4);
                interfaceName = packageName + "." + interfaceName;

                interfaceClass = Class.forName(interfaceName);
                
                // NOTE We can check the annoation to descide which kind of frontend we will use
                // Almostly we just need to use the jax-ws frontend
                // If we have wsdl , then wsdl first,
                // else code first
                
                servant = publishServantWithoutWSDL(ejb, jndiName, nameSpace, interfaceClass);
            }
        } catch (Exception e) {
            throw new ResourceAdapterInternalException(e.getMessage());
        }
        synchronized (servantsCache) {
            if (servant != null) {
                servantsCache.add(servant);
            }
        }
    }
    
    public Server publishServantWithoutWSDL(EJBObject ejb, String jndiName,
                                            String nameSpace, Class interfaceClass) 
        throws Exception {

        String hostName = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            //hostName = addr.getHostName();
            hostName = addr.getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String address = "http://" + hostName + ":9999/" + jndiName;

        ReflectionServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        Service service = createService(interfaceClass, bean);
        
        // REVISIT this is easy to be replace by EJBMethodInvoker
        service.setInvoker(new JAXWSMethodInvoker(ejb));

        ServerFactoryBean svrFactory = new ServerFactoryBean();

        return createServer(svrFactory, bean, address);

        
    }

    protected Service createService(Class interfaceClass, ReflectionServiceFactoryBean serviceFactory) 
        throws JAXBException {
        serviceFactory.setDataBinding(new JAXBDataBinding());
        serviceFactory.setBus(bus);
        serviceFactory.setServiceClass(interfaceClass);
        
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("test", "test");
        serviceFactory.setProperties(props);
        
        return serviceFactory.create();
    }
    
    protected Server createServer(ServerFactoryBean serverFactory, 
                                  ReflectionServiceFactoryBean serviceFactory, 
                                  String address) {
        serverFactory.setAddress(address);
        serverFactory.setTransportId("http://schemas.xmlsoap.org/soap/http");
        serverFactory.setServiceFactory(serviceFactory);
        serverFactory.setBus(bus);

        return serverFactory.create();
    }
   
    private EJBObject getEJBObject(String jndi) throws BusException {
        try {
            EJBHome home = getEJBHome(jndiContext, jndi);

//      ejbHomeClassLoader = home.getClass().getClassLoader();

            Method createMethod = home.getClass().getMethod("create", new Class[0]);

            return (EJBObject) createMethod.invoke(home, new Object[0]);
        } catch (NamingException e) {
            throw new BusException(e);
        } catch (NoSuchMethodException e) {
            throw new BusException(e);
        } catch (IllegalAccessException e) {
            throw new BusException(e);
        } catch (InvocationTargetException itex) {
            Throwable thrownException = itex.getTargetException();
            throw new BusException(thrownException);
        }
    }
    
    protected EJBHome getEJBHome(Context ejbContext, String jndiName) throws NamingException {
        Object obj = ejbContext.lookup(jndiName);
        return (EJBHome) PortableRemoteObject.narrow(obj, EJBHome.class);
    }

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

    protected void deregisterServants(Bus aBus) {
        synchronized (servantsCache) {
            if (!servantsCache.isEmpty()) {
                Iterator<Server> servants = servantsCache.iterator();
                while (servants.hasNext()) {
                    Server servant = servants.next();
                    servant.stop();                    
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
        String lp = null;
        String nameSpace = null;

        // String re = "(\[(.*)\])?([^\@]+)(@?(.*))??";
        // String[] qna = qns.split("(\[?+[^\]]*\])([^@])@?+(.*)");

        try {
            StringTokenizer st = new StringTokenizer(qns, "{},@", true);
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if ("{".equals(t)) {
                    nameSpace = st.nextToken();
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
        LOG.fine("QN=" + qns + ", ns=" + nameSpace + ", lp=" + lp);
        return new QName(nameSpace, lp);
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
