package org.objectweb.celtix.jbi.transport;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Celtix Transport Factory for JBI Transport.
 */
public class JBITransportFactory implements TransportFactory {

    private static final Logger LOG = Logger.getLogger(JBITransportFactory.class.getName()); 

    private CeltixServiceUnitManager suManager; 
    private DeliveryChannel deliveryChannel;

    public void init(Bus b) { 
    } 



    public DeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(DeliveryChannel newDeliverychannel) {
        LOG.fine("configuring DeliveryChannel: " + newDeliverychannel);
        deliveryChannel = newDeliverychannel;
    }

    public CeltixServiceUnitManager getServiceUnitManager() { 
        return suManager; 
    }
    
    public void setServiceUnitManager(CeltixServiceUnitManager sum) {
        if (sum == null) { 
            Thread.dumpStack(); 
        } 
        LOG.fine("configuring ServiceUnitManager: " + sum);
        suManager = sum;
    }


    public ServerTransport createServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException {
        LOG.info("creating JBI server transport");
        
        if (suManager == null || deliveryChannel == null) { 
            LOG.severe("JBITransportFactory is not properly initialised");
            LOG.severe("CeltixServiceUnitManager: " + suManager);
            LOG.severe("DeliveryChannel: " + deliveryChannel);
            throw new IllegalStateException("JBITransport factory not fully initalised");
        }

        return new JBIServerTransport(suManager, deliveryChannel); 
    } 

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException { 

        throw new RuntimeException("not yet implemented");
    }
 
    
    public ClientTransport createClientTransport(EndpointReferenceType address)
        throws WSDLException, IOException { 

        LOG.info("creating JBI client transport");

        if (deliveryChannel == null) { 
            LOG.severe("JBITransportFactory is not properly initialised");
            LOG.severe("DeliveryChannel: " + deliveryChannel);
            throw new IllegalStateException("JBITransport factory not fully initalised");
        }

        return new JBIClientTransport(deliveryChannel, address);
    }



    public void setResponseCallback(ResponseCallback callback) {
        throw new RuntimeException("not yet implemented");
    } 
}
