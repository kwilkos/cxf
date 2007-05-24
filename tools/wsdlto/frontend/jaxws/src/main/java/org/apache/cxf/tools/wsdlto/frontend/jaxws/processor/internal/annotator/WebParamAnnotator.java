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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.annotator;

import javax.jws.soap.SOAPBinding;

import org.apache.cxf.tools.common.model.Annotator;
import org.apache.cxf.tools.common.model.JavaAnnotatable;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaType;

public class WebParamAnnotator implements Annotator {
    public void annotate(JavaAnnotatable ja) {
        JavaParameter parameter = null;
        if (ja instanceof JavaParameter) {
            parameter = (JavaParameter) ja;
        } else {
            throw new RuntimeException("WebParamAnnotator only annotate the JavaParameter");
        }
        JavaMethod method = parameter.getMethod();
        JavaAnnotation webParamAnnotation = new JavaAnnotation("WebParam");
        String name = parameter.getName();
        String targetNamespace = method.getInterface().getNamespace();
        String partName = null;

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
            || parameter.isHeader()) {
            targetNamespace = parameter.getTargetNamespace();
            
            if (parameter.getQName() != null) {
                name = parameter.getQName().getLocalPart();
            }
            if (!method.isWrapperStyle()) {
                partName = parameter.getPartName();
            }
        }

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            name = parameter.getPartName();
            partName = parameter.getPartName();
        }

        if (partName != null) {
            webParamAnnotation.addArgument("partName", partName);
        }
        if (parameter.getStyle() == JavaType.Style.OUT || parameter.getStyle() == JavaType.Style.INOUT) {
            webParamAnnotation.addArgument("mode", "Mode." + parameter.getStyle().toString(), "");
        }
        webParamAnnotation.addArgument("name", name);
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT || parameter.isHeader()) {
            webParamAnnotation.addArgument("targetNamespace", targetNamespace);
        }

        parameter.setAnnotation(webParamAnnotation);
    }
}

