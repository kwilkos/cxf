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

package org.apache.cxf.tools.java2wsdl.generator.wsdl11.annotator;

import org.apache.cxf.tools.common.model.Annotator;
import org.apache.cxf.tools.common.model.JavaAnnotatable;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.java2wsdl.generator.wsdl11.model.WrapperBeanClass;

public class WrapperBeanAnnotator implements Annotator {

    public void annotate(final JavaAnnotatable clz) {
        WrapperBeanClass wrapperBeanClass = null;
        if (clz instanceof WrapperBeanClass) {
            wrapperBeanClass = (WrapperBeanClass) clz;
        } else {
            throw new RuntimeException("WrapperBeanAnnotator expect JavaClass as input");
        }

        JavaAnnotation xmlRootElement = new JavaAnnotation("XmlRootElement");
        xmlRootElement.addArgument("name", wrapperBeanClass.getElementName().getLocalPart());
        xmlRootElement.addArgument("namespace", wrapperBeanClass.getElementName().getNamespaceURI());

        JavaAnnotation xmlAccessorType = new JavaAnnotation("XmlAccessorType");
        xmlAccessorType.addArgument("XmlAccessType.FIELD", "null", "");

        JavaAnnotation xmlType = new JavaAnnotation("XmlType");
        xmlType.addArgument("name", wrapperBeanClass.getElementName().getLocalPart());
        xmlType.addArgument("namespace", wrapperBeanClass.getElementName().getNamespaceURI());

        // Revisit: why annotation is string?
        wrapperBeanClass.addAnnotation(xmlRootElement.toString());
        wrapperBeanClass.addAnnotation(xmlAccessorType.toString());
        wrapperBeanClass.addAnnotation(xmlType.toString());

        wrapperBeanClass.addImport("javax.xml.bind.annotation.XmlAccessType");
        wrapperBeanClass.addImport("javax.xml.bind.annotation.XmlAccessorType");
        wrapperBeanClass.addImport("javax.xml.bind.annotation.XmlRootElement");
        wrapperBeanClass.addImport("javax.xml.bind.annotation.XmlType");
    }
}
