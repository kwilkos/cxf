/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.util.Collection;

import javax.wsdl.PortType;

import com.sun.tools.xjc.api.ClassNameAllocator;

import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.ProcessorUtil;

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
