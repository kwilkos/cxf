package org.objectweb.celtix.bindings;

import javax.xml.ws.Binding;

import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;


/**
 * Contains base operations to be implemented by bindings.
 */
public interface BindingBase {
    
    /**
     * Returns whether the given addressing information is compatible with
     * this binding.
     *
     * @param address The address to use.
     * @return whether the given address is compatible.
     */
    boolean isCompatibleWithAddress(String address);
    
    /**
     * Returns the <code>Binding</code> reference.
     *
     * @return The Binding.
     */
    Binding getBinding();
    
    /**
     * Create an <code>ObjectMessageContext</code>.
     *
     * @return The newly created ObjectMessageContext.
     */
    ObjectMessageContext createObjectContext();
        

    /**
     * Create a <code>HandlerInvoker</code>.
     *
     * @return The newly create <code>HandlerInvoker</code>
     */
    HandlerInvoker createHandlerInvoker();
}
