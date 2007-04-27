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

package org.apache.cxf.tools.java2wsdl.processor.internal.jaxws;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaClass;
import org.apache.cxf.tools.util.AnnotationUtil;
import org.apache.cxf.tools.util.NameUtil;
import org.apache.cxf.tools.util.URIParserUtil;

public class Wrapper {
    private static final Logger LOG = LogUtils.getL7dLogger(Wrapper.class);
    private QName name;
    private JavaClass javaClass;
    private Method mehtod;

    public void setMethod(Method m) {
        this.mehtod = m;
    }

    public void setName(QName n) {
        this.name = n;
    }

    public JavaClass getWrapperBeanClass(Method m) {
        return new JavaClass();
    }
    
    protected JavaClass getWrapperBeanClass(QName wrapperBeanName) {
        JavaClass jClass = new JavaClass();
        if (wrapperBeanName == null) {
            return jClass;
        }

        String ns = wrapperBeanName.getNamespaceURI();
        jClass.setNamespace(ns);
        jClass.setPackageName(URIParserUtil.getPackageName(ns));
        jClass.setName(NameUtil.mangleNameToClassName(wrapperBeanName.getLocalPart()));
        return jClass;
    }

    private JavaClass merge(final JavaClass c1, final JavaClass c2) {
        if (c1.getNamespace() == null) {
            c1.setNamespace(c2.getNamespace());
        }

        if (c1.getPackageName() == null) {
            c1.setPackageName(c2.getPackageName());
        }

        if (c1.getName() == null) {
            c1.setName(c2.getName());
        }
        return c1;
    }
    
    public JavaClass getJavaClass() {
        if (javaClass == null) {
            JavaClass jClass1 = getWrapperBeanClass(this.name);
            JavaClass jClass2 = getWrapperBeanClass(this.mehtod);
            javaClass = merge(jClass2, jClass1);
        }
        return javaClass;
    }

    protected String getPackageName(final Method m) {
        return m.getDeclaringClass().getPackage().getName();
    }

    public boolean isWrapperAbsent() {
        return isWrapperAbsent(this.mehtod);
    }

    public boolean isWrapperAbsent(final Method m) {
        return false;
    }

    public boolean isWrapperBeanClassNotExist() {
        try {
            Message msg = new Message("LOADING_WRAPPER_CLASS", LOG, getJavaClass().getFullClassName());
            LOG.log(Level.INFO, msg.toString());
            getWrapperClass();
            return false;
        } catch (ToolException e) {
            return true;
        }
    }

    public Class getWrapperClass() {
        try {
            return AnnotationUtil.loadClass(getJavaClass().getFullClassName(),
                                            getClass().getClassLoader());
        } catch (Exception e) {
            Message msg = new Message("LOAD_WRAPPER_CLASS_FAILED", LOG, getJavaClass().getFullClassName());
            LOG.log(Level.WARNING, msg.toString());
            throw new ToolException(msg);
        }
    }
}
