package org.objectweb.celtix.jbi.se.state;

import java.util.logging.Logger;

import javax.jbi.JBIException;

import javax.jbi.component.ComponentContext;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStart extends AbstractServiceEngineStateMachine {

    
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineStart.class);
    
    
    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        LOG.info("in start state");
        if (operation == SEOperation.stop) {
            ServiceEngineStateFactory.getInstance().setCurrentState(
                ServiceEngineStateFactory.getInstance().getStopState());
        } else if (operation == SEOperation.start) {
            throw new JBIException("This JBI component is already started");
        } else if (operation == SEOperation.init) {
            throw new JBIException("This operation is unsupported, cannot init a started JBI component");
        } else if (operation == SEOperation.shutdown) {
            throw new JBIException("Cannot shutdown a started JBI component directly");
        }
    }

}
