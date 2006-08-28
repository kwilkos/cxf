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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.jaxb.attachment.JAXBAttachmentUnmarshaller;
import org.apache.cxf.message.Message;

public class MessageDataReader implements DataReader<Message> {
    
    final JAXBDataReaderFactory factory;

    public MessageDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(Message input) {
        return read(null, input);
    }
    
    public Object read(QName name, Message input) {
        return read(name, input, null);
    }
    
    public Object read(QName name, Message input, Class cls) {
        JAXBAttachmentUnmarshaller au = null;        
        if (input.get(Message.ATTACHMENT_DESERIALIZER) != null) {
            au = new JAXBAttachmentUnmarshaller(input); 
        }
        Object source = null;
        XMLStreamReader xsr = (XMLStreamReader)input.getContent(XMLStreamReader.class);
        if (xsr != null) {
            source = xsr;
        } else {
            XMLEventReader xer = (XMLEventReader)input.getContent(XMLEventReader.class);
            if (xer != null) {
                source = xer;
            } else {
                Node node = (Node)input.getContent(Node.class);
                source = node;
            }
        }
        if (source == null) {
            return null;
        }
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(), source,
                                             name,
                                             cls, 
                                             au);
    }

}
