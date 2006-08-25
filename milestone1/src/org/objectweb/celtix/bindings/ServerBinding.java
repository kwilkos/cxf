package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.ws.Endpoint;

public interface ServerBinding extends BindingBase {
    
    Endpoint getEndpoint(); 
    
    void activate() throws WSDLException, IOException;
    
    void deactivate() throws IOException;
}
