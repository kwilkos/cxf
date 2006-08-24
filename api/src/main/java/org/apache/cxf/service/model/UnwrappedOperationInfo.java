package org.apache.cxf.service.model;

import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;

public class UnwrappedOperationInfo extends OperationInfo {
    OperationInfo wrappedOp;

    public UnwrappedOperationInfo(OperationInfo op) {
        super(op);
        wrappedOp = op;
    }
    
    public OperationInfo getWrappedOperation() {
        return wrappedOp;
    }
    
    public boolean isUnwrapped() {
        return true;
    }

    
    
    public Object getProperty(String name) {
        return wrappedOp.getProperty(name);
    }
    
    public <T> T getProperty(String name, Class<T> cls) {
        return wrappedOp.getProperty(name, cls);
    }
    
    public void setProperty(String name, Object v) {
        wrappedOp.setProperty(name, v);
    }
    
    public void addExtensor(Object el) {
        wrappedOp.addExtensor(el);
    }

    public <T> T getExtensor(Class<T> cls) {
        return wrappedOp.getExtensor(cls);
    }
    public <T> List<T> getExtensors(Class<T> cls) {
        return wrappedOp.getExtensors(cls);
    }

    public List<ExtensibilityElement> getWSDL11Extensors() {
        return wrappedOp.getWSDL11Extensors();
    }
}
