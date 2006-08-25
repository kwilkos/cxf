package org.objectweb.celtix.jbi.se.state;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;


public interface ServiceEngineStateMachine {

    enum SEOperation {
        start, stop, init, shutdown
    };
    
    void changeState(SEOperation operation, ComponentContext context) throws JBIException;
    
}
