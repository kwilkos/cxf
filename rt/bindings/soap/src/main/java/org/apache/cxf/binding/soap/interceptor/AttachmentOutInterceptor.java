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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.ResourceBundle;

import org.apache.cxf.binding.attachment.CachedOutputStream;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.attachment.AttachmentSerializer;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.AbstractCachedOutputStream;

public class AttachmentOutInterceptor extends AbstractSoapInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public AttachmentOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }

    public void handleMessage(SoapMessage message) throws Fault {
        // Calling for soap out interceptor        
        message.getInterceptorChain().doIntercept(message);
        AbstractCachedOutputStream ops = (AbstractCachedOutputStream)message.getContent(OutputStream.class);
        try {
            Collection<Attachment> attachments = message.getAttachments();
            if (attachments.size() > 0) {
                CachedOutputStream cos = new CachedOutputStream();
                AttachmentSerializer as = new AttachmentSerializer(message, ops.getInputStream(), cos);
                as.serializeMultipartMessage();
                ops.resetOut(cos, false);
            }           
        } catch (IOException ioe) {
            throw new SoapFault(new Message("ATTACHMENT_IO", BUNDLE, ioe.toString()), 
                                SoapFault.ATTACHMENT_IO);
        }
    }


}
