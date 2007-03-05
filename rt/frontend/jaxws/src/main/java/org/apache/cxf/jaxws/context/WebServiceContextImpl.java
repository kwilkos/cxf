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

package org.apache.cxf.jaxws.context;

import java.security.Principal;

//  TODO JAX-WS 2.1
//import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;


public class WebServiceContextImpl implements WebServiceContext {

    private static ThreadLocal<MessageContext> context = new ThreadLocal<MessageContext>();

    public WebServiceContextImpl() { 
    }

    public WebServiceContextImpl(MessageContext ctx) { 
        setMessageContext(ctx);
    } 

    // Implementation of javax.xml.ws.WebServiceContext

    public final MessageContext getMessageContext() {
        return context.get();
    }

    public final Principal getUserPrincipal() {
        return null;
    }

    public final boolean isUserInRole(final String string) {
        return false;
    }
    
    //  TODO JAX-WS 2.1
    /*
    public EndpointReference getEndpointReference(Element... referenceParameters) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
                                                                Element... referenceParameters) {
        // TODO
        throw new UnsupportedOperationException();
    }
    */
    
    

    public static void setMessageContext(MessageContext ctx) {
        //ContextPropertiesMapping.mapCxf2Jaxws(ctx);
        context.set(ctx);
    }

    public static void clear() {
        context.set(null);
    }

}
