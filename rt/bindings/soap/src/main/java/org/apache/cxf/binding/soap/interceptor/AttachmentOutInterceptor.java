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

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.jaxb.attachment.AttachmentSerializer;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.phase.Phase;

public class AttachmentOutInterceptor extends AbstractSoapInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public AttachmentOutInterceptor() {
        super();
        setPhase(Phase.PRE_STREAM);
    }

    public void handleMessage(SoapMessage message) throws Fault {
        // TODO: We shouldn't be running this interceptor if MTOM isn't enabled
        // as caching everything is going to slow us down
        
        OutputStream os = message.getContent(OutputStream.class);
        CachedStream cs = new CachedStream();
        message.setContent(OutputStream.class, cs);
        
        // Calling for soap out interceptor
        message.getInterceptorChain().doIntercept(message);
        // Set back the output stream
        message.setContent(OutputStream.class, os);        
        try {
            Collection<Attachment> attachments = message.getAttachments();
            cs.flush();
            if (attachments.size() > 0) {
                AttachmentSerializer as = new AttachmentSerializer(message, cs.getInputStream(), os);
                as.serializeMultipartMessage();
            } else {
                // get wire connection, and copy xml infoset directly into it
                os.flush();
                AbstractCachedOutputStream.copyStream(cs.getInputStream(), os, 64 * 1024);                
            }
        } catch (IOException ioe) {
            throw new SoapFault(new Message("ATTACHMENT_IO", BUNDLE, ioe.toString()), 
                            SoapFault.ATTACHMENT_IO);
        }
    }

    private class CachedStream extends AbstractCachedOutputStream {
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
        }

        protected void onWrite() throws IOException {
        }
    }
}
