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

public class WebResultAnnotator implements Annotator {

    public void annotate(JavaAnnotatable ja) {
        JavaMethod method = null;
        if (ja instanceof JavaMethod) {
            method = (JavaMethod) ja;
        } else {
            throw new RuntimeException("WebResult can only annotate JavaMethod");
        }
            
        if (method.isOneWay()) {
            JavaAnnotation oneWayAnnotation = new JavaAnnotation("Oneway");
            method.addAnnotation("Oneway", oneWayAnnotation);
            method.getInterface().addImport("javax.jws.Oneway");
            return;
        }

        if ("void".equals(method.getReturn().getType())) {
            return;
        }
        JavaAnnotation resultAnnotation = new JavaAnnotation("WebResult");
        String targetNamespace = method.getReturn().getTargetNamespace();
        String name = "return";

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle()) {
            name = method.getName() + "Response";
        }

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            name = method.getReturn().getName();
            targetNamespace = method.getInterface().getNamespace();
           
        }
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT) {
            if (method.getReturn().getQName() != null) {
                name = method.getReturn().getQName().getLocalPart();
            }
            targetNamespace = method.getReturn().getTargetNamespace();
        }

        resultAnnotation.addArgument("name", name);
        resultAnnotation.addArgument("targetNamespace", targetNamespace);

        if (method.getSoapStyle() == SOAPBinding.Style.RPC
            || (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle())) {
            resultAnnotation.addArgument("partName", method.getReturn().getName());
        }

        method.addAnnotation("WebResult", resultAnnotation);
        method.getInterface().addImport("javax.jws.WebResult");
    }
}
