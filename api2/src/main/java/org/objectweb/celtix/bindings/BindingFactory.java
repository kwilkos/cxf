package org.objectweb.celtix.bindings;

import org.objectweb.celtix.service.model.BindingInfo;

/**
 * A factory interface for creating Bindings from BindingInfo metadata.
 */
public interface BindingFactory {
    
    /**
     * Create a Binding from the BindingInfo metadata.
     * 
     * @param binding
     */
    Binding createBinding(BindingInfo binding);

}
