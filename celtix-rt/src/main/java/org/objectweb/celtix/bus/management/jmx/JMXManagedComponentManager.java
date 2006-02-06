package org.objectweb.celtix.bus.management.jmx;


import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.management.InstrumentationEventFilter;
import org.objectweb.celtix.bus.management.InstrumentationEventListener;
import org.objectweb.celtix.management.Instrumentation;


/***
 * The manager class for the JMXManagedComponent which host the JMXManagedComponent
 * It implemenated the ManagementEventListener for the managed component register and unregister
 */
public class JMXManagedComponentManager implements InstrumentationEventListener {
    private static final Logger LOG = Logger.getLogger(JMXManagedComponentManager.class.getName());
   
    private InstrumentationEventFilter meFilter;
    private MBeanServer mbs;    
    private final Map<String, JMXManagedComponentFactory> componentFactories;
    
    
    
    public JMXManagedComponentManager() {
        meFilter = new InstrumentationEventFilter();
        mbs = ManagementFactory.getPlatformMBeanServer();
        componentFactories = new ConcurrentHashMap<String, JMXManagedComponentFactory>();
       // TODO need to read configurate files        
    }
    
    public InstrumentationEventFilter getManagementEventFilter() {
        return meFilter;
    }
    
    // setup the factories for component creatation
    public void loadFactories() {
        try {
            loadJMXManagedComponentFactroy(
                 "org.objectweb.celtix.bus.management.jmx.model.WorkQueueComponentFactory",
                 "WorkQueue");
            loadJMXManagedComponentFactroy(
                 "org.objectweb.celtix.bus.management.jmx.model.HTTPClientTransportComponentFactory",
                 "HTTPClientTransport");
            loadJMXManagedComponentFactroy(
                 "org.objectweb.celtix.bus.management.jmx.model.HTTPServerTransportComponentFactory",
                 "HTTPServerTransport");
        } catch (BusException be) {
            be.printStackTrace();
        }    

    }
    
    public void loadJMXManagedComponentFactroy(String classname, String objectname) throws BusException { 
        try {
            Class<? extends JMXManagedComponentFactory> clazz =
                Class.forName(classname).asSubclass(JMXManagedComponentFactory.class);    
            JMXManagedComponentFactory factory = clazz.newInstance();
            // setup information factory.init(bus);
            registerJMXManagedComponentFactory(objectname, factory);
        } catch (ClassNotFoundException e) {
            throw new BusException(e);
        } catch (InstantiationException e) {
            throw new BusException(e);
        } catch (IllegalAccessException e) {
            throw new BusException(e);
        }
    }
    
    public void registerMBean(Object object, ObjectName name) {        
        try {
            mbs.registerMBean(object, name);
            onRegister(name);
        } catch (InstanceAlreadyExistsException e) {            
            LOG.log(Level.SEVERE, "Object" + name + " InstanceAlreadyExistsException", e);
        } catch (MBeanRegistrationException e) {
            LOG.log(Level.SEVERE, "Object" + name + " MBeanRegistrationException", e);           
        } catch (NotCompliantMBeanException e) {
            LOG.log(Level.SEVERE, "Object" + name + "NotCompliantMBeanException", e);
        }
    }
    
    public void unregisterMBean(ObjectName name) {
        try {
            mbs.unregisterMBean(name);
            onUnregister(name);
        } catch (JMException e) {
            LOG.log(Level.SEVERE, "Object" + name + "JMException", e);
        }
    }
     
    
    protected void onRegister(ObjectName objectName) {        
    }
    
    protected void onUnregister(ObjectName objectName) {        
    }
    
    
    // find out the related JMX managed component do register and unregister things
    public void processEvent(BusEvent event) throws BusException {
        try {
            if (meFilter.isEventEnabled(event)) {
                Instrumentation instrumentation = (Instrumentation) event.getSource();
                if (meFilter.isCreateEvent(event)) {
                    JMXManagedComponentFactory factory = 
                        getJMXManagedComponentFactory(
                            instrumentation.getInstrumentationName());                      
                    JMXManagedComponent component = factory.createManagedComponent(instrumentation);
                    mbs.registerMBean(component, component.getObjectName()); 
                }
           
                if (meFilter.isRemovedEvent(event)) {
                    // unregist the component and distroy it.
                    ObjectName name = JMXManagedComponent.getObjectName(
                                         instrumentation.getUniqueInstrumentationName());
                    mbs.unregisterMBean(name);                  
                }  
            }
        } catch (InstanceNotFoundException e) {
            throw new BusException(e);
        } catch (MBeanRegistrationException e) {            
            throw new BusException(e);
        } catch (InstanceAlreadyExistsException e) {
            // the object existed
            System.out.println(" InstanceAlreadyExists " + e.getMessage());
            throw new BusException(e);
        } catch (NotCompliantMBeanException e) {            
            throw new BusException(e);
        } 
    }

    void registerJMXManagedComponentFactory(String name,
        JMXManagedComponentFactory factory) throws BusException {
        componentFactories.put(name, factory);
    }
    
    void deregisterJMXManagedComponentFactory(String name) throws BusException {
        componentFactories.remove(name);
    }    
   
    JMXManagedComponentFactory getJMXManagedComponentFactory(String name) throws BusException {
        return componentFactories.get(name);
    }
  

}
