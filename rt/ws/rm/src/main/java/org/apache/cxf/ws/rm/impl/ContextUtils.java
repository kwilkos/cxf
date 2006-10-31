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

package org.apache.cxf.ws.rm.impl;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.VersionTransformer;

/**
 * Holder for utility methods relating to contexts.
 */
public final class ContextUtils {

    /**
     * Prevents instantiation.
     */
    private ContextUtils() {
    }

    /**
     * @return a generated UUID
     */
    static String generateUUID() {
        return org.apache.cxf.ws.addressing.ContextUtils.generateUUID();
    }

    /**
     * Determine if current messaging role is that of requestor.
     * 
     * @param message the current Message
     * @return true iff the current messaging role is that of requestor
     */
    public static boolean isRequestor(Message message) {
        return org.apache.cxf.ws.addressing.ContextUtils.isRequestor(message);
    }

    /**
     * Determine if message is currently being processed on server side.
     * 
     * @param message the current Message
     * @return true iff message is currently being processed on server side
     */
    public static boolean isServerSide(Message message) {
        // TODO
        return false;
    }

    /**
     * Retrieves the addressing properties from the current message.
     * 
     * @param message the current message
     * @param isProviderContext true if the binding provider request context
     *            available to the client application as opposed to the message
     *            context visible to handlers
     * @param isOutbound true iff the message is outbound
     * @return the current addressing properties
     */
    public static AddressingPropertiesImpl retrieveMAPs(Message message, boolean isProviderContext,
                                                    boolean isOutbound) {
        return org.apache.cxf.ws.addressing.ContextUtils.retrieveMAPs(message, isProviderContext, isOutbound);
    }

    /**
     * Ensures the appropriate version of WS-Addressing is used.
     * 
     * @param maps the addressing properties
     */
    public static void ensureExposedVersion(AddressingProperties maps) {
        ((AddressingPropertiesImpl)maps).exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
    }

    /**
     * Returns the endpoint of this message, i.e. the client endpoint if the
     * current messaging role is that of requestor, or the server endpoint
     * otherwise.
     * 
     * @param message the current Message
     * @return the endpoint
     */
    public static Endpoint getEndpoint(Message message) {
        if (isRequestor(message)) {
            return message.getExchange().get(Endpoint.class);
        } else {
            return message.getExchange().get(Endpoint.class);
        }
    }

}
