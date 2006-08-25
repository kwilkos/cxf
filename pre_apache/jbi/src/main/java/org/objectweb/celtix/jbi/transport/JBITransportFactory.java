package org.objectweb.celtix.jbi.transport;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Celtix Transport Factory for JBI Transport.
 */
public class JBITransportFactory implements TransportFactory {

    private static final Logger LOG = LogUtils.getL7dLogger(JBITransportFactory.class); 

    private CeltixServiceUnitManager suManager; 
    private DeliveryChannel deliveryChannel;

    public void init(Bus b) { 
    } 



    public DeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(DeliveryChannel newDeliverychannel) {
        LOG.fine(new Message("CONFIG.DELIVERY.CHANNEL", LOG).toString() + newDeliverychannel);
        deliveryChannel = newDeliverychannel;
    }

    public CeltixServiceUnitManager getServiceUnitManager() { 
        return suManager; 
    }
    
    public void setServiceUnitManager(CeltixServiceUnitManager sum) {
        if (sum == null) { 
            Thread.dumpStack(); 
        } 
        LOG.fine(new Message("CONFIG.SU.MANAGER", LOG).toString() + sum);
        suManager = sum;
    }


    public ServerTransport createServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException {
        LOG.fine(new Message("CREATE.SERVER.TRANSPORT", LOG).toString());
        
        if (suManager == null || deliveryChannel == null) { 
            LOG.severe(new Message("JBI.TRANSPORT.FACTORY.NOT.INITIALIZED", LOG).toString());
            LOG.severe(new Message("SU.MANAGER", LOG).toString() + suManager);
            LOG.severe(new Message("DELIVERY.CHANNEL", LOG).toString() + deliveryChannel);
            throw new IllegalStateException(new Message("JBI.TRANSPORT.FACTORY.NOT.FULLY.INITIALIZED", 
                                                        LOG).toString());
        }

        return new JBIServerTransport(suManager, deliveryChannel); 
    } 

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException { 

        throw new RuntimeException(new Message("NOT.IMPLEMENTED", LOG).toString());
    }
 
    
    public ClientTransport createClientTransport(EndpointReferenceType address,
                                                 ClientBinding binding)
        throws WSDLException, IOException { 

        LOG.fine(new Message("CREATE.CLIENT.TRANSPORT", LOG).toString());

        if (deliveryChannel == null) { 
            LOG.severe(new Message("JBI.TRANSPORT.FACTORY.NOT.INITIALIZED", LOG).toString());
            LOG.severe(new Message("DELIVERY.CHANNEL", LOG).toString() + deliveryChannel);
            throw new IllegalStateException(new Message("JBI.TRANSPORT.FACTORY.NOT.FULLY.INITIALIZED", 
                                                        LOG).toString());
        }

        return new JBIClientTransport(deliveryChannel, address, binding);
    }



    public void setResponseCallback(ResponseCallback callback) {
        throw new RuntimeException(new Message("NOT.IMPLEMENTED", LOG).toString());
    } 
}
