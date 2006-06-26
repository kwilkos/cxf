package org.objectweb.celtix.servicemodel;


import javax.xml.namespace.QName;

public final class MessageInfo extends AbstractMessageContainer {
    private QName wrapperName;
    private boolean literal = true;
    private String targetNS;

    public MessageInfo(OperationInfo op, QName nm) {
        super(op, nm);
    }
    
    public QName getWrapperQName() {
        return wrapperName;
    }
    public void setWrapperQName(QName nm) {
        wrapperName = nm;
    }
    public boolean isParamStyleWrapped() {
        return wrapperName != null;
    }

    public boolean isLiteral() {
        return literal;
    }
    public void setLiteral(boolean b) {
        literal = b;
    }
    public String getTargetNamespace() {
        return targetNS;
    }
    public void setTargetNamespace(String s) {
        targetNS = s;
    }
    
}
