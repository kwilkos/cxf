package org.objectweb.celtix.bindings.soap2;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.service.model.BindingInfo;


public class SoapBindingFactory implements BindingFactory  {
    

    public Binding createBinding(BindingInfo binding) {
        return new SoapBinding();
    }
       
}
