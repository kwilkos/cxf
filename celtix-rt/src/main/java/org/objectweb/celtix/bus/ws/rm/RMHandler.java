package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.BindingContextUtils;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.SystemHandler;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
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
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class RMHandler implements LogicalHandler<LogicalMessageContext>, SystemHandler {

    public static final String RM_CONFIGURATION_URI = "http://celtix.objectweb.org/bus/ws/rm/rm-config";
    public static final String RM_CONFIGURATION_ID = "rm-handler";

    private static final Logger LOG = LogUtils.getL7dLogger(RMHandler.class);

    private RMSource source;
    private RMDestination destination;
    private RMProxy proxy;
    private RMServant servant;
    private Configuration configuration;
    private Timer timer;
    private boolean busLifeCycleListenerRegistered;

    private AbstractClientBinding clientBinding;
    private AbstractServerBinding serverBinding;
    private ClientTransport clientTransport;
    private ServerTransport serverTransport;

    public RMHandler() {        
        proxy = new RMProxy(this);
        servant = new RMServant();
    }

    public void close(MessageContext context) {
        // TODO commit transaction
    }

    public boolean handleFault(LogicalMessageContext context) {

        open(context);
        return false;
    }

    public boolean handleMessage(LogicalMessageContext context) {

        open(context);

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

    @PreDestroy
    public void shutdown() {
        if (null != getSource()) {
            getSource().shutdown();
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    public Timer getTimer() {
        return timer;
    }

    public ClientTransport getClientTransport() {
        return clientTransport;
    }

    public ServerTransport getServerTransport() {
        return serverTransport;
    }

    public AbstractClientBinding getClientBinding() {
        return clientBinding;
    }

    public AbstractServerBinding getServerBinding() {
        return serverBinding;
    }

    public boolean isServerSide() {
        return null != serverBinding;
    }

    public AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return clientBinding;
        }
        return serverBinding;
    }

    public RMProxy getProxy() {
        return proxy;
    }
    
    public RMServant getServant() {
        return servant;
    }
    
    protected RMSource getSource() {
        return source;        
    }
    
    protected RMDestination getDestination() {
        return destination;
    }

    protected void open(LogicalMessageContext context) {

        initialise(context);

        // TODO begin transaction
    }

    protected synchronized void initialise(MessageContext context) {
        if (null == clientTransport && null == serverTransport) {
            clientTransport = BindingContextUtils.retrieveClientTransport(context);
            if (null == clientTransport) {
                serverTransport = BindingContextUtils.retrieveServerTransport(context);
            }
        }
        
        assert null != serverTransport || null != clientTransport;

        if (null == clientBinding && null != clientTransport) {
            clientBinding = (AbstractClientBinding)BindingContextUtils.retrieveClientBinding(context);
        }

        if (null == serverBinding && null != serverTransport) {
            serverBinding = (AbstractServerBinding)BindingContextUtils.retrieveServerBinding(context);
        }
        
        assert null != serverBinding || null != clientBinding;

        if (null == configuration) {
            configuration = createConfiguration(context);
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

    protected Configuration createConfiguration(MessageContext context) {
        
        Configuration busCfg = getBinding().getBus().getConfiguration();
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        Configuration parent;
        org.objectweb.celtix.ws.addressing.EndpointReferenceType ref = getBinding().getEndpointReference();

        if (ContextUtils.isRequestor(context)) {
            String id = EndpointReferenceUtils.getServiceName(ref).toString()
                + "/" + EndpointReferenceUtils.getPortName(ref);
            parent = builder.getConfiguration(ServiceImpl.PORT_CONFIGURATION_URI,
                                                                id, busCfg);
        } else {
            parent = builder.getConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, EndpointReferenceUtils
                .getServiceName(ref).toString(), busCfg);
        }

        Configuration cfg = builder.getConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);
        if (null == cfg) {
            cfg = builder.buildConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);
        }
        return cfg;

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
            LOG.fine("inbound sequence: " + (null == inSeqId ? "null" : inSeqId.getValue()));

            // not for partial responses to oneway requests

            if (!(isServerSide() && BindingContextUtils.isOnewayTransport(context))) {

                if (!ContextUtils.isRequestor(context)) {
                    assert null != inSeqId;
                }
                
                // get the current sequence, requesting the creation of a new one if necessary
                
                Sequence seq = getSequence(inSeqId, context, maps);
                assert null != seq;

                // increase message number and store a sequence type object in
                // context

                seq.nextMessageNumber(inSeqId, inMessageNumber);
                rmpsOut.setSequence(seq);

                // if this was the last message in the sequence, reset the
                // current sequence so that a new one will be created next 
                // time the handler is invoked

                if (null != seq.getLastMessageNumber()) {
                    source.setCurrent(null);
                }

                // tell the source to store a copy of the message in the
                // retransmission
                // queue and schedule the next retransmission

                getSource().addUnacknowledged(MessageContextWrapper.unwrap(context));

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
        
        AddressingPropertiesImpl maps = ContextUtils.retrieveMAPs(context, false, false);
        assert null != maps;

        String action = null;
        if (null != maps.getAction()) {
            action = maps.getAction().getValue();
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }

        // nothing to do if this is a response to a CreateSequence request

        if (RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)) {
            return;
        } else if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)) {
            Object[] parameters = (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
            CreateSequenceType cs = (CreateSequenceType)parameters[0];
            AttributedURI to = VersionTransformer.convert(maps.getTo());
            ContextUtils.retrieveTo(context);

            CreateSequenceResponseType csr = getServant().createSequence(getDestination(), cs, to);
            context.put(ObjectMessageContext.METHOD_RETURN, csr);
           
            maps = ContextUtils.retrieveMAPs(context, true, true);
            if (null == maps) {
                LOG.fine("No outbound addressing properties stored in provider context, create new ones.");
                maps = new AddressingPropertiesImpl();
            }
            AttributedURIType actionURI = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
            actionURI.setValue(RMUtils.getRMConstants().getCreateSequenceResponseAction());
            maps.setAction(actionURI);
            ContextUtils.storeMAPs(maps, context, true, false, true, true);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Set action for outbound addressing properties to: " + maps.getAction().getValue());
            }

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
                Sequence seq = getDestination().getSequence(ar.getIdentifier());
                if (null != seq) {
                    seq.scheduleImmediateAcknowledgement();
                } else {
                    LOG.severe("No such sequence.");
                }
            }
        }
    }

    private void addAcknowledgements(RMPropertiesImpl rmpsOut, Identifier inSeqId, AttributedURI to) {

        for (Sequence seq : getDestination().getAllSequences()) {
            if (seq.sendAcknowledgement()
                && ((seq.getAcksTo().getAddress().getValue().equals(RMUtils.getAddressingConstants()
                    .getAnonymousURI()) && Sequence.identifierEquals(seq.getIdentifier(), inSeqId))
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
    
    private Sequence getSequence(Identifier inSeqId, 
                                 LogicalMessageContext context, 
                                 AddressingPropertiesImpl maps) throws SequenceFault {
        Sequence seq = getSource().getCurrent(inSeqId);

        if (null == seq) {
            // TODO: better error handling
            try {
                EndpointReferenceType acksTo = null;
                if (isServerSide()) {
                    AddressingPropertiesImpl inMaps = ContextUtils
                        .retrieveMAPs(context, false, false);
                    inMaps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
                    acksTo = RMUtils.createReference(inMaps.getTo().getValue());
                } else {
                    acksTo = VersionTransformer.convert(maps.getReplyTo());
                    // for oneways
                    if (Names.WSA_NONE_ADDRESS.equals(acksTo.getAddress().getValue())) {
                        acksTo = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
                    }
                }

                getProxy().createSequence(getSource(), acksTo, inSeqId);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            seq = getSource().getCurrent();
        }
        return seq;
    }

}
