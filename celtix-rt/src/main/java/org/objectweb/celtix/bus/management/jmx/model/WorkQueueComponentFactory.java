package org.objectweb.celtix.bus.management.jmx.model;

import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.management.jmx.JMXManagedComponentFactory;
import org.objectweb.celtix.bus.workqueue.WorkQueueInstrumentation;
import org.objectweb.celtix.management.Instrumentation;

public class WorkQueueComponentFactory implements JMXManagedComponentFactory {

    public JMXManagedComponent createManagedComponent(Instrumentation i) {
        //check the Instrumentation object type
        WorkQueueInstrumentation wqi = (WorkQueueInstrumentation) i;
        return new WorkQueueComponent(wqi);
        
    }

}
