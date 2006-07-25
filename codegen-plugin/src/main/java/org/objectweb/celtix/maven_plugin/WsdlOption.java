package org.objectweb.celtix.maven_plugin;

import java.io.File;
import java.util.List;

public class WsdlOption {
    String wsdl;
    List packagenames;
    List extraargs;
    File dependencies[];
    File redundantDirs[];

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
