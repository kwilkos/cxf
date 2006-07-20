package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 */
public class BindingOperationInfo extends AbstractPropertiesHolder {
    
    final BindingInfo bindingInfo;
    OperationInfo opInfo;

    final BindingMessageInfo inputMessage;
    final BindingMessageInfo outputMessage;
    Map<String, BindingFaultInfo> faults;
    
    BindingOperationInfo(BindingInfo bi, OperationInfo opinfo) { 
        bindingInfo = bi;
        opInfo = opinfo;
        
        if (opInfo.getInput() != null) {
            inputMessage = new BindingMessageInfo(opInfo.getInput(), this);
        } else {
            inputMessage = null;
        }
        if (opInfo.getOutput() != null) {
            outputMessage = new BindingMessageInfo(opInfo.getOutput(), this);
        } else {
            outputMessage = null;
        }
        
        Collection<FaultInfo> of = opinfo.getFaults();
        if (!of.isEmpty()) {
            faults = new ConcurrentHashMap<String, BindingFaultInfo>(of.size());
            for (FaultInfo fault : of) {
                faults.put(fault.getFaultName(), new BindingFaultInfo(fault, this));
            }
        }        
    }
    
    public BindingInfo getBinding() {
        return bindingInfo;
    }
    
    public String getName() {
        return opInfo.getName();
    }
    
    public OperationInfo getOperationInfo() {
        return opInfo;
    }

    public BindingMessageInfo getInput() {
        return inputMessage;
    }
    
    public BindingMessageInfo getOutput() {
        return outputMessage;
    }
    
    public BindingFaultInfo getFault(String name) {
        if (faults != null) {
            return faults.get(name);
        }
        return null;
    }
    public Collection<BindingFaultInfo> getFaults() {
        if (faults == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(faults.values());
    }

}
