package org.objectweb.celtix.service.model;

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

}
