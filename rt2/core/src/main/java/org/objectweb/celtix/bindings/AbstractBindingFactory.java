package org.objectweb.celtix.bindings;

import java.util.Collection;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.ExtensibilityElement;

import org.objectweb.celtix.service.model.AbstractPropertiesHolder;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.wsdl11.WSDLBindingFactory;

public abstract class AbstractBindingFactory implements BindingFactory, WSDLBindingFactory {
    
    /**
     * Copies extensors from the Binding to BindingInfo.
     * @param service
     * @param binding
     * @return
     */
    public BindingInfo createBindingInfo(ServiceInfo service, Binding binding) {

        BindingInfo bi = new BindingInfo(service);
        bi.setName(binding.getQName());
        copyExtensors(bi, binding.getExtensibilityElements());
        
        for (BindingOperation bop : cast(binding.getBindingOperations(), BindingOperation.class)) {
            String inName = null;
            String outName = null;
            if (bop.getBindingInput() != null) {
                inName = bop.getBindingInput().getName();
            }
            if (bop.getBindingOutput() != null) {
                inName = bop.getBindingOutput().getName();
            }
            BindingOperationInfo bop2 = bi.buildOperation(bop.getName(), inName, outName);
            copyExtensors(bop2, bop.getExtensibilityElements());
            bi.addOperation(bop2);
            
            if (bop.getBindingInput() != null) {
                copyExtensors(bop2.getInput(), bop.getBindingInput().getExtensibilityElements());
            }
            if (bop.getBindingOutput() != null) {
                copyExtensors(bop2.getOutput(), bop.getBindingOutput().getExtensibilityElements());
            }
            for (BindingFault f : cast(bop.getBindingFaults().values(), BindingFault.class)) {
                copyExtensors(bop2.getFault(f.getName()),
                              bop.getBindingFault(f.getName()).getExtensibilityElements());
            }
        } 
        return bi;
    }

    // utility for dealing with the JWSDL collections that are 1.4 based.   We can 
    // kind of use a normal for loop with this
    @SuppressWarnings("unchecked")
    private <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>)p;
    }

    private void copyExtensors(AbstractPropertiesHolder info, List<?> extList) {
        for (ExtensibilityElement ext : cast(extList, ExtensibilityElement.class)) {
            info.addExtensor(ext);
        }
    }
}
