package org.objectweb.celtix.jbi.se.state;

import java.util.logging.Logger;

import javax.jbi.JBIException;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStop implements ServiceEngineStateMachine {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineStop.class);
    
    public void changeState(SEOperation operation) throws JBIException {
        LOG.info("in stop state");
        if (operation == SEOperation.start) {
            ServiceEngineStateFactory.getInstance().setCurrentState(
                ServiceEngineStateFactory.getInstance().getStartState());
        } else if (operation == SEOperation.shutdown) {
            ServiceEngineStateFactory.getInstance().setCurrentState(
                ServiceEngineStateFactory.getInstance().getShutdownState());
        } else if (operation == SEOperation.init) {
            throw new JBIException("This operation is unsupported, cannot init a stopped JBI component");
        } else if (operation == SEOperation.stop) {
            throw new JBIException("Cannot stop a JBI component which is already stopped");
        }
    }

}
