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
package org.apache.cxf.jaxws.binding.soap;

import java.lang.reflect.Method;

import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import org.apache.cxf.binding.soap.SoapBindingInfoFactoryBean;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.jaxws.support.AbstractJaxWsServiceFactoryBean;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;

/**
 * Introspects the SOAPBinding annotation to provide to construct
 * a {@link org.apache.cxf.service.model.BindingInfo}.
 */
public class JaxWsSoapBindingInfoFactoryBean extends SoapBindingInfoFactoryBean {

    @Override
    protected String getStyle() {
        SOAPBinding sb = getServiceClass().getAnnotation(SOAPBinding.class);
        if (sb != null) {
            if (sb.style().equals(Style.DOCUMENT)) {
                return "document";
            } else if (sb.style().equals(Style.RPC)) {
                return "rpc";
            }
        }
        return super.getStyle();
    }

    Class<?> getServiceClass() {
        return getJaxWsServiceFactory().getJaxWsImplementorInfo()
            .getEndpointClass();
    }

    private AbstractJaxWsServiceFactoryBean getJaxWsServiceFactory() {
        return (AbstractJaxWsServiceFactoryBean)getServiceFactory();
    }
    
    @Override
    public String getUse() {
        SOAPBinding sb = getServiceClass().getAnnotation(SOAPBinding.class);
        if (sb != null) {
            if (sb.use().equals(Use.LITERAL)) {
                return "literal";
            } else if (sb.use().equals(Use.ENCODED)) {
                return "encoded";
            }
        }
        return super.getStyle();
    }

    @Override
    protected boolean isHeader(BindingOperationInfo op, MessagePartInfo part) {
        MethodDispatcher md = (MethodDispatcher) getService().get(MethodDispatcher.class.getName());
        Method method = md.getMethod(op);
        
        return getJaxWsServiceFactory().isHeader(method, part.getIndex());
    }
    
    
}
