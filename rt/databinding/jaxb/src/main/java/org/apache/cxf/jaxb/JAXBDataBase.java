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

package org.apache.cxf.jaxb;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;

import org.apache.cxf.jaxb.attachment.JAXBAttachmentMarshaller;
import org.apache.cxf.jaxb.attachment.JAXBAttachmentUnmarshaller;
import org.apache.cxf.message.Attachment;

/**
 * 
 */
public abstract class JAXBDataBase {
    
    protected JAXBContext context; 
    protected Schema schema;
    protected Collection<Attachment> attachments;
    protected boolean attachmentProcessingEnabled;
    protected boolean unwrapJAXBElement = true;
    
    protected JAXBDataBase(JAXBContext ctx) {
        context = ctx;
    }
    
    public void setSchema(Schema s) {
        this.schema = s;
    }

    public void setJAXBContext(JAXBContext jc) {
        this.context = jc;
    }
    
    public Schema getSchema() {
        return schema;
    }
    public JAXBContext getJAXBContext() {
        return context;
    }

    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        this.attachments = attachments;
    }

    protected AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return new JAXBAttachmentUnmarshaller(attachments);
    }

    protected AttachmentMarshaller getAttachmentMarrshaller() {
        return new JAXBAttachmentMarshaller(attachments);
    }
    
    public void setProperty(String prop, Object value) {
        if (prop.equals(JAXBDataBinding.UNWRAP_JAXB_ELEMENT)) {
            unwrapJAXBElement = Boolean.TRUE.equals(value);
        }
    }
}
