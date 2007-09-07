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
    public JAXRSInvoker() {
    }

    public Object invoke(Exchange exchange, Object o) {
        OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);

        ClassResourceInfo classResourceInfo = ori.getClassResourceInfo();
        Method m = classResourceInfo.getMethodDispatcher().getMethod(ori);
        Object serviceObject = null;
        try {
            serviceObject = classResourceInfo.getResourceClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }


/*        MethodDispatcher md = (MethodDispatcher)
            exchange.get(Service.class).get(MethodDispatcher.class.getName());
        Method m = md.getMethod(bop);*/
        //Method m = (Method)bop.getOperationInfo().getProperty(Method.class.getName());
        //m = matchMethod(m, serviceObject);

        List<Object> params = null;
        if (o instanceof List) {
            params = CastUtils.cast((List<?>)o);
        } else if (o != null) {
            params = new MessageContentsList(o);
        }

        return invoke(exchange, serviceObject, m, params);
    }

    public Object getServiceObject(Exchange ex) {
        return null;
    }

}
