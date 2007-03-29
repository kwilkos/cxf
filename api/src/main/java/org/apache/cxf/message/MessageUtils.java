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

package org.apache.cxf.message;

/**
 * Holder for utility methods relating to messages.
 */
public final class MessageUtils {

    /**
     * Prevents instantiation.
     */
    private MessageUtils() {
    }

    /**
     * Determine if message is outbound.
     * 
     * @param message the current Message
     * @return true iff the message direction is outbound
     */
    public static boolean isOutbound(Message message) {
        Exchange exchange = message.getExchange();
        return message != null && exchange != null
               && (message == exchange.getOutMessage() || message == exchange.getOutFaultMessage());
    }

    /**
     * Determine if message is fault.
     * 
     * @param message the current Message
     * @return true iff the message is a fault
     */
    public static boolean isFault(Message message) {
        return message != null
               && message.getExchange() != null
               && (message == message.getExchange().getInFaultMessage() || message == message.getExchange()
                   .getOutFaultMessage());
    }

    /**
     * Determine if current messaging role is that of requestor.
     * 
     * @param message the current Message
     * @return true iff the current messaging role is that of requestor
     */
    public static boolean isRequestor(Message message) {
        Boolean requestor = (Boolean)message.get(Message.REQUESTOR_ROLE);
        return requestor != null && requestor.booleanValue();
    }
    
    /**
     * Determine if the current message is a partial response.
     * 
     * @param message the current message
     * @return true iff the current messags is a partial response
     */
    public static boolean isPartialResponse(Message message) {
        return Boolean.TRUE.equals(message.get(Message.PARTIAL_RESPONSE_MESSAGE));
    }

}
