package org.apache.cxf.binding.soap.model;


public class SoapOperationInfo {
    private String action;
    private String style;

    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
}
