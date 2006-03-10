package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.Collection;

import javax.wsdl.PortType;

import com.sun.tools.xjc.api.ClassNameAllocator;

import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class ClassNameAllocatorImpl implements ClassNameAllocator {
    private static final String TYPE_SUFFIX = "_Type";
    private Collection<PortType> portTypes;
    private ClassCollector collector;
    public ClassNameAllocatorImpl(ClassCollector classCollector) {
        collector = classCollector;
    }

    private boolean isNameCollision(String packageName, String className) {
        return collector.containSeiClass(packageName, className);
    }

    public String assignClassName(String packageName, String className) {
        String fullClzName = className;
        if (isNameCollision(packageName, className)) {
            fullClzName = className + TYPE_SUFFIX;
        }
        collector.addTypesClassName(packageName, className, packageName + "." + fullClzName);
        return fullClzName;
    }

    public void setPortTypes(Collection<PortType> types, String packageName) {
        portTypes = types;
        setSeiClassNames(packageName);
    }

    private void setSeiClassNames(String packageName) {
        for (PortType porttype : portTypes) {
            String ns = porttype.getQName().getNamespaceURI();
            String type = porttype.getQName().getLocalPart();
            String pkgName = ProcessorUtil.parsePackageName(ns, packageName);
            String className = ProcessorUtil.mangleNameToClassName(type);
            String fullClassName = pkgName + "." + className;
            if (packageName == null) {
                packageName = pkgName;
            }
            collector.addSeiClassName(packageName, className, fullClassName);
        }
    }
}
