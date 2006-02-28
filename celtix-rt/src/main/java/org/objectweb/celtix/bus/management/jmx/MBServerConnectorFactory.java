package org.objectweb.celtix.bus.management.jmx;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.objectweb.celtix.common.logging.LogUtils;



/** 
 * Deal with the MBeanServer Connections 
 *
 */
public class MBServerConnectorFactory {    
        
    public static final String DEFAULT_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi";
    
    private static final Logger LOG = LogUtils.getL7dLogger(MBServerConnectorFactory.class);

    private MBeanServer server;

    private String serviceUrl = DEFAULT_SERVICE_URL;

    private Map environment;

    private boolean threaded;

    private boolean daemon;

    private JMXConnectorServer connectorServer;
    
    public void setMBeanServer(MBeanServer ms) {
        server = ms;
    }

    public void setServiceUrl(String url) {
        serviceUrl = url;
    }

    public void setEnvironment(Properties env) {
        environment = env;
    }
   
    public void setEnvironment(Map env) {
        environment = env;
    }
    
    public void setThreaded(boolean fthread) {
        threaded = fthread;
    }
    
    public void setDaemon(boolean fdaemon) {
        daemon = fdaemon;
    }


    @SuppressWarnings("unchecked")
    public void createConnector() throws IOException {
        
        if (server == null) {
            server = MBeanServerFactory.createMBeanServer(); 
        }
        // startup the rmi registry locally
        try {
            java.rmi.registry.LocateRegistry.createRegistry(9913);
            
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "CREATE_REGISTRY_FAULT_MSG", new Object[]{ex});
        }     
        
        // Create the JMX service URL.
        JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

        // Create the connector server now.
        this.connectorServer = 
            JMXConnectorServerFactory.newJMXConnectorServer(url, environment, server);

       
        if (this.threaded) {
             // Start the connector server asynchronously (in a separate thread).
            Thread connectorThread = new Thread() {
                public void run() {
                    try {
                        connectorServer.start();
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "START_CONNECTOR_FAILURE_MSG", new Object[]{ex});
                    } 
                }
            };
            
            connectorThread.setName("JMX Connector Thread [" + this.serviceUrl + "]");
            connectorThread.setDaemon(daemon);
            connectorThread.start();
        } else {
             // Start the connector server in the same thread.
            this.connectorServer.start();
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("JMX connector server started: " + this.connectorServer);
        }    
    }

    public void destroy() throws IOException {        
        this.connectorServer.stop();        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("JMX connector server stoped: " + this.connectorServer);
        } 
    }

}
