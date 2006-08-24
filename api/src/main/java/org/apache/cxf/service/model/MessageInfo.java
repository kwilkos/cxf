package org.apache.cxf.service.model;


import javax.xml.namespace.QName;

public class MessageInfo extends AbstractMessageContainer {
    public MessageInfo(OperationInfo op, QName nm) {
        super(op, nm);
    }
    
}
