package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassCollector {

    private final Map<String, String> seiClassNames = new HashMap<String, String>();
    private final Map<String, String> typesClassNames = new HashMap<String, String>();
    private final Map<String, String> exceptionClassNames = new HashMap<String, String>();
    private final Map<String, String> serviceClassNames = new HashMap<String, String>();
    private final Map<String, String> implClassNames = new HashMap<String, String>();
    private final Map<String, String> clientClassNames = new HashMap<String, String>();
    private final Map<String, String> serverClassNames = new HashMap<String, String>();
    

    public boolean containSeiClass(String packagename, String type) {
        return seiClassNames.containsKey(key(packagename, type));
    }

    public boolean containTypesClass(String packagename, String type) {
        return typesClassNames.containsKey(key(packagename, type));
    }

    public boolean containExceptionClass(String packagename, String type) {
        return exceptionClassNames.containsKey(key(packagename, type));
    }

    public void addSeiClassName(String packagename, String type, String fullClassName) {
        seiClassNames.put(key(packagename, type), fullClassName);
    }

    public void addTypesClassName(String packagename, String type, String fullClassName) {
        typesClassNames.put(key(packagename, type), fullClassName);
    }

    public void addServerClassName(String packagename, String type, String fullClassName) {
        serverClassNames.put(key(packagename, type), fullClassName);
    }

    public void addImplClassName(String packagename, String type, String fullClassName) {
        implClassNames.put(key(packagename, type), fullClassName);
    }

    public void addClientClassName(String packagename, String type, String fullClassName) {
        clientClassNames.put(key(packagename, type), fullClassName);
    }

    public void addServiceClassName(String packagename, String type, String fullClassName) {
        serviceClassNames.put(key(packagename, type), fullClassName);
    }

    public void addExceptionClassName(String packagename, String type, String fullClassName) {
        exceptionClassNames.put(key(packagename, type), fullClassName);
    }

    public String getTypesFullClassName(String packagename, String type) {
        return typesClassNames.get(key(packagename, type));
    }

    private String key(String packagename, String type) {
        return packagename + "#" + type;
    }

    public List<String> getGeneratedFileInfo() {
        List<String> generatedFileList = new ArrayList<String>();
        generatedFileList.addAll(seiClassNames.values());
        generatedFileList.addAll(typesClassNames.values());
        generatedFileList.addAll(exceptionClassNames.values());
        generatedFileList.addAll(serviceClassNames.values());
        generatedFileList.addAll(implClassNames.values());
        generatedFileList.addAll(clientClassNames.values());
        return generatedFileList;
    }

}
