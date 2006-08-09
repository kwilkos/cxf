package org.objectweb.celtix.jaxws.support;

import java.util.Map;

import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.ClientImpl;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;

/**
 * A JAX-WS specific implementation of the Celtix {@link Client} interface.
 * Specialisation is achieved by overwriting {@link ClientImpl.setExchangeProperties} 
 * in which a HandlerInvokerChain is created and stored as a property in the {@link Exchange} object.
 * Likewise, the keys under which the frontend specific data reader and writer factories for
 * the underlying operation can be looked up in the Service Model are stores as properties
 * in the {@link Exchange} object.
 * The flow of control in the base class implementation of {@link ClientImp.invokeObject} is left 
 * unchanged.
 *  
 */

public class JaxwsClientImpl extends ClientImpl {
    
    Bus bus;
    Endpoint endpoint;
    Binding binding;    
    
    public JaxwsClientImpl(Bus bs, Endpoint e, Binding b) {
        super(bs, e);
    }
 
    protected void setExchangeProperties(Exchange exchange, Map<String, Object> ctx) {
        exchange.put(Message.DATAREADER_FACTORY_KEY, JaxwsEndpointImpl.JAXWS_DATAREADER_FACTORY);
        exchange.put(Message.DATAWRITER_FACTORY_KEY, JaxwsEndpointImpl.JAXWS_DATAWRITER_FACTORY);
    }
    
}
