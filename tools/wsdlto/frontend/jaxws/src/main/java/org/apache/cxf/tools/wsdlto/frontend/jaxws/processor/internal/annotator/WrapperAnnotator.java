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

import org.apache.cxf.tools.common.model.Annotator;
import org.apache.cxf.tools.common.model.JavaAnnotatable;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;

public class WrapperAnnotator implements Annotator {
    JavaParameter wrapperRequest;
    JavaParameter wrapperResponse;

    public WrapperAnnotator(JavaParameter request, JavaParameter response) {
        wrapperRequest = request;
        wrapperResponse = response;
    }
    
    public void annotate(JavaAnnotatable ja) {
        JavaMethod method;
        if (ja instanceof JavaMethod) {
            method = (JavaMethod) ja;
        } else {
            throw new RuntimeException("RequestWrapper and ResponseWrapper can only annotate JavaMethod");
        }
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
