package org.objectweb.celtix.service.model;


import javax.xml.namespace.QName;

public final class MessageInfo extends AbstractMessageContainer {
    private MessageInfo unwrappedMI;
    
    public MessageInfo(OperationInfo op, QName nm) {
        super(op, nm);
    }
    
    public MessageInfo getUnwrappedMessage() {
        return unwrappedMI;
    }
    public void setUnwrappedMessage(MessageInfo mi) {
        unwrappedMI = mi;
    }
    
}
