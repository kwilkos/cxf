package org.objectweb.celtix.tools.common;

import java.util.*;
import org.objectweb.celtix.tools.utils.URIParserUtil;

public class ProcessorEnvironment {

    private Map<String, Object> paramMap;
    private String packageName;
    private Map<String, String> namespacePackageMap = new HashMap<String, String>();

    public void setParameters(Map<String, Object> map) {
        this.paramMap = map;
    }
    
    public boolean containsKey(String key) {
        return (paramMap == null) ? false : paramMap.containsKey(key);
    }

    public Object get(String key) {
        return (paramMap == null) ? null : paramMap.get(key);
    }

    public void put(String key, Object value) {
        if (paramMap == null) {
            paramMap = new HashMap<String, Object>();
        }
        paramMap.put(key, value);
    }

    public void remove(String key) {
        if (paramMap == null) {
            return;
        }
        paramMap.remove(key);
    }

    public boolean optionSet(String key) {
        return (get(key) == null) ? false : true;
    }

    public boolean isVerbose() {
        if (get(ToolConstants.CFG_VERBOSE) == null) {
            return false;
        } else {
            return (Boolean) get(ToolConstants.CFG_VERBOSE);
        }
    }

    public void addNamespacePackageMap(String namespace, String pn) {
        this.namespacePackageMap.put(namespace, pn);
    }

    private String mapNamespaceToPackageName(String ns) {
        return this.namespacePackageMap.get(ns);
    }

    public boolean hasNamespace(String ns) {
        return this.namespacePackageMap.containsKey(ns);
    }

    public void setPackageName(String pkgName) {
        this.packageName = pkgName;
    }
    
    public String getPackageName() {
        return this.packageName;
    }

    public String mapPackageName(String ns) {
        if (hasNamespace(ns)) {
            return mapNamespaceToPackageName(ns);
        } else {
            return getPackageName();
        }
    }

    public String getCustomizedNS(String ns) {
        return URIParserUtil.getNamespace(mapPackageName(ns));
    }
}
