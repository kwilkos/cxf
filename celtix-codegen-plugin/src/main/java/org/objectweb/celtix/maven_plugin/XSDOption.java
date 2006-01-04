package org.objectweb.celtix.maven_plugin;


public class XSDOption {
    String xsd;
    String packagename;
    
    public String getPackagename() {
        return packagename;
    }
    public void setPackagename(String pn) {
        this.packagename = pn;
    }
    public String getXsd() {
        return xsd;
    }
    public void setXsd(String x) {
        this.xsd = x;
    }


}
