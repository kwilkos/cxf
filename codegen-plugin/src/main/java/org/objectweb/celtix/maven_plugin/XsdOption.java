package org.objectweb.celtix.maven_plugin;

import java.io.File;

public class XsdOption {
    String xsd;
    String packagename;
    String bindingFile;
    File dependencies[];
    File redundantDirs[];
    
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
    public String getBindingFile() {
        return bindingFile;
    }
    public void setBindingFile(String bf) {
        this.bindingFile = bf;
    }
    public void setDependencies(File files[]) {
        dependencies = files;
    }
    public File[] getDependencies() {
        return dependencies;
    }

    public void setDeleteDirs(File files[]) {
        redundantDirs = files;
    }
    public File[] getDeleteDirs() {
        return redundantDirs;
    }
}
