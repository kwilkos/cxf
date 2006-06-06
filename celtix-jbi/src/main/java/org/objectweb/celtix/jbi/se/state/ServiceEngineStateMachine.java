package org.objectweb.celtix.jbi.se.state;

import javax.jbi.JBIException;

public interface ServiceEngineStateMachine {

    enum SEOperation {
        start, stop, init, shutdown
    };
    
    void changeState(SEOperation operation) throws JBIException;
    
}
