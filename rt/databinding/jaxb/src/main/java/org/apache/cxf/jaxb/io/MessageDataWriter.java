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

package org.apache.cxf.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.jaxb.attachment.JAXBAttachmentMarshaller;
import org.apache.cxf.message.Message;


public class MessageDataWriter implements DataWriter<Message> {

    final JAXBDataWriterFactory factory;

    public MessageDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }

    public void write(Object obj, Message output) {
        write(obj, null, output);
    }

    public void write(Object obj, QName elName, Message output) {
        // if the mtom is enabled, we need to create the attachment mashaller
        JAXBAttachmentMarshaller am = null;
        // if (output.containsKey(Message.MTOM_ENABLED)) {
        am = new JAXBAttachmentMarshaller(output);
        // }
        Object source = null;
        XMLStreamWriter xsw = (XMLStreamWriter) output.getContent(XMLStreamWriter.class);
        if (xsw != null) {
            source = xsw;
        } else {
            XMLEventWriter xew = (XMLEventWriter) output.getContent(XMLEventWriter.class);
            if (xew != null) {
                source = xew;
            } else {
                Node node = (Node) output.getContent(Node.class);
                source = node;
            }
        }
        if (source == null) {
            return;
        }

        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(), factory.getSchema(), obj, elName, source,
                            am);
        }
    }

}
