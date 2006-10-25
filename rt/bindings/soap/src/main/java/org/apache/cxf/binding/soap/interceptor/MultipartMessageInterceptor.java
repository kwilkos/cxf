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

package org.apache.cxf.binding.soap.interceptor;

import org.apache.cxf.binding.attachment.AttachmentDeserializer;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MultipartMessageInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String ATTACHMENT_DIRECTORY = "attachment-directory";
    public static final String ATTACHMENT_MEMORY_THRESHOLD = "attachment-memory-threshold";
    public static final int THRESHHOLD = 1024 * 100;

    /**
     * contruct the soap message with attachments from mime input stream
     * 
     * @param messageParam
     */
    
    public MultipartMessageInterceptor() {
        super();
        setPhase(Phase.RECEIVE);
    }
    
    public void handleMessage(Message message) {
        
        AttachmentDeserializer ad = new AttachmentDeserializer(message);
        if (ad.preprocessMessage()) {
            message.put(AttachmentDeserializer.class, ad);
        }
    }

    public void handleFault(Message messageParam) {
    }

}
