package org.objectweb.celtix.jbi.se.state;

import java.util.logging.Logger;

import javax.jbi.JBIException;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineShutdown implements ServiceEngineStateMachine {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineShutdown.class);
    
    public void changeState(SEOperation operation) throws JBIException {
        LOG.info("in shutdown state");
        if (operation == SEOperation.init) {
            ServiceEngineStateFactory.getInstance().setCurrentState(
                ServiceEngineStateFactory.getInstance().getStopState());
        } else if (operation == SEOperation.shutdown) {
            throw new JBIException("This JBI component is already shutdown");
        } else if (operation == SEOperation.stop) {
            throw new JBIException("This operation is unsupported, cannot stop a shutdown JBI component");
        } else if (operation == SEOperation.start) {
            throw new JBIException("Cannot start a shutdown JBI component directly, need init first");
        }
    }

}
