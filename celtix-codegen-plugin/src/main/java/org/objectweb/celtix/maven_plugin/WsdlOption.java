package org.objectweb.celtix.maven_plugin;

import java.util.List;

public class WsdlOption {
    String wsdl;
    List packagenames;
    List extraargs;

    public List getExtraargs() {
        return extraargs;
    }

    public void setExtraargs(List ea) {
        this.extraargs = ea;
    }

    public List getPackagenames() {
        return packagenames;
    }

    public void setPackagenames(List pn) {
        this.packagenames = pn;
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String w) {
        wsdl = w;
    }
}
