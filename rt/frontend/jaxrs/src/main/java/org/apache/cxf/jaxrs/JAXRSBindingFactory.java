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
package org.apache.cxf.jaxrs;


import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;

import org.apache.cxf.binding.xml.XMLBinding;
import org.apache.cxf.jaxrs.interceptor.JAXRSDispatchInterceptor;
import org.apache.cxf.jaxrs.interceptor.JAXRSOutInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;

public class JAXRSBindingFactory extends AbstractBindingFactory {

    public static final String JAXRS_BINDING_ID = "http://apache.org/cxf/binding/jaxrs";

    public JAXRSBindingFactory() {
    }

    public Binding createBinding(BindingInfo bi) {
        XMLBinding binding = new XMLBinding(bi);

        binding.getInInterceptors().add(new JAXRSDispatchInterceptor());
        binding.getOutInterceptors().add(new JAXRSOutInterceptor());
        
        //TODO: Add fault interceptors

        return binding;
    }

    /*
     * The concept of Binding is not used in this JAX-RS impl. Here we use
     * Binding merely to make this JAX-RS impl compatible with CXF framework
     */
    public BindingInfo createBindingInfo(Service service, String namespace, Object obj) {
        BindingInfo info = new BindingInfo(null, JAXRSBindingFactory.JAXRS_BINDING_ID);

        return info;
    }


}
