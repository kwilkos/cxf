package org.objectweb.celtix.management;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;
import org.objectweb.celtix.configuration.instrumentation.types.InstrumentationPolicyType;
import org.objectweb.celtix.configuration.instrumentation.types.MBServerPolicyType;
import org.objectweb.celtix.management.jmx.JMXManagedComponentManager;




public class InstrumentationManagerImpl implements InstrumentationManager {    
    static final Logger LOG = LogUtils.getL7dLogger(InstrumentationManagerImpl.class);
    static final String INSTRUMENTATION_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/configuration/instrumentation";

    // TODO: avoid clashes with bus id

    static final String INSTRUMENTATION_CONFIGURATION_ID = 
        "instrumentation";
    
    private Configuration configuration;
    private List <Instrumentation> instrumentations;
    private JMXManagedComponentManager jmxManagedComponentManager;

    
    public InstrumentationManagerImpl() {
        this(new ConfigurationBuilderImpl());
    }

    public InstrumentationManagerImpl(ConfigurationBuilder builder) {
        LOG.info("Setting up InstrumentationManager");
        
        configuration = getConfiguration(builder);
        InstrumentationPolicyType ip = 
            configuration.getObject(InstrumentationPolicyType.class, "InstrumentationControl");   
        
        if (ip.isInstrumentationEnabled()) {
            instrumentations = new LinkedList<Instrumentation>();
        }
            
        if (ip.isJMXEnabled()) {           
            jmxManagedComponentManager = new JMXManagedComponentManager();
            MBServerPolicyType mbsp = configuration.getObject(MBServerPolicyType.class, "MBServer");
            jmxManagedComponentManager.init(mbsp);
        }
        
        
    }
    
    private Configuration getConfiguration(ConfigurationBuilder cb) {
        
        CompoundName id = new CompoundName(INSTRUMENTATION_CONFIGURATION_ID); 
        return cb.getConfiguration(INSTRUMENTATION_CONFIGURATION_URI,  id);
    }

    public void shutdown() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Shutdown InstrumentationManager ");
        }
        
        if (jmxManagedComponentManager != null) {
            jmxManagedComponentManager.shutdown();
        }
    }
    
    public void register(Instrumentation it) {
        if (it == null) {
            // just return
            return;
        } else {
            instrumentations.add(it); 
            if (jmxManagedComponentManager != null) {
                jmxManagedComponentManager.registerMBean(it);
            }
        }        
    }

    public void unregister(Object component) {
        for (Iterator<Instrumentation> i = instrumentations.iterator(); i.hasNext();) {
            Instrumentation it = i.next();
            if (it.getComponent() == component) {
                i.remove();   
                if (it != null && jmxManagedComponentManager != null) {
                    jmxManagedComponentManager.unregisterMBean(it);               
                }
                return;
            }
        }
    }
    
    public List<Instrumentation> getAllInstrumentation() {
        // TODO need to add more query interface
        return instrumentations;
    }

    public MBeanServer getMBeanServer() {        
        return jmxManagedComponentManager.getMBeanServer();
    }
      

}
