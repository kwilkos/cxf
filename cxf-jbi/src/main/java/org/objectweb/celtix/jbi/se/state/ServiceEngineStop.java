package org.objectweb.celtix.jbi.se.state;

import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;



import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStop extends AbstractServiceEngineStateMachine {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineStop.class);
    
    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        LOG.info("in stop state");
        if (operation == SEOperation.start) {
            startSE();
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

    private void startSE() throws JBIException {
        try {
            if (ctx == null) {
                return;
            }
            DeliveryChannel chnl = ctx.getDeliveryChannel();
            configureJBITransportFactory(chnl, suManager); 
            LOG.info(new Message("SE.STARTUP.COMPLETE", LOG).toString());
            
        } catch (Throwable e) {
            throw new JBIException(e);
        }
    }

}
