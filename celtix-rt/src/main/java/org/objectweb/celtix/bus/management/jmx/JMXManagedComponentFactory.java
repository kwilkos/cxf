package org.objectweb.celtix.bus.management.jmx;

import org.objectweb.celtix.management.Instrumentation;



/**
 *  The Managed components instants factory to create the ManagedComponent
 *  Which can setup the message for it.
 */
public interface JMXManagedComponentFactory {
    JMXManagedComponent createManagedComponent(Instrumentation i);
 
}
