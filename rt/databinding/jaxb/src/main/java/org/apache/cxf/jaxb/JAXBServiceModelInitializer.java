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

package org.apache.cxf.jaxb;

import java.lang.reflect.AnnotatedElement;
import java.util.ResourceBundle;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;

/**
 * Walks the service model and sets up the element/type names.
 */
class JAXBServiceModelInitializer extends ServiceModelVisitor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXBServiceModelInitializer.class);

    public JAXBServiceModelInitializer(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public void begin(MessagePartInfo part) {
        Class<?> clazz = (Class<?>)part.getProperty(Class.class.getName());

        XmlRootElement root = (XmlRootElement)clazz.getAnnotation(XmlRootElement.class);
        XmlType type = (XmlType)clazz.getAnnotation(XmlType.class);
        String local = null;
        String nsUri = null;
        boolean isElement = false;

        if (root != null) {
            isElement = false;
            local = root.name();
            nsUri = root.namespace();
        } else if (type != null) {
            isElement = true;
            local = type.name();
            nsUri = type.namespace();
        } else if (clazz.isAnnotationPresent(XmlEnum.class)) {
            isElement = true;
            local = clazz.getSimpleName();
            nsUri = "##default";
        } else {
            // we've got a non JAXB bean - i.e. String, etc
            return;
        }

        if ("##default".equals(local)) {
            local = clazz.getSimpleName();
        }

        if ("##default".equals(nsUri)) {
            nsUri = getPackageNs(clazz);
        }

        part.setIsElement(isElement);
        if (isElement) {
            part.setElementQName(new QName(nsUri, local));
        } else {
            part.setTypeQName(new QName(nsUri, local));
        }
    }

    public static String getPackageNs(Class clazz) {
        AnnotatedElement pack = clazz.getPackage();
        // getPackage isn't guaranteed to return a package
        if (pack == null) {
            try {
                pack = ClassLoaderUtils.loadClass(
                    clazz.getName().substring(0, clazz.getName().lastIndexOf('.')) + ".package-info", clazz);
            } catch (Exception ex) {
                // do nothing
            }
        }

        if (pack == null) {
            throw new UncheckedException(new Message("UNKNOWN_PACKAGE_NS", BUNDLE, clazz));
        }

        javax.xml.bind.annotation.XmlSchema schema = pack
            .getAnnotation(javax.xml.bind.annotation.XmlSchema.class);
        String namespace = null;
        if (schema != null) {
            namespace = schema.namespace();
        } else {
            namespace = "";
        }
        return namespace;
    }
}
