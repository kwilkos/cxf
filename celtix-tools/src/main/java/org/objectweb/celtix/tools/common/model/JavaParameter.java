package org.objectweb.celtix.tools.common.model;
import com.sun.xml.bind.api.TypeReference;
public class JavaParameter extends JavaType {

    private boolean holder;
    private String holderName;
    private String holderClass;
    private JavaAnnotation annotation;
    private String partName;

    public JavaParameter() {
    }
    public JavaParameter(String pname, TypeReference pref , JavaType.Style pstyle) {
        name = pname;
        typeRef = pref;
        style = pstyle;
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

    public void setAnnotation(JavaAnnotation anno) {
        this.annotation = anno;
    }

    public JavaAnnotation getAnnotation() {
        return this.annotation;
    }

    public void setPartName(String name) {
        this.partName = name;
    }
    
    public String getPartName() {
        return getPartName();
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        if (holder) {
            sb.append("\nIS Holder: [Holder Name]:");
            sb.append(holderName);
        }
        sb.append("\n Annotation:");
        sb.append(annotation);
        
        sb.append("\n PartName");
        sb.append(partName);
        return sb.toString();
    }
}
