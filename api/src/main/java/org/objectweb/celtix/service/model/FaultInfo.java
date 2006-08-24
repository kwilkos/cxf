package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

public final class FaultInfo extends AbstractMessageContainer {
    private QName faultName;
    
    public FaultInfo(QName fname, QName mname, OperationInfo info) {
        super(info, mname);
        faultName = fname;
    }
    
    public QName getFaultName() {
        return faultName;
    }
    public void setFaultName(QName fname) {
        faultName = fname;
    }
}
