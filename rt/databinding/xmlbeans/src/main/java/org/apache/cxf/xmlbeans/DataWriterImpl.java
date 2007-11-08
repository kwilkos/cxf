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

package org.apache.cxf.xmlbeans;


import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlTokenSource;

public class DataWriterImpl implements DataWriter<XMLStreamWriter> {
    private static final Logger LOG = LogUtils.getLogger(XmlBeansDataBinding.class);
    
    public DataWriterImpl() {
    }
    
    public void write(Object obj, XMLStreamWriter output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, MessagePartInfo part, XMLStreamWriter output) {
        try {
            if (obj != null
                || !(part.getXmlSchema() instanceof XmlSchemaElement)) {
                XmlTokenSource source = (XmlTokenSource)obj;
                XmlOptions options = new XmlOptions();
                XMLStreamReader reader = source.newCursor().newXMLStreamReader(options);
                output.writeStartElement(part.getConcreteName().getNamespaceURI(),
                                         part.getConcreteName().getLocalPart());
                StaxUtils.copy(reader, output, true);
                output.writeEndElement();
            } else if (obj == null && needToRender(obj, part)) {
                output.writeStartElement(part.getConcreteName().getNamespaceURI(),
                                         part.getConcreteName().getLocalPart());
                output.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new Fault(new Message("MARSHAL_ERROR", LOG, obj), e);
        }
    }

    private boolean needToRender(Object obj, MessagePartInfo part) {
        if (part != null && part.getXmlSchema() instanceof XmlSchemaElement) {
            XmlSchemaElement element = (XmlSchemaElement)part.getXmlSchema();
            return element.isNillable() && element.getMinOccurs() > 0;
        }
        return false;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        // TODO Auto-generated method stub
        
    }

    public void setProperty(String key, Object value) {
        // TODO Auto-generated method stub
        
    }

    public void setSchema(Schema s) {
        // TODO Auto-generated method stub
        
    }
}
