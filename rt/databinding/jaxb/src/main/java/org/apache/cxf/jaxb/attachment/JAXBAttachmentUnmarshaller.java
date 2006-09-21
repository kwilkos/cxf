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

package org.apache.cxf.jaxb.attachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class JAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBAttachmentUnmarshaller.class);

    private Message message;

    private AttachmentDeserializer ad;

    public JAXBAttachmentUnmarshaller(Message messageParam) {
        super();
        this.message = messageParam;
        ad = message.get(AttachmentDeserializer.class);
        if (ad == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_ATTACHMENT_DESERIALIZER", LOG));
        }
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String contentId) {
        return getAttachment(contentId).getDataHandler();
    }

    @Override
    public byte[] getAttachmentAsByteArray(String contentId) {
        // TODO Auto-generated method stub
        Attachment att = getAttachment(contentId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            copy(att.getDataHandler().getInputStream(), bos);
        } catch (IOException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("ATTACHMENT_READ_ERROR", LOG), e);
        }
        return bos.toByteArray();
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        try {
            final byte[] buffer = new byte[8096];
            for (int n = input.read(buffer); -1 != n; n = input.read(buffer)) {
                output.write(buffer, 0, n);
            }
        } finally {
            output.close();
            input.close();
        }
    }

    @Override
    public boolean isXOPPackage() {
        String contentTypeOfSoapBodyPart;        
        Attachment primaryMimePart = message.getContent(Attachment.class);
        if (primaryMimePart == null) {
            return false;
        } else {
            contentTypeOfSoapBodyPart = primaryMimePart.getHeader("Content-Type");
        }
        if (contentTypeOfSoapBodyPart != null
                        && contentTypeOfSoapBodyPart.indexOf("application/xop+xml") >= 0) {

            if (contentTypeOfSoapBodyPart.indexOf("application/soap+xml") >= 0) {
                return true;
            } else if (contentTypeOfSoapBodyPart.indexOf("text/xml") >= 0) {
                return true;
            }
        }
        return false;

    }

    private Attachment getAttachment(String contentId) {
        Attachment att = null;
        try {
            att = ad.getAttachment(contentId);
        } catch (MessagingException me) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("FAILED_GETTING_ATTACHMENT", LOG,
                            contentId), me);
        } catch (IOException ioe) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("FAILED_GETTING_ATTACHMENT", LOG,
                            contentId), ioe);
        }
        if (att == null) {
            throw new IllegalArgumentException("Attachment " + contentId + " was not found.");
        }
        return att;
    }

}
