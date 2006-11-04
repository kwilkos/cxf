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
package org.apache.cxf.jaxws.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

// Do some context mapping work from rt-core to jaxws stander
// NOTE if there some change in cxf Message property name, this class should be update

public final class ContextPropertiesMapping {    
    
    private static Map<String, String> cxf2jaxwsMap = new HashMap<String, String>();
    private static Map<String, String> jaxws2cxfMap = new HashMap<String, String>();
    
    static {
        cxf2jaxwsMap.put(Message.ENDPOINT_ADDRESS, 
                          BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        cxf2jaxwsMap.put(Message.USERNAME,
                          BindingProvider.USERNAME_PROPERTY);
        cxf2jaxwsMap.put(Message.PASSWORD,
                          BindingProvider.PASSWORD_PROPERTY);
        
        cxf2jaxwsMap.put(Message.HTTP_REQUEST_METHOD,
                          MessageContext.HTTP_REQUEST_METHOD);
        cxf2jaxwsMap.put(Message.RESPONSE_CODE, 
                          MessageContext.HTTP_RESPONSE_CODE);        
        cxf2jaxwsMap.put(Message.PATH_INFO, 
                          MessageContext.PATH_INFO);
        cxf2jaxwsMap.put(Message.QUERY_STRING, 
                          MessageContext.QUERY_STRING);
        
        jaxws2cxfMap.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                         Message.ENDPOINT_ADDRESS);
        jaxws2cxfMap.put(BindingProvider.USERNAME_PROPERTY,
                         Message.USERNAME);
        jaxws2cxfMap.put(BindingProvider.PASSWORD_PROPERTY,
                         Message.PASSWORD);
                
        jaxws2cxfMap.put(MessageContext.HTTP_REQUEST_METHOD,
                         Message.HTTP_REQUEST_METHOD);
        jaxws2cxfMap.put(MessageContext.HTTP_RESPONSE_CODE,
                         Message.RESPONSE_CODE);        
        jaxws2cxfMap.put(MessageContext.PATH_INFO,
                         Message.PATH_INFO);
        jaxws2cxfMap.put(MessageContext.QUERY_STRING,
                         Message.QUERY_STRING);
        
    }
    
    private ContextPropertiesMapping() {
        
    }
    
    private static void mapContext(Map<String, Object> context, Map<String, String> map) {
        Set<String> keyset = context.keySet();
        String[] keys = new String[0];
        keys = keyset.toArray(keys);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String mappingString = map.get(key);
            if (null != mappingString) {
                Object obj = context.get(key);
                context.put(mappingString, obj);
            }
        }
    }
   
    public static void mapRequestfromJaxws2Cxf(Map<String, Object> context) {
        //deal with PROTOCOL_HEADERS mapping  
        Object requestHeaders = 
            context.get(MessageContext.HTTP_REQUEST_HEADERS);
        if (null != requestHeaders) {
            context.put(Message.PROTOCOL_HEADERS, requestHeaders);
        }       
        mapJaxws2Cxf(context);
    }
    
    public static void mapResponsefromCxf2Jaxws(Map<String, Object> context) {
        //deal with PROTOCOL_HEADERS mapping
        Object responseHeaders = 
            context.get(Message.PROTOCOL_HEADERS);
        if (null != responseHeaders) {
            context.put(MessageContext.HTTP_RESPONSE_HEADERS, responseHeaders);
        }  
        mapCxf2Jaxws(context);
    }
    
    private static void mapJaxws2Cxf(Map<String, Object> context) {
        mapContext(context, jaxws2cxfMap);
    }
        
    private static void mapCxf2Jaxws(Map<String, Object> context) {
        mapContext(context, cxf2jaxwsMap);
    }
    
    
    public static MessageContext createWebServiceContext(Exchange exchange) {
        MessageContext ctx = new WrappedMessageContext(exchange.getInMessage());
        mapCxf2Jaxws(ctx);        
        Object requestHeaders = 
            exchange.getInMessage().get(Message.PROTOCOL_HEADERS);
        if (null != requestHeaders) {
            ctx.put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
        }       
               
        Message outMessage = exchange.getOutMessage();
        if (null != outMessage) {
            Object responseHeaders =
                outMessage.get(Message.PROTOCOL_HEADERS);
            if (responseHeaders == null) {
                responseHeaders = new HashMap<String, List<String>>();
                outMessage.put(Message.PROTOCOL_HEADERS, responseHeaders);         
            }
            ctx.put(MessageContext.HTTP_RESPONSE_HEADERS, responseHeaders);
        }   
        return ctx;
    }
    
    public static void updateWebServiceContext(Exchange exchange, MessageContext ctx) {
        //get the context response code and setback to out message
        if (ctx.containsKey(MessageContext.HTTP_RESPONSE_CODE)) {
            exchange.getOutMessage().put(Message.RESPONSE_CODE, ctx.get(MessageContext.HTTP_RESPONSE_CODE));
        }
    }
   

}
