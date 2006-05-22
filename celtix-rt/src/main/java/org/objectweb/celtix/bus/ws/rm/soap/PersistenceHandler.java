package org.objectweb.celtix.bus.ws.rm.soap;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.bus.ws.rm.ConfigurationHelper;
import org.objectweb.celtix.bus.ws.rm.DestinationSequence;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.PersistenceManager;
import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.bus.ws.rm.RMDestination;
import org.objectweb.celtix.bus.ws.rm.RMHandler;
import org.objectweb.celtix.bus.ws.rm.RMMessageImpl;
import org.objectweb.celtix.bus.ws.rm.RMPropertiesImpl;
import org.objectweb.celtix.bus.ws.rm.RMSource;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.RetransmissionQueue;
import org.objectweb.celtix.bus.ws.rm.SourceSequence;
import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreFactory;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;


public class PersistenceHandler implements SOAPHandler<SOAPMessageContext>,
    PersistenceManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(PersistenceHandler.class);
    
    @Resource(name = JAXWSConstants.CLIENT_BINDING_PROPERTY) ClientBinding clientBinding;
    @Resource(name = JAXWSConstants.SERVER_BINDING_PROPERTY) ServerBinding serverBinding;
    @Resource(name = JAXWSConstants.CLIENT_TRANSPORT_PROPERTY) ClientTransport clientTransport;
    @Resource(name = JAXWSConstants.SERVER_TRANSPORT_PROPERTY) ServerTransport serverTransport;
    
    private RMStore store;
    private RetransmissionQueue retransmissionQueue;
    private ConfigurationHelper configurationHelper;
    
    @PostConstruct
    public synchronized void initialise() {
        RMHandler handler = RMHandler.getHandlerMap().get(getBinding());  
        handler.setPersistenceManager(this);
        LOG.fine("Set persistence manager.");
     
        configurationHelper = new ConfigurationHelper(getBinding(), null == clientBinding);
        
        store = new RMStoreFactory().getStore(configurationHelper.getConfiguration());            
        
        retransmissionQueue = new RetransmissionQueueImpl(this, configurationHelper.getRMAssertion());
             
        restore();
    }
    
    @PreDestroy
    public void shutdown() {
        getQueue().stop();
    }
    
    // --- SOAPHandler interface ---
 
    public Set<QName> getHeaders() {
        return Names.HEADERS;
    }

    public void close(MessageContext arg0) {
    }

    public boolean handleFault(SOAPMessageContext context) {
        initQueue();
        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        }
        return true;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        initQueue();
        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        }
        return true;
    }
    
    // --- PersistenceHandler interface

    public ConfigurationHelper getConfigurationHelper() {
        return configurationHelper;
    }
    
    public RetransmissionQueue getQueue() {
        return retransmissionQueue;
    }

    public RMStore getStore() {
        return store;
    }
    
    // --- protected or private ---
       
    void handleOutbound(SOAPMessageContext context) {
        LOG.entering(getClass().getName(), "handleOutbound");
        // do nothing unless this is an application message        
        
        if (!isApplicationMessage(context)) {
            return; 
        }
        
        // tell the source to store a copy of the message in the
        // retransmission queue
        // and schedule the next retransmission
        
        RMPropertiesImpl rmpsOut = 
            (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, true);
        
        if (null == rmpsOut) {
            // handler chain traversal may have been reversed before
            // reaching RM logical handler - OK to ignore?
            return;
        }
        
        if (null == rmpsOut.getSequence()) {
            // cannot be an application message (may be a partial response)
            return;
        }
        
        BigInteger mn = rmpsOut.getSequence().getMessageNumber();
        boolean lm = null != rmpsOut.getSequence().getLastMessage();
        Identifier sid = rmpsOut.getSequence().getIdentifier();        
        
        // create a new SourceSequence object instead of retrieving the one
        // maintained by the RM source for the sequence identifier 
        // as the current/last message number properties of the latter may have 
        // changed since
        
        SourceSequence seq = new SourceSequence(sid, null, null, mn, lm);         
        RMMessageImpl msg = new RMMessageImpl(mn, context);
        cacheUnacknowledged(seq, msg);
    }    
    
    void restore() {        
        if (null == store) {
            return;
        }
        
        String endpointId = configurationHelper.getEndpointId();
        
        Collection<RMSourceSequence> sss = store.getSourceSequences(endpointId);
        
        // Don't make any of these sequences the current sequence, thus forcing
        // termination of the recovered sequences as soon as possible
        
        RMHandler handler = RMHandler.getHandlerMap().get(getBinding());
        RMSource source = handler.getSource();
        for (RMSourceSequence ss : sss) {
            source.addSequence((SourceSequence)ss, false);
        }
        
        retransmissionQueue.populate(source.getAllSequences());
        if (!retransmissionQueue.isEmpty()) {
            initQueue();
        }   
        
        RMDestination destination = handler.getDestination();
        Collection<RMDestinationSequence> dss = store.getDestinationSequences(endpointId);
        for (RMDestinationSequence ds : dss) {
            destination.addSequence((DestinationSequence)ds, false);
        }
    }
    
    public void cacheUnacknowledged(SourceSequence seq, RMMessage msg) {

        ObjectMessageContext clone = getBinding().createObjectContext();
        clone.putAll(msg.getContext());
        getQueue().addUnacknowledged(clone);
        if (null != getStore()) {
            getStore().persistOutgoing(seq, msg);
        }

    }
   
    AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return (AbstractBindingBase)clientBinding;
        }
        return (AbstractBindingBase)serverBinding;
    }
    
    ClientTransport getClientTransport() {
        return clientTransport;
    }
    
    ServerTransport getServerTransport() {
        return serverTransport;
    }
    
    private boolean isApplicationMessage(SOAPMessageContext context) {
        boolean isApplicationMessage = true;
        AddressingPropertiesImpl maps =
            ContextUtils.retrieveMAPs(context, false, true);
        
        if (null == maps) {
            return false;
        }
      
        // ensure the appropriate version of WS-Addressing is used       
        maps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);

        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)
            || RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getLastMessageAction().equals(action)
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)
            || RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
            isApplicationMessage = false;
        }
        return isApplicationMessage;
    }
    
    void initialise(ConfigurationHelper ch, RMStore s, RetransmissionQueue q) {
        configurationHelper = ch;
        store = s;
        retransmissionQueue = q;
    }
    
    private void initQueue() {
        getQueue().start(getBinding().getBus().getWorkQueueManager()
                         .getAutomaticWorkQueue());
    }
    
}
