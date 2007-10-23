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

package org.apache.cxf.jaxrs.interceptor;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactoryImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.Phase;

public class JAXRSOutInterceptor extends AbstractOutDatabindingInterceptor {

    public JAXRSOutInterceptor() {
        super(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        Exchange exchange = message.getExchange();
        OperationResourceInfo operation = (OperationResourceInfo)exchange.get(OperationResourceInfo.class
            .getName());

        if (operation == null) {
            return;
        }

        MessageContentsList objs = MessageContentsList.getContentsList(message);
        if (objs == null || objs.size() == 0) {
            return;
        }

        OutputStream out = message.getContent(OutputStream.class);
        
        if (objs.get(0) != null) {
            Object responseObj = objs.get(0);
            if (objs.get(0) instanceof Response) {
                Response response = (Response)responseObj;
                responseObj = response.getEntity();   

                message.put(Message.RESPONSE_CODE, response.getStatus());
                
                if (responseObj == null) {
                    return;
                }
            } 
            
            Class targetType = responseObj.getClass();
/*            if (responseObj.getClass().isArray()) {
                targetType = responseObj.getClass().getComponentType();
            } else if (responseObj instanceof List && ((List)responseObj).get(0) != null) {
                //NOTE: if its a List, the provider should try to determine if it can support
                //every object inside the List instead of the first one only.
                targetType = ((List)responseObj).get(0).getClass();
                
            }*/
 
            //TODO: decide the output media type based on resource method/resource class/provider
            String[] methodMimeTypes = exchange.get(OperationResourceInfo.class).getProduceMimeTypes();
            
            EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
                .createEntityProvider(targetType, methodMimeTypes, false);

            try {
                if (!"*/*".equals(methodMimeTypes[0])) {
                    message.put(Message.CONTENT_TYPE, methodMimeTypes[0]);
                }
                
                provider.writeTo(responseObj, null, null, out);

            } catch (IOException e) {
                e.printStackTrace();
            }        
            
        }

    }

}
