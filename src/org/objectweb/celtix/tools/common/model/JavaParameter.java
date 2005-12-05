package org.objectweb.celtix.tools.common.model;

public class JavaParameter extends JavaType {

    private boolean holder;
    private String holderName;
    
    public JavaParameter() {
    }

    public JavaParameter(String n, String t, String tns) {
        super(n, t, tns);
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean b) {
        holder = b;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String hn) {
        this.holderName = hn;
    }
}
