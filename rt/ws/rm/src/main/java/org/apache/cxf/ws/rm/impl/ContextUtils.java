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

import java.util.UUID;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

/**
 * Holder for utility methods relating to contexts.
 */
public final class ContextUtils {

    /**
     * Used to fabricate a Uniform Resource Name from a UUID string
     */
    private static final String URN_UUID = "urn:uuid:";

    /**
     * Prevents instantiation.
     */
    private ContextUtils() {
    }

    /**
     * @return a generated UUID
     */
    static String generateUUID() {
        return URN_UUID + UUID.randomUUID();
    }

    /**
     * Determine if message is outbound.
     * 
     * @param message the current Message
     * @return true iff the message direction is outbound
     */
    static boolean isOutbound(Message message) {
        Exchange exchange = message.getExchange();
        return message != null && exchange != null && message == exchange.getOutMessage();
    }
}
