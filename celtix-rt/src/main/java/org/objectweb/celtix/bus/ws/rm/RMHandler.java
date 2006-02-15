package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.handlers.SystemHandler;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class RMHandler implements LogicalHandler<LogicalMessageContext>, SystemHandler {
    
    public static final String RM_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/ws/rm/rm-config";
    public static final String RM_CONFIGURATION_ID = "rm-handler";
    
    private RMSource source;
    private RMDestination destination;
    private RMService service;
    private Configuration configuration;
    
    private AbstractClientBinding clientBinding;
    private AbstractServerBinding serverBinding;
    private ClientTransport clientTransport;
    private ServerTransport serverTransport;
    
    public RMHandler() {
        service = new RMService(this);
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
    
    public Configuration getConfiguration() {
        return configuration;
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
    
    public AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return clientBinding;
        }
        return serverBinding;
    }
    
    public RMService getService() {
        return service;
    }
    
    
    private void open(LogicalMessageContext context) {
   
        initialise(context);
        
        // TODO begin transaction
    }
    
    private void initialise(MessageContext context) {
        if (ContextUtils.isOutbound(context)) {
            if (ContextUtils.isRequestor(context) && null == source) {
                source = new RMSource(this);
            }
        } else {
            if (!ContextUtils.isRequestor(context) && null == destination) {
                destination = new RMDestination(this);
            }
        } 
        
        if (null == clientTransport && null == serverTransport) {
            clientTransport = ContextUtils.retreiveClientTransport(context);
            serverTransport = ContextUtils.retreiveServerTransport(context);
        }
        
        if (null == clientBinding && null == serverBinding) {
            clientBinding = RMContextUtils.retrieveClientBinding(context);
            serverBinding = RMContextUtils.retrieveServerBinding(context);
        }
        
        if (null == configuration) {
            configuration = createConfiguration(context);
        }
    }
    
    private Configuration createConfiguration(MessageContext context) {
        Configuration busCfg = getBinding().getBus().getConfiguration();
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        Configuration parent;
        EndpointReferenceType ref = getBinding().getEndpointReference();
        
        if (ContextUtils.isRequestor(context)) {
            Configuration serviceCfg = builder.getConfiguration(
                ServiceImpl.SERVICE_CONFIGURATION_URI,
                EndpointReferenceUtils.getServiceName(ref).toString(),
                busCfg);
            parent = builder.getConfiguration(
                ServiceImpl.PORT_CONFIGURATION_URI,
                EndpointReferenceUtils.getPortName(ref),
                serviceCfg);            
        } else {
            parent = builder.getConfiguration(
                EndpointImpl.ENDPOINT_CONFIGURATION_URI,
                EndpointReferenceUtils.getServiceName(ref).toString(),
                busCfg);
        }
        
        Configuration cfg = builder.getConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);
        if (null == cfg) {
            cfg = builder.buildConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);
        }
        return cfg;
        
    }
    
    private void handleOutbound(LogicalMessageContext context) {

        // nothing to do if this is a CreateSequence request
        
        String action = RMContextUtils.retrieveAction(context);
        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)) {
            return;
        }
        
        Sequence seq = source.getCurrent();
        if (null == seq) {
            try {
                service.createSequence(source);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            seq = source.getCurrent();       
        }
        assert null != seq;
        
        // store a sequence type object in the context (incl. getting the next sequence number),
        
        RMContextUtils.storeSequenceProperties(context, seq);
        
        // tell the source to store a copy of the message in the retransmission
        // queue and schedule the next retransmission
        
        source.addUnacknowledged(context);
        
        
        
        // add Acknowledgements
        
        
        
        
        
        
        
    }
    
    private void handleInbound(LogicalMessageContext context) {
        
    }
}
