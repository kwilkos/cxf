package org.objectweb.celtix.bus.management.jmx;



import java.io.IOException;
import java.lang.management.ManagementFactory;
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

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.management.InstrumentationEventFilter;
import org.objectweb.celtix.bus.management.InstrumentationEventListener;
import org.objectweb.celtix.bus.management.jmx.export.runtime.ModelMBeanAssembler;
import org.objectweb.celtix.bus.management.jmx.export.runtime.RunTimeModelMBean;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.management.Instrumentation;


/***
 * The manager class for the JMXManagedComponent which host the JMXManagedComponent
 * It implemenated the ManagementEventListener for the managed component register and unregister
 */


public class JMXManagedComponentManager implements InstrumentationEventListener {
    private static final Logger LOG = LogUtils.getL7dLogger(JMXManagedComponentManager.class);
    
    private boolean platformMBeanServer = true;
    //private Bus bus;
    private InstrumentationEventFilter meFilter;
    private MBeanServer mbs;
    private ModelMBeanAssembler mbAssembler; 
    private MBServerConnectorFactory mcf;
    
    public JMXManagedComponentManager(Bus b) {
        //bus = b;
        
        meFilter = new InstrumentationEventFilter();
        // TODO need to read configurate files  
        mbAssembler = new ModelMBeanAssembler();
        
       
    }
    
    public void init() {
                
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Setting up MBeanServer ");
        }          
           
        if (platformMBeanServer) {
            mbs = ManagementFactory.getPlatformMBeanServer();
        } else {
            // TODO get the configuration and setup the ConnectorFactory
            mbs = MBeanServerFactory.createMBeanServer(JMXUtils.DOMAIN_STRING);            
            mcf = new MBServerConnectorFactory();
            mcf.setMBeanServer(mbs);
            mcf.setThreaded(true);
            try {            
                mcf.createConnector();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "START_CONNECTOR_FAILURE_MSG", new Object[]{ex});
            }
        }
        
       
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
    
    public InstrumentationEventFilter getManagementEventFilter() {
        return meFilter;
    }
   
    public void registerMBean(Object object, ObjectName name) {        
        try {
            onRegister(name);
            mbs.registerMBean(object, name);           
        } catch (InstanceAlreadyExistsException e) {            
            LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e});
        } catch (MBeanRegistrationException e) {
            LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e});          
        } catch (NotCompliantMBeanException e) {
            LOG.log(Level.SEVERE, "REGISTER_FAILURE_MSG", new Object[]{name, e});
        }
    }
    
    public void unregisterMBean(ObjectName name) {
        
        try {
            onUnregister(name);
            mbs.unregisterMBean(name);            
        } catch (JMException e) {
            LOG.log(Level.SEVERE, "UNREGISTER_FAILURE_MSG", new Object[]{name, e});
        }
    }
     
    
    protected void onRegister(ObjectName objectName) { 
        
    }
    
    protected void onUnregister(ObjectName objectName) { 
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("unregistered the object to MBserver" 
                               + objectName);
        }   
    }
    
    
    // find out the related JMX managed component do register and unregister things   
     
    public void processEvent(BusEvent event) throws BusException {
        if (meFilter.isEventEnabled(event)) {
            Instrumentation instrumentation = (Instrumentation) event.getSource();
                
            if (meFilter.isCreateEvent(event)) {
                    
                ModelMBeanInfo mbi = mbAssembler.getModelMbeanInfo(instrumentation.getClass());
                
                if (mbi != null) {
                    RunTimeModelMBean rtMBean;
                    try {
                        rtMBean = (RunTimeModelMBean)mbs.instantiate(
                            "org.objectweb.celtix.bus.management.jmx.export.runtime.RunTimeModelMBean");
                                       
                        rtMBean.setModelMBeanInfo(mbi);
                        
                        rtMBean.setManagedResource(instrumentation, "ObjectReference");
                                               
                        registerMBean(rtMBean,
                            JMXUtils.getObjectName(instrumentation.getUniqueInstrumentationName()));
                        if (LOG.isLoggable(Level.INFO)) {
                            LOG.info("registered the object to MBserver" 
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

           
            if (meFilter.isRemovedEvent(event)) {
               // unregist the component and distroy it.
                ObjectName name;                 
                name = JMXUtils.getObjectName(
                    instrumentation.getUniqueInstrumentationName());               
                unregisterMBean(name);
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("unregistered the object to MBserver" 
                                       + instrumentation.getUniqueInstrumentationName());
                }   
                    
            }  
        }       
    }

   

}
