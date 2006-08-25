package org.objectweb.celtix.tools.common.model;

public class JavaParameter extends JavaType {

    private boolean holder;
    private String holderName;
    private String holderClass;
    private String annotation;

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

    public void setHolderClass(String clz) {
        this.holderClass = clz;
    }

    public String getHolderClass() {
        return this.holderClass;
    }

    public void setAnnotation(String anno) {
        this.annotation = anno;
    }

    public String getAnnotation() {
        return this.annotation;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        if (holder) {
            sb.append("\nIS Holder: [Holder Name]:");
            sb.append(holderName);
        }
        sb.append("\n Annotation:");
        sb.append(annotation);
        return sb.toString();
    }
}
