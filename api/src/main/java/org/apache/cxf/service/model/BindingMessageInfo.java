package org.apache.cxf.service.model;

public class BindingMessageInfo extends AbstractPropertiesHolder {

    MessageInfo msg;
    BindingOperationInfo op;
    
    BindingMessageInfo(MessageInfo m, BindingOperationInfo boi) {
        op = boi;
        msg = m;
    }
    
    public MessageInfo getMessageInfo() {
        return msg;
    }
    
    public BindingOperationInfo getBindingOperation() {
        return op;
    }
}
