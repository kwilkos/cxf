package org.apache.cxf.binding.soap.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.service.model.MessagePartInfo;

public class SoapBodyInfo {
    private List <MessagePartInfo> parts = new ArrayList<MessagePartInfo>();
    private String use;
    
    public List<MessagePartInfo> getParts() {
        return parts;
    }

    public void setParts(List<MessagePartInfo> parts) {
        this.parts = parts;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }
}
