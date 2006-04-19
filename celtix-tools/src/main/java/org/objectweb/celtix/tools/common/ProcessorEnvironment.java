package org.objectweb.celtix.tools.common;

import java.io.*;
import java.util.*;
import org.xml.sax.InputSource;
import org.objectweb.celtix.tools.utils.PropertyUtil;
import org.objectweb.celtix.tools.utils.URIParserUtil;

public class ProcessorEnvironment {

    private Map<String, Object> paramMap;
    private String packageName;
    private Map<String, String> namespacePackageMap = new HashMap<String, String>();
    private Map<String, String> excludeNamespacePackageMap = new HashMap<String, String>();
    private final Map<String, InputSource> jaxbBindingFiles = new HashMap<String, InputSource>();

    public ProcessorEnvironment() {
    }
    
    public void loadDefaultNS2Pck()  {
        try {
            PropertyUtil properties = new PropertyUtil();
            properties.load(getResourceAsStream("toolspec/toolspecs/namespace2package.cfg"));
            namespacePackageMap.putAll(properties.getMaps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadDefaultExcludes()  {
        try {
            PropertyUtil properties = new PropertyUtil();
            properties.load(getResourceAsStream("toolspec/toolspecs/wsdltojavaexclude.cfg"));
            excludeNamespacePackageMap.putAll(properties.getMaps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getResourceAsStream(String file) throws IOException {
        return ProcessorEnvironment.class.getResourceAsStream(file);
    }
    
    public void setParameters(Map<String, Object> map) {
        this.paramMap = map;
    }
    
    public boolean containsKey(String key) {
        return (paramMap == null) ? false : paramMap.containsKey(key);
    }

    public Object get(String key) {
        return (paramMap == null) ? null : paramMap.get(key);
    }

    public Object get(String key, Object defaultValue) {
        if (!optionSet(key)) {
            return defaultValue;
        } else {
            return get(key);
        }
    }

    public boolean getBooleanValue(String key, String defaultValue) {
        return Boolean.valueOf((String) get(key, defaultValue)).booleanValue();
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
            return get(ToolConstants.CFG_VERBOSE) == ToolConstants.CFG_VERBOSE;
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

    public void addExcludeNamespacePackageMap(String namespace, String pn) {
        this.excludeNamespacePackageMap.put(namespace, pn);
    }

    public boolean hasExcludeNamespace(String ns) {
        return this.excludeNamespacePackageMap.containsKey(ns);
    }

    public String getExcludePackageName(String ns) {
        return this.excludeNamespacePackageMap.get(ns);
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

    public void addJaxbBindingFile(String location, InputSource is) {
        this.jaxbBindingFiles.put(location, is);
    }

    public Map<String, InputSource> getJaxbBindingFile() {
        return this.jaxbBindingFiles;
    }

    public boolean isExcludeNamespaceEnabled() {        
        return excludeNamespacePackageMap.size() > 0;
    }
}
