package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.BindingBase;
import org.objectweb.celtix.bindings.BindingContextUtils;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.ServerBinding;

import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.SystemHandler;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.RelatesToType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.TerminateSequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMHandler implements LogicalHandler<LogicalMessageContext>, SystemHandler {

    public static final String RM_CONFIGURATION_URI = "http://celtix.objectweb.org/bus/ws/rm/rm-config";
    public static final String RM_CONFIGURATION_ID = "rm-handler";

    private static final Logger LOG = LogUtils.getL7dLogger(RMHandler.class);
    private static Map<BindingBase, RMHandler> handlers;

    private RMSource source;
    private RMDestination destination;
    
    private RMProxy proxy;
    private RMServant servant;
    private ConfigurationHelper configurationHelper;
    private PersistenceManager persistenceManager;
    private Timer timer;
    private boolean busLifeCycleListenerRegistered;
      
    @Resource(name = JAXWSConstants.BUS_PROPERTY) private Bus bus;
    @Resource(name = JAXWSConstants.CLIENT_BINDING_PROPERTY) private ClientBinding clientBinding;
    @Resource(name = JAXWSConstants.SERVER_BINDING_PROPERTY) private ServerBinding serverBinding;
    @Resource(name = JAXWSConstants.CLIENT_TRANSPORT_PROPERTY) private ClientTransport clientTransport;
    @Resource(name = JAXWSConstants.SERVER_TRANSPORT_PROPERTY) private ServerTransport serverTransport;

    public RMHandler() {        
        proxy = new RMProxy(this);
        servant = new RMServant();
    }
    
    /**
     * Rely on order of resource injection and invocation of @postConstruct annotated methods
     * for system handlers: pre-logical, post-logical, pre-protocol, post-protocol (i.e. assume 
     * that the logical rm handler is already fully initialised).
     *
     */
    
    @PostConstruct
    protected synchronized void initialise() {
        
        getHandlerMap().put(getBinding(), this);
        
        if (null == configurationHelper) {
            configurationHelper = new ConfigurationHelper(getBinding(), null == clientBinding);
        } 
        
        if (null == getSource()) {
            source = new RMSource(this);            
        }
        
        if (null == destination) {
            destination = new RMDestination(this);
        }
        
        if (null == timer) {
            timer = new Timer();
        }
        
        if (!busLifeCycleListenerRegistered) {
            getBinding().getBus().getLifeCycleManager()
                .registerLifeCycleListener(new RMBusLifeCycleListener(getSource()));
            busLifeCycleListenerRegistered = true;
        }
    }
    
    public static Map<BindingBase, RMHandler> getHandlerMap() {
        if (null == handlers) {
            handlers = new HashMap<BindingBase, RMHandler>();
        }
        return handlers;
    }

    public void close(MessageContext context) {
        // TODO commit transaction
    }

    public boolean handleFault(LogicalMessageContext context) {
        return false;
    }

    public boolean handleMessage(LogicalMessageContext context) {
        try {
            if (ContextUtils.isOutbound(context)) {
                handleOutbound(context);
            } else {
                handleInbound(context);
            }
        } catch (SequenceFault sf) {
            sf.printStackTrace();
            LOG.log(Level.SEVERE, "SequenceFault", sf);
        }
        return true;
    }
    
    public ConfigurationHelper getConfigurationHelper() {
        return configurationHelper;
    }
    
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    public void setPersistenceManager(PersistenceManager pm) {
        persistenceManager = pm;
    }
    
    public RMStore getStore() {
        if (null != persistenceManager) {
            return persistenceManager.getStore();
        }
        return null;
    }

    public Timer getTimer() {
        return timer;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public Transport getTransport() {
        return null == clientTransport ? serverTransport : clientTransport;
    }

    public ClientTransport getClientTransport() {
        return clientTransport;
    }

    public ServerTransport getServerTransport() {
        return serverTransport;
    }

    public ClientBinding getClientBinding() {
        return clientBinding;
    }

    public ServerBinding getServerBinding() {
        return serverBinding;
    }

    public boolean isServerSide() {
        return null != serverBinding;
    }

    public AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return (AbstractBindingBase)clientBinding;
        }
        return (AbstractBindingBase)serverBinding;
    }

    public RMProxy getProxy() {
        return proxy;
    }
    
    public RMServant getServant() {
        return servant;
    }
    
    public RMSource getSource() {
        return source;        
    }
    
    public RMDestination getDestination() {
        return destination;
    }

    protected void open(LogicalMessageContext context) {
        // TODO begin transaction
    }

    

    protected void handleOutbound(LogicalMessageContext context) throws SequenceFault {
        LOG.entering(getClass().getName(), "handleOutbound");
        AddressingPropertiesImpl maps =
            ContextUtils.retrieveMAPs(context, false, true);
      
        // ensure the appropriate version of WS-Addressing is used       
        maps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);

        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }

        // nothing to do if this is a CreateSequence, TerminateSequence or
        // SequenceInfo request

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }

        boolean isApplicationMessage = true;
        
        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)
            || RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getLastMessageAction().equals(action)
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)
            || RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
            isApplicationMessage = false;
        }
        
        RMPropertiesImpl rmpsOut = (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, true);
        if (null == rmpsOut) {
            rmpsOut = new RMPropertiesImpl();
            RMContextUtils.storeRMProperties(context, rmpsOut, true);
        }
        
        RMPropertiesImpl rmpsIn = null;
        Identifier inSeqId = null;
        BigInteger inMessageNumber = null;
        
        if (isApplicationMessage) {
                        
            rmpsIn = (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, false);
            
            if (null != rmpsIn && null != rmpsIn.getSequence()) {
                inSeqId = rmpsIn.getSequence().getIdentifier();
                inMessageNumber = rmpsIn.getSequence().getMessageNumber();
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("inbound sequence: " + (null == inSeqId ? "null" : inSeqId.getValue()));
            }

            // not for partial responses to oneway requests

            if (!(isServerSide() && BindingContextUtils.isOnewayTransport(context))) {

                if (!ContextUtils.isRequestor(context)) {
                    assert null != inSeqId;
                }
                
                // get the current sequence, requesting the creation of a new one if necessary
                
                SourceSequence seq = getSequence(inSeqId, context, maps);
                assert null != seq;

                // increase message number and store a sequence type object in
                // context

                seq.nextMessageNumber(inSeqId, inMessageNumber);
                rmpsOut.setSequence(seq);

                // if this was the last message in the sequence, reset the
                // current sequence so that a new one will be created next 
                // time the handler is invoked

                if (seq.isLastMessage()) {
                    source.setCurrent(null);
                }
            }
        }
        
        // add Acknowledgements (to application messages or explicitly 
        // created Acknowledgement messages only)

        if (isApplicationMessage 
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)) {
            AttributedURI to = VersionTransformer.convert(maps.getTo());
            assert null != to;
            addAcknowledgements(rmpsOut, inSeqId, to);
        }

        // indicate to the binding that a response is expected from the transport although
        // the web method is a oneway method

        if (BindingContextUtils.isOnewayMethod(context)
            || RMUtils.getRMConstants().getLastMessageAction().equals(action)) {
            context.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, Boolean.FALSE);
        }
    }

    protected void handleInbound(LogicalMessageContext context) throws SequenceFault {

        LOG.entering(getClass().getName(), "handleInbound");
        RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
        
        final AddressingPropertiesImpl maps = ContextUtils.retrieveMAPs(context, false, false);
        assert null != maps;

        String action = null;
        if (null != maps.getAction()) {
            action = maps.getAction().getValue();
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }

        if (RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)) {
            Object[] parameters = (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
            CreateSequenceResponseType csr = (CreateSequenceResponseType)parameters[0];
            getServant().createSequenceResponse(getSource(), 
                                                csr,
                                                getProxy().getOfferedIdentifier());

            return;
        } else if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)) {
            Object[] parameters = (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
            CreateSequenceType cs = (CreateSequenceType)parameters[0];

            final CreateSequenceResponseType csr =
                getServant().createSequence(getDestination(), cs, maps);
            
            Runnable response = new Runnable() {
                public void run() {
                    try {
                        getProxy().createSequenceResponse(maps, csr);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (SequenceFault sf) {
                        sf.printStackTrace();
                    }
                }
            };
            getBinding().getBus().getWorkQueueManager().getAutomaticWorkQueue().execute(response);
    
            return;
        } else if (RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)) {
            Object[] parameters = (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
            TerminateSequenceType cs = (TerminateSequenceType)parameters[0];

            getServant().terminateSequence(getDestination(), cs.getIdentifier());
        }
        
        // for application AND out of band messages

        if (null != rmps) {            
            
            processAcknowledgments(rmps);

            processAcknowledgmentRequests(rmps);  
            
            processSequence(rmps, maps);
        }
    }

    private void processAcknowledgments(RMProperties rmps) {
        Collection<SequenceAcknowledgement> acks = rmps.getAcks();
        if (null != acks) {
            for (SequenceAcknowledgement ack : acks) {
                getSource().setAcknowledged(ack);
            }
        }
    }

    private void processSequence(RMProperties rmps, AddressingProperties maps) throws SequenceFault {
        SequenceType s = rmps.getSequence();
        if (null == s) {
            return;
        }   
        getDestination().acknowledge(s, 
            null == maps.getReplyTo() ? null : maps.getReplyTo().getAddress().getValue());
    }

    private void processAcknowledgmentRequests(RMProperties rmps) {
        Collection<AckRequestedType> requested = rmps.getAcksRequested();
        if (null != requested) {
            for (AckRequestedType ar : requested) {
                DestinationSequence seq = getDestination().getSequence(ar.getIdentifier());
                if (null != seq) {
                    seq.scheduleImmediateAcknowledgement();
                } else {
                    LOG.severe("No such sequence.");
                }
            }
        }
    }

    private void addAcknowledgements(RMPropertiesImpl rmpsOut, Identifier inSeqId, AttributedURI to) {

        for (DestinationSequence seq : getDestination().getAllSequences()) {
            if (seq.sendAcknowledgement()
                && ((seq.getAcksTo().getAddress().getValue().equals(RMUtils.getAddressingConstants()
                    .getAnonymousURI()) && AbstractSequenceImpl.identifierEquals(seq.getIdentifier(), 
                                                                                inSeqId))
                    || to.getValue().equals(seq.getAcksTo().getAddress().getValue()))) {
                rmpsOut.addAck(seq);
            } else if (LOG.isLoggable(Level.FINE)) {
                if (!seq.sendAcknowledgement()) {
                    LOG.fine("no need to add an acknowledgements for sequence "
                             + seq.getIdentifier().getValue());
                } else {
                    LOG.fine("sequences acksTo (" + seq.getAcksTo().getAddress().getValue()
                             + ") does not match to (" + to.getValue() + ")");
                }
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            Collection<SequenceAcknowledgement> acks = rmpsOut.getAcks();
            if (null == acks) {
                LOG.fine("No acknowledgements added");
            } else {
                LOG.fine("Added " + acks.size() + " acknowledgements.");
            }
        }
    }
    
    private SourceSequence getSequence(Identifier inSeqId, 
                                 LogicalMessageContext context, 
                                 AddressingPropertiesImpl maps) throws SequenceFault {
        SourceSequence seq = getSource().getCurrent(inSeqId);

        if (null == seq) {
            // TODO: better error handling
            org.objectweb.celtix.ws.addressing.EndpointReferenceType to = null;
            try {
                EndpointReferenceType acksTo = null;
                RelatesToType relatesTo = null;
                if (isServerSide()) {
                    AddressingPropertiesImpl inMaps = ContextUtils
                        .retrieveMAPs(context, false, false);
                    inMaps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
                    acksTo = RMUtils.createReference(inMaps.getTo().getValue());
                    to = inMaps.getReplyTo();    
                    getServant().setUnattachedIdentifier(inSeqId);
                    relatesTo = ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType();
                    DestinationSequence inSeq = getDestination().getSequence(inSeqId);
                    relatesTo.setValue(inSeq != null ? inSeq.getCorrelationID() : null);
                } else {
                    acksTo = VersionTransformer.convert(maps.getReplyTo());
                    // for oneways
                    if (Names.WSA_NONE_ADDRESS.equals(acksTo.getAddress().getValue())) {
                        acksTo = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
                    }
                }

                getProxy().createSequence(getSource(), to, acksTo, relatesTo);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            seq = getSource().awaitCurrent(inSeqId);
            seq.setTarget(to);
        }
        
        return seq;
    }
    
}
