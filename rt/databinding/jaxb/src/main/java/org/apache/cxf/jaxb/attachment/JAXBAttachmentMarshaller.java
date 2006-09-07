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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

import org.apache.cxf.binding.attachment.AttachmentImpl;
import org.apache.cxf.binding.attachment.AttachmentUtil;
import org.apache.cxf.binding.attachment.ByteDataSource;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class JAXBAttachmentMarshaller extends AttachmentMarshaller {

    private Message message;
    private Collection<Attachment> atts;
    private boolean isXop;

    public JAXBAttachmentMarshaller(Message messageParam) {
        super();
        this.message = messageParam;
        atts = message.getAttachments();
    }

    public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNS,
                                    String elementLocalName) {
        ByteDataSource source = new ByteDataSource(data, offset, length);
        source.setContentType(mimeType);
        DataHandler handler = new DataHandler(source);

        String id;
        try {
            id = AttachmentUtil.createContentID(elementNS);
        } catch (UnsupportedEncodingException e) {
            throw new Fault(e);
        }
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return "cid:" + id;
    }

    public String addMtomAttachment(DataHandler handler, String elementNS, String elementLocalName) {

        String id;
        try {
            id = AttachmentUtil.createContentID(elementNS);
        } catch (UnsupportedEncodingException e) {
            throw new Fault(e);
        }
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return "cid:" + id;
    }

    @Override
    public String addSwaRefAttachment(DataHandler handler) {
        String id = UUID.randomUUID() + "@" + handler.getName();
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return id;
    }

    public void setXOPPackage(boolean xop) {
        this.isXop = xop;
    }

    @Override
    public boolean isXOPPackage() {
        return isXop;
    }
}
