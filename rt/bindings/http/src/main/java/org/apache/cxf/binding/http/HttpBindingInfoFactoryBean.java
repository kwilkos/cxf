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
package org.apache.cxf.binding.http;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.cxf.service.factory.MethodDispatcher;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.codehaus.jra.Delete;
import org.codehaus.jra.Get;
import org.codehaus.jra.HttpResource;
import org.codehaus.jra.Post;
import org.codehaus.jra.Put;

public class HttpBindingInfoFactoryBean extends AbstractBindingInfoFactoryBean {

    private URIMapper mapper = new URIMapper();
    
    @Override
    public BindingInfo create() {
        BindingInfo info = new BindingInfo(getServiceInfo(), 
                                           HttpBindingFactory.HTTP_BINDING_ID);
        info.setName(new QName(getServiceInfo().getName().getNamespaceURI(), 
                               getServiceInfo().getName().getLocalPart() + "HttpBinding"));
        
        ReflectionServiceFactoryBean sf = (ReflectionServiceFactoryBean) getServiceFactory();
        Service service = sf.getService();
        MethodDispatcher md = (MethodDispatcher) service.get(MethodDispatcher.class.getName()); 

        for (OperationInfo o : getServiceInfo().getInterface().getOperations()) {
            BindingOperationInfo bop = info.buildOperation(o.getName(), o.getInputName(), o.getOutputName());

            info.addOperation(bop);
            
            Method m = md.getMethod(bop);
            
            HttpResource r = m.getAnnotation(HttpResource.class);
            if (r == null) {
                continue;
            }
            
            String verb;
            if (m.isAnnotationPresent(Get.class)) {
                verb = "GET";
            } else if (m.isAnnotationPresent(Post.class)) {
                verb = "POST";
            } else if (m.isAnnotationPresent(Put.class)) {
                verb = "PUT";
            } else if (m.isAnnotationPresent(Delete.class)) {
                verb = "DELETE";
            } else {
                // todo: use the method name to determine the verb
                verb = "GET";
            }
            
            mapper.bind(bop, r.location(), verb);
        }
        
        service.put(URIMapper.class.getName(), mapper);
        return info;
    }
}
