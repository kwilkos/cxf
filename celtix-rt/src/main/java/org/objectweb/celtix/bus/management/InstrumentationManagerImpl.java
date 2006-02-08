package org.objectweb.celtix.bus.management;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.bus.management.jmx.JMXManagedComponentManager;
import org.objectweb.celtix.bus.transports.http.AbstractHTTPServerTransport;
import org.objectweb.celtix.bus.transports.http.HTTPClientTransport;
import org.objectweb.celtix.bus.transports.http.HTTPClientTransportInstrumentation;
import org.objectweb.celtix.bus.transports.http.HTTPServerTransportInstrumentation;
import org.objectweb.celtix.bus.workqueue.WorkQueueInstrumentation;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.InstrumentationManager;


/** The basic manager information center for common management model
 *  Instrumentation components will be registed to InstrumenationManager.
 *  The Instrumentation mananger will send event to notifier the management 
 *  layer to expose the managed component. 
 *  Instrumentation manager also provider a qurey interface for the instrumentation.
 *  The JMX layer could query the detail information for instrumentation.
 */
public class InstrumentationManagerImpl implements InstrumentationManager, BusEventListener {    
    static final Logger LOG = Logger.getLogger(InstrumentationManagerImpl.class.getName());
    private Bus bus;
    private List <Instrumentation> instrumentations;
    private JMXManagedComponentManager jmxManagedComponentManager;
    private ComponentEventFilter componentEventFilter;
    
    public InstrumentationManagerImpl(Bus b) throws BusException {
        bus = b;
        instrumentations = new LinkedList<Instrumentation>();
        componentEventFilter = new ComponentEventFilter();
        // need to register the listeners
        // configurat to listener        
        jmxManagedComponentManager = new JMXManagedComponentManager();
        jmxManagedComponentManager.loadFactories();
        bus.addListener((BusEventListener)jmxManagedComponentManager, 
                        jmxManagedComponentManager.getManagementEventFilter());
        
        bus.addListener((BusEventListener)this, 
                        componentEventFilter);
    }
    
    
    public void regist(Instrumentation is) {
        instrumentations.add(is);        
        //create the instrumentation creation event        
        bus.sendEvent(new InstrumentationCreatedEvent(is));
        
    }

    public void unregist(Object component) {
        for (Iterator<Instrumentation> i = instrumentations.iterator(); i.hasNext();) {
            Instrumentation it = i.next();
            if (it.getComponent() == component) {
                i.remove();                
            }
                    
            if (it != null) {
                //create the instrumentation remove event           
                bus.sendEvent(new InstrumentationRemovedEvent(it));               
            }
        }
    }

    // get the instance and create the right component
    public void processEvent(BusEvent e) throws BusException {
        Instrumentation it;
        if (e.getID().equals(ComponentCreatedEvent.COMPONENT_CREATED_EVENT)) {
            Class<?> source = e.getSource().getClass();
            if (WorkQueueManagerImpl.class.isAssignableFrom(source)) {
                it = new WorkQueueInstrumentation(
                              (WorkQueueManagerImpl)e.getSource());
                regist(it);
            }
            if (HTTPClientTransport.class.isAssignableFrom(source)) {
                it = new HTTPClientTransportInstrumentation(
                              (HTTPClientTransport)e.getSource());
                regist(it);
            }
            if (AbstractHTTPServerTransport.class.isAssignableFrom(source)) {
                it = new HTTPServerTransportInstrumentation(
                               (AbstractHTTPServerTransport)e.getSource());
                regist(it);
            }                
            
        } else if (e.getID().equals(ComponentRemovedEvent.COMPONENT_REMOVED_EVENT)) {           
            unregist(e.getSource());
        }
    }

   
    public List<Instrumentation> getAllInstrumentation() {
        // TODO need to add more qurey interface
        return instrumentations;
    }
   

}
