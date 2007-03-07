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
package org.apache.cxf.aegis.type.mtom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;
import org.apache.cxf.message.Attachment;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public abstract class AbstractXOPType extends Type {
    public final static String XOP_NS = "http://www.w3.org/2004/08/xop/include";
    public final static String XML_MIME_NS = "http://www.w3.org/2004/11/xmlmime";

    public final static QName XOP_INCLUDE = new QName(XOP_NS, "Include");
    public final static QName XOP_HREF = new QName("href");
    public final static QName XML_MIME_TYPE = new QName(XML_MIME_NS, "mimeType");

    public AbstractXOPType() {
    }

    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        Object o = null;
        while (reader.hasMoreElementReaders()) {
            MessageReader child = reader.getNextElementReader();
            if (child.getName().equals(XOP_INCLUDE)) {
                MessageReader mimeReader = child.getAttributeReader(XOP_HREF);
                String type = mimeReader.getValue();
                o = readInclude(type, child, context);
            }
            child.readToEnd();
        }

        return o;
    }

    public Object readInclude(String type, MessageReader reader, Context context) throws DatabindingException {
        String href = reader.getAttributeReader(XOP_HREF).getValue();

        Attachment att = AttachmentUtil.getAttachment(href, context.getAttachments());

        if (att == null) {
            throw new DatabindingException("Could not find the attachment " + href);
        }

        try {
            return readAttachment(att, context);
        } catch (IOException e) {
            throw new DatabindingException("Could not read attachment", e);
        }
    }

    protected abstract Object readAttachment(Attachment att, Context context) throws IOException;

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) throws DatabindingException {
        Collection<Attachment> attachments = context.getAttachments();
        if (attachments == null) {
            attachments = new ArrayList<Attachment>();
            context.setAttachments(attachments);
        }

        String id = AttachmentUtil.createContentID(getSchemaType().getNamespaceURI());

        Attachment att = createAttachment(object, id);

        attachments.add(att);

        String contentType = getContentType(object, context);
        if (contentType != null) {
            MessageWriter mt = writer.getAttributeWriter(XML_MIME_TYPE);
            mt.writeValue(contentType);
        }

        MessageWriter include = writer.getElementWriter(XOP_INCLUDE);
        MessageWriter href = include.getAttributeWriter(XOP_HREF);
        href.writeValue("cid:" + id);

        include.close();
    }

    protected abstract Attachment createAttachment(Object object, String id);

    protected abstract String getContentType(Object object, Context context);
}
