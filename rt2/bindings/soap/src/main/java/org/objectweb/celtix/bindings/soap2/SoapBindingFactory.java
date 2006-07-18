package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.Binding;

/* 
 * remove comment when dependency dependencies on old api and runtime have been removed
 */
public class SoapBindingFactory /* implements BindingFactory */  {
    
    // private Bus bus;
    
    public void init(Bus b) {
        // bus = b;
    }
    
    public Binding createBinding() throws WSDLException, IOException {
        return new SoapBinding();
    }
}
