package org.apache.cxf.binding.soap.model;

import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class SoapBindingInfo extends BindingInfo {
    private SoapVersion soapVersion;
    private String style;
    private String transportURI;

    public SoapBindingInfo(ServiceInfo serv, String n, SoapVersion soapVersion) {
        super(serv, n);

        this.soapVersion = soapVersion;
    }

    public SoapVersion getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    public String getStyle() {
        return style;
    }

    public String getStyle(OperationInfo operation) {
        return style;
    }

    public OperationInfo getOperationByAction(String action) {
        for (BindingOperationInfo b : getOperations()) {
            SoapOperationInfo opInfo = b.getExtensor(SoapOperationInfo.class);

            if (opInfo.getAction().equals(action)) {
                return b.getOperationInfo();
            }
        }

        return null;
    }

    /**
     * Get the soap action for an operation. Will never return null.
     * 
     * @param operation
     * @return
     */
    public String getSoapAction(OperationInfo operation) {
        BindingOperationInfo b = (BindingOperationInfo)getOperation(operation.getName());
        SoapOperationInfo opInfo = b.getExtensor(SoapOperationInfo.class);

        if (opInfo != null && opInfo.getAction() != null) {
            return opInfo.getAction();
        }
        
        return "";
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getTransportURI() {
        return transportURI;
    }

    public void setTransportURI(String transportURI) {
        this.transportURI = transportURI;
    }

}
