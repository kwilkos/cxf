package org.apache.cxf.management.jmx;



import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;


import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.instrumentation.types.MBServerPolicyType;
import org.apache.cxf.management.Instrumentation;
import org.apache.cxf.management.jmx.export.runtime.ModelMBeanAssembler;


/**
 * The manager class for the JMXManagedComponent which hosts the JMXManagedComponents.
 */
public class JMXManagedComponentManager {
    private static final Logger LOG = LogUtils.getL7dLogger(JMXManagedComponentManager.class);
   
    private boolean platformMBeanServer;    
    private ModelMBeanAssembler mbAssembler; 
    private MBServerConnectorFactory mcf;    
    private MBeanServer mbs;
    
    public JMXManagedComponentManager() {        
        mbAssembler = new ModelMBeanAssembler();    
    }
    
       
    public void init(MBServerPolicyType mbpt) {
        
        // get the init information from configuration
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Setting up MBeanServer ");
        }
        
        mbs = MBeanServerFactory.createMBeanServer(JMXUtils.DOMAIN_STRING);            
        mcf = MBServerConnectorFactory.getInstance();
        mcf.setMBeanServer(mbs);
        mcf.setThreaded(mbpt.getJMXConnector().isThreaded());
        mcf.setDaemon(mbpt.getJMXConnector().isDaemon());
        mcf.setServiceUrl(mbpt.getJMXConnector().getJMXServiceURL());
        try {            
            mcf.createConnector();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "START_CONNECTOR_FAILURE_MSG", new Object[]{ex});
        }        
        
    }
    
    public MBeanServer getMBeanServer() {
        return mbs;
    }
    
    public void shutdown() { 
        if (!platformMBeanServer) {
            try {
                mcf.destroy();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "STOP_CONNECTOR_FAILURE_MSG", new Object[]{ex});
            }
        }
    }
   
    public void registerMBean(Object object, ObjectName name) {        
        try {
            mbs.registerMBean(object, name);           
        } catch (InstanceAlreadyExistsException e) {            
            try { 
                mbs.unregisterMBean(name);                
                mbs.registerMBean(object, name);
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e1});
            }
        } catch (MBeanRegistrationException e) {
            LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e});          
        } catch (NotCompliantMBeanException e) {
            LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e});
        }
    }
    
    public void unregisterMBean(ObjectName name) {      
        try {
            mbs.unregisterMBean(name);            
        } catch (JMException e) {
            LOG.log(Level.SEVERE, "UNREGISTER_FAILURE_MSG", new Object[]{name, e});
        }
    }
    
    /**
     * Create the related JMX component and register it.
     * @param instrumentation
     */
    public void registerMBean(Instrumentation instrumentation) {
        ModelMBeanInfo mbi = mbAssembler.getModelMbeanInfo(instrumentation.getClass());
        
        if (mbi != null) {                    
            RequiredModelMBean rtMBean;
            try {
                                        
                rtMBean = (RequiredModelMBean)mbs.instantiate(
                    "javax.management.modelmbean.RequiredModelMBean");
                               
                rtMBean.setModelMBeanInfo(mbi);
                
                rtMBean.setManagedResource(instrumentation, "ObjectReference");
                                       
                registerMBean(rtMBean,
                    JMXUtils.getObjectName(instrumentation.getInstrumentationName(),
                                           instrumentation.getUniqueInstrumentationName()));
                                       
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("registered the object to MBserver " 
                                       + instrumentation.getUniqueInstrumentationName());
                } 
                    
                   
            } catch (ReflectionException e) {
                LOG.log(Level.SEVERE, "INSTANTIANTE_FAILURE_MSG", new Object[]{e});
            } catch (MBeanException e) {
                LOG.log(Level.SEVERE, "MBEAN_FAILURE_MSG", new Object[]{e});
            } catch (InstanceNotFoundException e) {
                LOG.log(Level.SEVERE, "SET_MANAGED_RESOURCE_FAILURE_MSG", new Object[]{e});
            } catch (InvalidTargetObjectTypeException e) {
                LOG.log(Level.SEVERE, "SET_MANAGED_RESOURCE_FAILURE_MSG", new Object[]{e});
            }
        } else {
            LOG.log(Level.SEVERE, "GET_MANAGED_INFORMATION_FAILURE_MSG", 
                    new Object[]{instrumentation.getInstrumentationName()});
        }                
    }
    
    /**
     * Identify the related JMX component and unregister it.
     * @param instrumentation
     */
    public void unregisterMBean(Instrumentation instrumentation) {
        ObjectName name;                 
        name = JMXUtils.getObjectName(instrumentation.getInstrumentationName(),
            instrumentation.getUniqueInstrumentationName());               
        unregisterMBean(name);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("unregistered the object to MBserver" 
                               + instrumentation.getUniqueInstrumentationName());
        }   
    }
    

}

