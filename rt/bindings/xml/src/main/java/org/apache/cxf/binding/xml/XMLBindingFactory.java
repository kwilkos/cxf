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
package org.apache.cxf.binding.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.xml.interceptor.XMLMessageInInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLMessageOutInterceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;

public class XMLBindingFactory extends AbstractBindingFactory {

    private Map cachedBinding = new HashMap<BindingInfo, Binding>();

    public Binding createBinding(BindingInfo binding) {

        if (cachedBinding.get(binding) != null) {
            return (Binding) cachedBinding.get(binding);
        }

        XMLBinding xb = new XMLBinding();
        
        xb.getInInterceptors().add(new StaxInInterceptor());
        xb.getInInterceptors().add(new XMLMessageInInterceptor());
        xb.getInFaultInterceptors().add(xb.getInFaultInterceptor());
        
        xb.getOutInterceptors().add(new StaxOutInterceptor());
        xb.getOutInterceptors().add(new XMLMessageOutInterceptor());
        xb.getOutFaultInterceptors().add(xb.getOutFaultInterceptor());

        return xb;
    }

}
