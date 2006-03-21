package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
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
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
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

        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        } else {
            handleInbound(context);
        }
        return true;
    }

    @PreDestroy
    public void shutdown() {
        if (source != null) {
            source.shutdown();
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

    private void open(LogicalMessageContext context) {

        initialise(context);

        // TODO begin transaction
    }

    private void initialise(MessageContext context) {
        if (null == clientTransport && null == serverTransport) {
            clientTransport = BindingContextUtils.retrieveClientTransport(context);
            if (null == clientTransport) {
                serverTransport = BindingContextUtils.retrieveServerTransport(context);
            }
        }

        if (null == clientBinding && null != clientTransport) {
            clientBinding = (AbstractClientBinding)BindingContextUtils.retrieveClientBinding(context);
        }

        if (null == serverBinding && null != serverTransport) {
            serverBinding = (AbstractServerBinding)BindingContextUtils.retrieveServerBinding(context);
        }

        if (null == configuration) {
            configuration = createConfiguration(context);
        }
        
        if (ContextUtils.isOutbound(context)) {
            if (ContextUtils.isRequestor(context) && null == source) {
                // REVISIT share sources across handler chanins
                source = new RMSource(this);
            }
        } else {
            if (!ContextUtils.isRequestor(context) && null == destination) {
                // REVISIT share destinations across handler chanins
                destination = new RMDestination(this);
            }
        } 

        if (null == timer) {
            timer = new Timer();
        }
    }

    private Configuration createConfiguration(MessageContext context) {
        
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

    private void handleOutbound(LogicalMessageContext context) {
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

        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)
            || RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
            return;
        }

        // not for partial responses to oneway requests

        if (!(isServerSide() && BindingContextUtils.isOnewayTransport(context))) {
            Sequence seq = source.getCurrent();
            if (null == seq) {
                // TODO: better error handling
                try {
                    proxy.createSequence(source);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (SequenceFault ex) {
                    ex.printStackTrace();
                }

                seq = source.getCurrent();
            }
            assert null != seq;

            // increase message number and store a sequence type object in the
            // context

            seq.nextMessageNumber();
            RMContextUtils.storeSequence(context, seq);

            // tell the source to store a copy of the message in the
            // retransmission
            // queue and schedule the next retransmission

            source.addUnacknowledged(MessageContextWrapper.unwrap(context));

        }


        
        // add Acknowledgements       
        
        if (null != destination) {
            AttributedURI to = VersionTransformer.convert(maps.getTo());
            assert null != to;
            addAcknowledgements(context, to);
        }

        // indicate to the binding that a response is expected from the transport although
        // the web method is a oneway method

        if (!isServerSide() && BindingContextUtils.isOnewayMethod(context)) {
            context.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, Boolean.FALSE);
        }
    }

    private void handleInbound(LogicalMessageContext context) {

        LOG.entering(getClass().getName(), "handleInbound");

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

            try {
                CreateSequenceResponseType csr = servant.createSequence(destination, cs, to);
                context.put(ObjectMessageContext.METHOD_RETURN, csr);
            } catch (SequenceFault ex) {
                // ignore for now
                ex.printStackTrace();
            }

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
                LOG.fine("Changed action in context from: " + action + " to: " + maps.getAction().getValue());
            }

            return;
        } else if (RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)) {
            Object[] parameters = (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
            TerminateSequenceType cs = (TerminateSequenceType)parameters[0];

            try {
                servant.terminateSequence(destination, cs.getIdentifier());
            } catch (SequenceFault ex) {
                // ignore for now
            }
        }

        // for application AMD out of band messages

        processAcknowledgments(context);

        processAcknowledgmentRequests(context);

        // only for application messages
        if (null != action) {
            processSequence(context);
        }

        // clean up
        RMContextUtils.removeRMProperties(context);
    }

    private void processAcknowledgments(LogicalMessageContext context) {
        Collection<SequenceAcknowledgement> acks = RMContextUtils.retrieveAcknowledgments(context);
        if (null != acks) {
            for (SequenceAcknowledgement ack : acks) {
                source.setAcknowledged(ack);
            }
        }
    }

    private void processSequence(LogicalMessageContext context) {
        SequenceType s = RMContextUtils.retrieveSequence(context);
        destination.acknowledge(s);
    }

    private void processAcknowledgmentRequests(LogicalMessageContext context) {
        Collection<AckRequestedType> requested = RMContextUtils.retrieveAcksRequested(context);
        if (null != requested) {
            for (AckRequestedType ar : requested) {
                Sequence seq = destination.getSequence(ar.getIdentifier());
                if (null != seq) {
                    seq.scheduleImmediateAcknowledgement();
                } else {
                    LOG.severe("No such sequence.");
                }
            }
        }
    }

    private void addAcknowledgements(LogicalMessageContext context, AttributedURI to) {

        RMContextUtils.removeAcknowledgments(context);

        for (Sequence seq : destination.getAllSequences()) {
            if (seq.sendAcknowledgement()
                && (seq.getAcksTo().getAddress().getValue().equals(RMUtils.getAddressingConstants()
                                                                   .getAnonymousURI())
                    || to.equals(seq.getAcksTo().getAddress().getValue()))) {
                RMContextUtils.storeAcknowledgment(context, seq);
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
            Collection<SequenceAcknowledgement> acks = RMContextUtils.retrieveAcknowledgments(context);
            if (null == acks) {
                LOG.fine("No acknowledgements added.");
            } else {
                LOG.fine("Added " + acks.size() + " acknowledgements.");
            }
        }
    }

}
