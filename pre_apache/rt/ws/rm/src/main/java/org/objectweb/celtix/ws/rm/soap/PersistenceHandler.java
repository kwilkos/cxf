package org.objectweb.celtix.ws.rm.soap;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.configuration.wsrm.DeliveryAssuranceType;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.soap.MAPCodec;
import org.objectweb.celtix.ws.rm.ConfigurationHelper;
import org.objectweb.celtix.ws.rm.DestinationSequence;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.Names;
import org.objectweb.celtix.ws.rm.PersistenceManager;
import org.objectweb.celtix.ws.rm.RMContextUtils;
import org.objectweb.celtix.ws.rm.RMDestination;
import org.objectweb.celtix.ws.rm.RMHandler;
import org.objectweb.celtix.ws.rm.RMMessageImpl;
import org.objectweb.celtix.ws.rm.RMPropertiesImpl;
import org.objectweb.celtix.ws.rm.RMSource;
import org.objectweb.celtix.ws.rm.RMUtils;
import org.objectweb.celtix.ws.rm.RetransmissionQueue;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.SourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.persistence.RMStoreFactory;


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
    private RMSoapHandler rmSOAPHandler;
    private MAPCodec wsaSOAPHandler;
    
    @PostConstruct
    public synchronized void initialise() {
        RMHandler handler = RMHandler.getHandlerMap().get(getBinding());  
        handler.setPersistenceManager(this);
     
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
        } else {
            handleInbound(context);
        }
        return true;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        initQueue();
        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        } else {
            handleInbound(context);
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
    
    protected RMSoapHandler getRMSoapHandler() {
        if (null == rmSOAPHandler) {
            AbstractBindingImpl abi = getBinding().getBindingImpl();
            List<Handler> handlerChain = abi.getPostProtocolSystemHandlers();
            for (Handler h : handlerChain) {
                if (h instanceof RMSoapHandler) {
                    rmSOAPHandler = (RMSoapHandler)h;
                }
            }
        }
        return rmSOAPHandler;
    }

    protected MAPCodec getWsaSOAPHandler() {
        if (null == wsaSOAPHandler) {
            AbstractBindingImpl abi = getBinding().getBindingImpl();
            List<Handler> handlerChain = abi.getPostProtocolSystemHandlers();
            for (Handler h : handlerChain) {
                if (h instanceof MAPCodec) {
                    wsaSOAPHandler = (MAPCodec)h;
                }
            }
        }
        return wsaSOAPHandler;
    }

       
    void handleOutbound(SOAPMessageContext context) {
        
        LOG.entering(getClass().getName(), "handleOutbound");
        
        DeliveryAssuranceType da = configurationHelper.getDeliveryAssurance();
        if (!da.isSetAtLeastOnce()) {
            return;
        }
        
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
    
    void handleInbound(SOAPMessageContext context) {
        LOG.entering(getClass().getName(), "handleOutbound");
        
        DeliveryAssuranceType da = configurationHelper.getDeliveryAssurance();
        if (!(da.isSetInOrder() && da.isSetAtLeastOnce())) {
            return;
        }
        
        if (null == getStore()) {
            return;            
        }
        
        SequenceType s = getSequence(context.getMessage());
        if (null == s) {
            return;
        }
        
        RMMessage msg = new RMMessageImpl(s.getMessageNumber(), context);        
        RMDestinationSequence ds = new DestinationSequence(s.getIdentifier(), null, null, null);
        getStore().persistIncoming(ds, msg);        
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
    
    private SequenceType getSequence(SOAPMessage message) {
        SOAPHeader header = null;
        try {
            SOAPEnvelope env = message.getSOAPPart().getEnvelope();
            header = env.getHeader();
        } catch (SOAPException ex) {
            LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", ex);  
        }
        
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = 
                (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName(); 
            if (Names.WSRM_NAMESPACE_NAME.equals(headerName.getURI())
                && Names.WSRM_SEQUENCE_NAME.equals(localName)) {
                try {
                    return RMSoapHandler.decodeProperty(SequenceType.class, headerElement, null); 
                } catch (JAXBException ex) {
                    LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", ex);
                }
            }
        }
        return null;
    }
    
}
