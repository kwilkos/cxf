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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.cxf.binding.attachment.AttachmentDataSource;
import org.apache.cxf.binding.attachment.AttachmentUtil;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class AttachmentSerializer {

    private static final String[] FILTER = new String[] {"Message-ID", "Mime-Version", "Content-Type"};

    private Message message;
    private InputStream in;
    private OutputStream out;

    public AttachmentSerializer(Message messageParam, InputStream inParam, OutputStream outParam) {
        message = messageParam;
        in = inParam;
        out = outParam;
    }

    /**
     * Using result in soapMessage & attachment to write to output stream
     * 
     * @param soapMessage
     * @param in input stream contain the attachment
     * @param out
     * @throws CxfRioException
     */

    public String serializeMultipartMessage() {

        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);
        String soapPartId;
        try {
            soapPartId = AttachmentUtil.createContentID(null);
        } catch (UnsupportedEncodingException e) {
            throw new Fault(e);
        }
        String subType = AttachmentUtil.getMimeSubType(message, soapPartId);
        MimeMultipart mimeMP = new MimeMultipart(subType);

        // InputStream in = soapMessage.getContent(InputStream.class);
        AttachmentDataSource ads = new AttachmentDataSource("application/xop+xml", in);
        MimeBodyPart soapPart = new MimeBodyPart();
        try {
            soapPart.setDataHandler(new DataHandler(ads));
            soapPart.setContentID("<" + soapPartId + ">");
            soapPart.addHeader("Content-Type", "application/xop+xml");
            soapPart.addHeader("type", message.getAttachmentMimeType());
            soapPart.addHeader("charset", "utf-8");
            soapPart.addHeader("Content-Transfer-Encoding", "binary");
            mimeMP.addBodyPart(soapPart);

            for (Attachment att : message.getAttachments()) {
                MimeBodyPart part = new MimeBodyPart();
                part.setDataHandler(att.getDataHandler());
                part.setContentID("<" + att.getId() + ">");
                if (att.isXOP()) {
                    part.addHeader("Content-Transfer-Encoding", "binary");
                }
                mimeMP.addBodyPart(part);
            }
            mimeMessage.setContent(mimeMP);
            mimeMessage.writeTo(out, FILTER);
        } catch (MessagingException me) {
            throw new Fault(me);
        } catch (IOException ioe) {
            throw new Fault(ioe);
        }
        return mimeMP.getContentType();
    }



}
