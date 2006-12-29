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

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.model.Annotator;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.util.SOAPBindingUtil;

public class WebMethodAnnotator implements Annotator {
   
    public void addSOAPBindingAnnotation(JavaMethod method) {
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle()) {
            JavaAnnotation bindingAnnotation = new JavaAnnotation("SOAPBinding");
            bindingAnnotation.addArgument("parameterStyle", SOAPBindingUtil.getBindingAnnotation("BARE"), "");
            method.addAnnotation("SOAPBinding", bindingAnnotation);
        }
    }

    public void addWebMethodAnnotation(JavaMethod method) {
        addWebMethodAnnotation(method, method.getOperationName());
    }

    public void addWebMethodAnnotation(JavaMethod method, String operationName) {
        JavaAnnotation methodAnnotation = new JavaAnnotation("WebMethod");
        methodAnnotation.addArgument("operationName", operationName);
        if (!StringUtils.isEmpty(method.getSoapAction())) {
            methodAnnotation.addArgument("action", method.getSoapAction());
        }
        method.addAnnotation("WebMethod", methodAnnotation);
        method.getInterface().addImport("javax.jws.WebMethod");
    }

    public void addWebResultAnnotation(JavaMethod method) {
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

    public void addWrapperAnnotation(JavaMethod method,
                                     JavaParameter wrapperRequest,
                                     JavaParameter wrapperResponse) {
        if (wrapperRequest != null) {
            JavaAnnotation wrapperRequestAnnotation = new JavaAnnotation("RequestWrapper");
            wrapperRequestAnnotation.addArgument("localName", wrapperRequest.getType());
            wrapperRequestAnnotation.addArgument("targetNamespace", wrapperRequest.getTargetNamespace());
            wrapperRequestAnnotation.addArgument("className", wrapperRequest.getClassName());
            method.addAnnotation("RequestWrapper", wrapperRequestAnnotation);
            method.getInterface().addImport("javax.xml.ws.RequestWrapper");
        }
        if (wrapperResponse != null) {
            JavaAnnotation wrapperResponseAnnotation = new JavaAnnotation("ResponseWrapper");
            wrapperResponseAnnotation.addArgument("localName", wrapperResponse.getType());
            wrapperResponseAnnotation.addArgument("targetNamespace", wrapperResponse.getTargetNamespace());
            wrapperResponseAnnotation.addArgument("className", wrapperResponse.getClassName());
            method.addAnnotation("ResponseWrapper", wrapperResponseAnnotation);
            method.getInterface().addImport("javax.xml.ws.ResponseWrapper");
        }

    }
}
