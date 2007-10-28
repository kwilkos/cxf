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


import java.lang.reflect.Method;
import java.util.List;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.AbstractInvoker;

public class JAXRSInvoker extends AbstractInvoker {
    private List<Object> resourceObjects;

    public JAXRSInvoker() {
    }
    
    public JAXRSInvoker(List<Object> resourceObjects) {
        this.resourceObjects = resourceObjects;
    }
    
    public Object invoke(Exchange exchange, Object o) {
        OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);

        ClassResourceInfo classResourceInfo = ori.getClassResourceInfo();
        Method m = classResourceInfo.getMethodDispatcher().getMethod(ori);
        Object resourceObject = getServiceObject(exchange);

        List<Object> params = null;
        if (o instanceof List) {
            params = CastUtils.cast((List<?>)o);
        } else if (o != null) {
            params = new MessageContentsList(o);
        }

        return invoke(exchange, resourceObject, m, params);
    }

    
    // REVISIT: Not sure how to deal with Resource class life cycle. The current
    // spec suggests two models, per-request and singleton, and it is the
    // reponsibility of JSR-311 runtime to create resource instances.
    public Object getServiceObject(Exchange exchange) {
        Object serviceObject = null;
        
        OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
        ClassResourceInfo classResourceInfo = ori.getClassResourceInfo();
        
        if (resourceObjects != null) {
            Class c  = classResourceInfo.getResourceClass();
            for (Object resourceObject : resourceObjects) {
                if (c.isInstance(resourceObject)) {
                    serviceObject = resourceObject;
                }
            }
        }
        
        if (serviceObject == null) {
            try {
                serviceObject = classResourceInfo.getResourceClass().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return serviceObject;
    }

}
