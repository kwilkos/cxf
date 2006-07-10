package org.objectweb.celtix.servicemodel;

import javax.xml.namespace.QName;

public final class FaultInfo extends AbstractMessageContainer {
    private String faultName;
    
    public FaultInfo(String fname, QName mname, OperationInfo info) {
        super(info, mname);
        faultName = fname;
    }
    
    public String getFaultName() {
        return faultName;
    }
    public void setFaultName(String fname) {
        faultName = fname;
    }
}
