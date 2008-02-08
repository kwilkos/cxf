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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.jaxrs.JAXRSUtils;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactoryImpl;
import org.apache.cxf.jaxrs.provider.ResponseImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.Phase;

public class JAXRSOutInterceptor extends AbstractOutDatabindingInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXRSOutInterceptor.class);

    public JAXRSOutInterceptor() {
        super(Phase.MARSHAL);
    }

    @SuppressWarnings("unchecked")
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
                
                message.put(Message.RESPONSE_CODE, response.getStatus());
                if (response instanceof ResponseImpl) {
                    ((ResponseImpl)response).serializeMetadata(message);
                }
                
                responseObj = response.getEntity();
                if (responseObj == null) {
                    return;
                }
            } 
            
            Class targetType = responseObj.getClass();
            String[] availableContentTypes = computeAvailableContentTypes(message);  
            
            StringBuffer typesTmp = new StringBuffer();
            for (String type : availableContentTypes) {
                typesTmp.append(type + ",");
            }
            LOG.info("Available content types for response is: " + typesTmp);

            EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
                .createEntityProvider(targetType, availableContentTypes, false);
            LOG.info("Response EntityProvider is: " + provider.getClass().getName());

            try {
                String outputContentType = computeFinalContentTypes(availableContentTypes, provider);
                LOG.info("Response content type is: " + outputContentType);
               
                message.put(Message.CONTENT_TYPE, computeFinalContentTypes(availableContentTypes, provider));
                
                provider.writeTo(responseObj, null, null, out);
            } catch (IOException e) {
                e.printStackTrace();
            }        
            
        }

    }
    
    private String[] computeAvailableContentTypes(Message message) {
        Exchange exchange = message.getExchange();
        
        String[] methodMimeTypes = exchange.get(OperationResourceInfo.class).getProduceMimeTypes();
        String acceptContentType = (String)exchange.get(Message.ACCEPT_CONTENT_TYPE);

        List<String> types = new ArrayList<String>();
        if (acceptContentType != null) {
            while (acceptContentType.length() > 0) {
                String tp = acceptContentType;
                if (acceptContentType.contains(",")) {
                    tp = acceptContentType.substring(0, acceptContentType.indexOf(','));
                    acceptContentType = acceptContentType
                        .substring(acceptContentType.indexOf(',') + 1).trim();
                } else {
                    acceptContentType = "";
                }
                try {
                    MimeType mt = new MimeType(tp);
                    types.add(mt.getBaseType());
                } catch (MimeTypeParseException e) {
                    // ignore
                }
            }
        }
        if (types.isEmpty()) {
            types.add("*/*");
        }
        
        return JAXRSUtils.intersectMimeTypes(methodMimeTypes,
                                             types.toArray(new String[types.size()]));        
    }
    
    private String computeFinalContentTypes(String[] requestContentTypes, EntityProvider provider) {
        String[] providerMimeTypes = {"*/*"};            

        ProduceMime c = provider.getClass().getAnnotation(ProduceMime.class);
        if (c != null) {
            providerMimeTypes = c.value();               
        } 
        
        String[] list = JAXRSUtils.intersectMimeTypes(requestContentTypes, providerMimeTypes);
        return list[0];      
    }
}
