package org.objectweb.celtix.jbi;

import javax.jbi.component.ComponentContext;

/**
 * Defines a service consumer to be started by the Celtix Service
 * Engine.
 * 
 */
public interface ServiceConsumer extends Runnable { 

    void stop();
    void setComponentContext(ComponentContext componentCtx);

} 
