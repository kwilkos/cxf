package org.objectweb.celtix.geronimo.container;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.bus.transports.http.HTTPTransportFactory;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class GeronimoTransportFactory extends HTTPTransportFactory {

    private ThreadLocal<CeltixWebServiceContainer> currentContainer
        = new ThreadLocal<CeltixWebServiceContainer>();
        
   
    public CeltixWebServiceContainer getCurrentContainer() {
        return currentContainer.get();
    }
  
    public void setCurrentContainer(CeltixWebServiceContainer container) {
        currentContainer.set(container);
    }

   
    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        
        GeronimoServerTransport ret = new GeronimoServerTransport(getBus(), address);
        getCurrentContainer().setServerTransport(ret);
        return ret;
    }

}
