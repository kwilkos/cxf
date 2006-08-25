package org.objectweb.celtix.transports.jms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class JMSDestination implements Destination {
    static final Logger LOG = LogUtils.getL7dLogger(JMSDestination.class);
    final EndpointInfo endpointInfo;
    final EndpointReferenceType reference;
    final ConduitInitiator conduitInitiator;
    final JMSDestinationConfiguration config;
    
    
    public JMSDestination(Bus b,
                          ConduitInitiator ci,
                          EndpointInfo endpointInfo) throws IOException {
        this(b, ci, endpointInfo, new JMSDestinationConfiguration(b, endpointInfo));
    }
    
    public JMSDestination(Bus b,
                          ConduitInitiator ci,
                          EndpointInfo info,
                          JMSDestinationConfiguration cfg) throws IOException {
        endpointInfo = info;
        conduitInitiator = ci;
        config = cfg;        
        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(config.getAddress());
        reference.setAddress(address);
    }
    
    public EndpointReferenceType getAddress() {       
        return reference;
    }

    public Conduit getBackChannel(Message inMessage, 
                                  Message partialResponse, 
                                  EndpointReferenceType address) throws WSDLException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    public void setMessageObserver(MessageObserver observer) {
        // TODO Auto-generated method stub
        
    }

}
