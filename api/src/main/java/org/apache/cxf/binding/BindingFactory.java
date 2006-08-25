package org.apache.cxf.binding;

import org.apache.cxf.service.model.BindingInfo;

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
