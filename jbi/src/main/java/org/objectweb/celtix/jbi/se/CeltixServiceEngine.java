package org.objectweb.celtix.jbi.se;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.state.AbstractServiceEngineStateMachine;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateFactory;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine;


/** A JBI component.  Initializes the Celtix JBI transport
 */
public class CeltixServiceEngine implements ComponentLifeCycle, Component {
    
    public static final String JBI_TRANSPORT_ID = "http://celtix.object.org/transport/jbi";
    
    
    
    private static final Logger LOG = LogUtils.getL7dLogger(CeltixServiceEngine.class);
    
    
   
    private ServiceEngineStateFactory stateFactory = ServiceEngineStateFactory.getInstance();
    
    public CeltixServiceEngine() {
        stateFactory.setCurrentState(stateFactory.getShutdownState());
    }
    
    // Implementation of javax.jbi.component.ComponentLifeCycle
    
    public final ObjectName getExtensionMBeanName() {
        return null;
    }
    
    public final void shutDown() throws JBIException {
        try {
            LOG.fine(new Message("SE.SHUTDOWN", LOG).toString());
            stateFactory.getCurrentState().changeState(ServiceEngineStateMachine.SEOperation.shutdown, null);
        } catch (Throwable ex) { 
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new JBIException(ex);
        } 
    }
    
    public final void init(ComponentContext componentContext) throws JBIException {
        try {
            stateFactory.getCurrentState().changeState(
                ServiceEngineStateMachine.SEOperation.init, componentContext);
        } catch (Throwable ex) { 
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw new JBIException(ex);
        } 
    }
    
    
    
    
    
    public final void start() throws JBIException {
        try { 
            LOG.fine(new Message("SE.STARTUP", LOG).toString());
            stateFactory.getCurrentState().changeState(ServiceEngineStateMachine.SEOperation.start, null);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new JBIException(ex);
        }
    }
    
    public final void stop() throws JBIException {
        try {
            LOG.fine(new Message("SE.STOP", LOG).toString());
            stateFactory.getCurrentState().changeState(ServiceEngineStateMachine.SEOperation.stop, null);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new JBIException(ex);
        }
    }
    
    // Implementation of javax.jbi.component.Component
    
    public final ComponentLifeCycle getLifeCycle() {
        LOG.fine("CeltixServiceEngine returning life cycle");
        return this;
    }
    
    public final ServiceUnitManager getServiceUnitManager() {
        LOG.fine("CeltixServiceEngine return service unit manager");
        return AbstractServiceEngineStateMachine.getSUManager();
    }
    
    public final Document getServiceDescription(final ServiceEndpoint serviceEndpoint) {
        Document doc = 
            AbstractServiceEngineStateMachine.getSUManager().getServiceDescription(serviceEndpoint);
        LOG.fine("CeltixServiceEngine returning service description: " + doc);
        return doc;
    }
    
    public final boolean isExchangeWithConsumerOkay(final ServiceEndpoint ep, 
                                                    final MessageExchange exchg) {
        
        LOG.fine("isExchangeWithConsumerOkay: endpoint: " + ep 
                 + " exchange: " + exchg);
        return true;
    }
    
    public final boolean isExchangeWithProviderOkay(final ServiceEndpoint ep, 
                                                    final MessageExchange exchng) {
        LOG.fine("isExchangeWithConsumerOkay: endpoint: " + ep 
                 + " exchange: " + exchng);
        return true;
    }
    
    public final ServiceEndpoint resolveEndpointReference(final DocumentFragment documentFragment) {
        return null;
    }
    
    
    
    
    
}
