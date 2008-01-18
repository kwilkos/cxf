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
import org.apache.cxf.aegis.type.basic.Base64Type;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;
import org.apache.cxf.message.Attachment;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Base class for MtoM types.
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public abstract class AbstractXOPType extends Type {
    public static final String XOP_NS = "http://www.w3.org/2004/08/xop/include";
    public static final String XML_MIME_NS = "http://www.w3.org/2005/05/xmlmime";
    public static final String XML_MIME_ATTR_LOCAL_NAME = "expectedContentTypes";

    public static final QName XOP_INCLUDE = new QName(XOP_NS, "Include");
    public static final QName XOP_HREF = new QName("href");
    
    private String expectedContentTypes;
    // the base64 type knows how to deal with just plain base64 here, which is essentially always 
    // what we get in the absence of the optimization. So we need something of a coroutine.
    private Base64Type fallbackDelegate;

    public AbstractXOPType(String expectedContentTypes) {
        this.expectedContentTypes = expectedContentTypes;
        fallbackDelegate = new Base64Type(this);
    }

    /**
     * This is called from base64Type when it recognizes an XOP attachment.
     * @param reader
     * @param context
     * @return
     * @throws DatabindingException
     */
    public Object readMtoM(MessageReader reader, Context context) throws DatabindingException {
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

    /**
     * This defers to the plain base64 type, which calls back into here above for XOP.
     * {@inheritDoc}
     */
    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        Object thingRead = fallbackDelegate.readObject(reader, context);
        // If there was actually an attachment, the delegate will have called back to us and gotten
        // the appropriate data type. If there wasn't an attachment, it just returned the bytes. 
        // Our subclass have to package them.
        if (thingRead.getClass() == (new byte[0]).getClass()) {
            String contentType = context.getContentType();
            return wrapBytes((byte[])thingRead, contentType);
        }

        return thingRead;
    }
    
    private Object readInclude(String type, MessageReader reader,
                              Context context) throws DatabindingException {
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
    public void writeObject(Object object, MessageWriter writer,
                            Context context) throws DatabindingException {
        if (!context.isMtomEnabled()) {
            fallbackDelegate.writeObject(getBytes(object), writer, context);
            return;
        }
        
        Collection<Attachment> attachments = context.getAttachments();
        if (attachments == null) {
            attachments = new ArrayList<Attachment>();
            context.setAttachments(attachments);
        }

        String id = AttachmentUtil.createContentID(getSchemaType().getNamespaceURI());

        Attachment att = createAttachment(object, id);

        attachments.add(att);

        MessageWriter include = writer.getElementWriter(XOP_INCLUDE);
        MessageWriter href = include.getAttributeWriter(XOP_HREF);
        href.writeValue("cid:" + id);

        include.close();
    }

    protected abstract Attachment createAttachment(Object object, String id);

    protected abstract String getContentType(Object object, Context context);
    
    /**
     * If one of these types arrives unoptimized, we need to convert it to the 
     * desired return type.
     * @param bareBytes the bytes pulled out of the base64.
     * @param contentType when we support xmime:contentType, this will be passed along.
     * @return
     */
    protected abstract Object wrapBytes(byte[] bareBytes, String contentType);

    /**
     * if MtoM is not enabled, we need bytes to turn into base64.
     * @return
     */
    protected abstract byte[] getBytes(Object object);

    @Override
    public void addToSchemaElement(Element schemaElement) {
        if (expectedContentTypes != null) {
            Namespace ns = Namespace.getNamespace("xmime", XML_MIME_NS);
            Attribute expectedContentTypeAttribute = new Attribute(XML_MIME_ATTR_LOCAL_NAME, 
                                                                   expectedContentTypes,
                                                                   ns);
            schemaElement.setAttribute(expectedContentTypeAttribute);
        }
    }
}
