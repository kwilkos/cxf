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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal;

import java.util.*;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.util.SOAPBindingUtil;

public class SEIAnnotationProcessor extends AbstractProcessor {

    public SEIAnnotationProcessor(ToolContext penv) {
        super(penv);
    }
    
    public void process(ServiceInfo service) {
        JavaModel javaModel = context.get(JavaModel.class);
        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String)iter.next();
            JavaInterface intf = interfaces.get(interfaceName);
            
            JavaAnnotation serviceAnnotation = new JavaAnnotation("WebService");
            serviceAnnotation.addArgument("targetNamespace", intf.getNamespace());
            serviceAnnotation.addArgument("wsdlLocation", intf.getLocation());
            serviceAnnotation.addArgument("name", intf.getWebServiceName());

            intf.addAnnotation(serviceAnnotation.toString());
            
            if (service.getBindings().size() == 0) {
                return;
            }
            if (processBinding(intf)) {
                JavaAnnotation bindingAnnotation = new JavaAnnotation("SOAPBinding");
                String style = SOAPBindingUtil.getBindingAnnotation(intf.getSOAPStyle().toString());
                bindingAnnotation.addArgument("style", style, "");
                String use = SOAPBindingUtil.getBindingAnnotation(intf.getSOAPUse().toString());
                bindingAnnotation.addArgument("use", use, "");
                if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT) {
                    String parameterStyle = SOAPBindingUtil.getBindingAnnotation(intf.
                                                                                 getSOAPParameterStyle().
                                                                                 toString());
                    bindingAnnotation.addArgument("parameterStyle", parameterStyle, "");
                }
                intf.addAnnotation(bindingAnnotation.toString());
            }
        }        
    }

    private boolean processBinding(JavaInterface intf) {
        SOAPBinding.Style soapStyle = intf.getSOAPStyle();
        SOAPBinding.Use soapUse = intf.getSOAPUse();
        boolean isWrapped = true;
        int count = 0;
        for (JavaMethod method : intf.getMethods()) {
            if (!method.isWrapperStyle()) {
                isWrapped = false;
                count++;
            }
            if (soapStyle == null
                && method.getSoapStyle() != null) {
                soapStyle = method.getSoapStyle();
            }
            if (soapUse == null
                && method.getSoapUse() != null) {
                soapUse = method.getSoapUse();
            }
        }

        if (soapStyle == SOAPBinding.Style.DOCUMENT) {
            intf.setSOAPStyle(SOAPBinding.Style.DOCUMENT);
            if (isWrapped) {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.WRAPPED);
            } else {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.BARE);
            }
        } else if (soapStyle == null) {
            intf.setSOAPStyle(SOAPBinding.Style.DOCUMENT);
            if (isWrapped) {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.WRAPPED);
            } else {
                intf.setSOAPParameterStyle(SOAPBinding.ParameterStyle.BARE);
            }
            
        } else {
            intf.setSOAPStyle(SOAPBinding.Style.RPC);
        }
        
        if (soapUse == SOAPBinding.Use.LITERAL) {
            intf.setSOAPUse(SOAPBinding.Use.LITERAL);
        } else if (soapUse == null) {
            intf.setSOAPUse(SOAPBinding.Use.LITERAL);
        } else {
            intf.setSOAPUse(SOAPBinding.Use.ENCODED);
        }

        if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT
            && count != 0
            && count != intf.getMethods().size()) {
            return false;
        }

        if (intf.getSOAPStyle() == SOAPBinding.Style.DOCUMENT
            && intf.getSOAPUse() == SOAPBinding.Use.LITERAL
            && intf.getSOAPParameterStyle() == SOAPBinding.ParameterStyle.WRAPPED) {
            return false;
        }
        return true;
    }
}
