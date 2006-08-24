package org.apache.cxf.configuration;

import java.util.ResourceBundle;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;


public class CompoundName {

    public static final String SEPARATOR_STR = ".";
    
    private static final ResourceBundle BUNDLE = 
        BundleUtils.getBundle(CompoundName.class, "APIMessages");
    
    
    private final String[] parts;
    private final int hash;
    
    public CompoundName(String... p) {
        if (null == p || 0 == p.length) {
            throw new ConfigurationException(new Message("NO_PARTS_EXC", BUNDLE));
        }
        for (int i = 0; i < p.length; i++) {
            if (null == p[i]) {
                throw new ConfigurationException(new Message("INVALID_PART_EXC", BUNDLE));
            }
        }
        parts = p;
        int h  = 7;
        for (int i = 0; i < parts.length; i++) {
            h = h * 31 + parts[i].hashCode();
        }
        hash = h;
    }
    
    public CompoundName(CompoundName parent, String sfx) {
        if (null == sfx) {
            throw new ConfigurationException(new Message("INVALID_PART_EXC", BUNDLE));
        }
        parts = new String[parent.parts.length + 1];
        System.arraycopy(parent.parts, 0, parts, 0, parent.parts.length);
        parts[parent.parts.length] = sfx;                 
        
        hash = parent.hashCode() * 31 + sfx.hashCode();
    }
    
    

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof CompoundName) {
            CompoundName other = (CompoundName)obj;
            if (parts.length != other.parts.length) {
                return false;
            }
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].equals(other.parts[i])) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                buf.append(SEPARATOR_STR);
            }
            buf.append(parts[i]);
        }
        return buf.toString();
    }

    public CompoundName getParentName() {
        if (parts.length > 1) {
            String[] pparts = new String[parts.length - 1];
            System.arraycopy(parts, 0, pparts, 0, pparts.length);
            return new CompoundName(pparts);
        }
        return null;
    }
    
    
}

