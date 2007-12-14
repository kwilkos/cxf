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

package org.apache.cxf.aegis.databinding;

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;

import org.w3c.dom.Element;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.W3CDOMStreamReader;

/**
 * 
 */
public class ElementDataReader implements DataReader<Element> {
    XMLStreamDataReader reader;
    
    ElementDataReader(AegisDatabinding binding) {
        reader = new XMLStreamDataReader(binding);
    }

    /** {@inheritDoc}*/
    public Object read(Element input) {
        try {
            W3CDOMStreamReader sreader = new W3CDOMStreamReader(input);
            sreader.nextTag(); //advance into the first tag
            return reader.read(sreader);
        } catch (XMLStreamException e) {
            throw new Fault(e);
        }
    }

    /** {@inheritDoc}*/
    public Object read(MessagePartInfo part, Element input) {
        try {
            W3CDOMStreamReader sreader = new W3CDOMStreamReader(input);
            sreader.nextTag(); //advance into the first tag
            return reader.read(part, sreader);
        } catch (XMLStreamException e) {
            throw new Fault(e);
        }
    }

    /** {@inheritDoc}*/
    public Object read(QName name, Element input, Class type) {
        try {
            W3CDOMStreamReader sreader = new W3CDOMStreamReader(input);
            sreader.nextTag(); //advance into the first tag
            return reader.read(name, sreader, type);
        } catch (XMLStreamException e) {
            throw new Fault(e);
        }
    }

    /** {@inheritDoc}*/
    public void setAttachments(Collection<Attachment> attachments) {
        reader.setAttachments(attachments);
    }

    /** {@inheritDoc}*/
    public void setProperty(String prop, Object value) {
        reader.setProperty(prop, value);
    }

    /** {@inheritDoc}*/
    public void setSchema(Schema s) {
        reader.setSchema(s);
    }

}
