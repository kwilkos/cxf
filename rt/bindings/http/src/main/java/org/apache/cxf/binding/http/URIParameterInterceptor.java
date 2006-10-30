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
package org.apache.cxf.binding.http;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import org.apache.cxf.binding.http.IriDecoderHelper.Param;
import org.apache.cxf.binding.xml.interceptor.XMLMessageInInterceptor;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class URIParameterInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger LOG = Logger.getLogger(URIParameterInterceptor.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(URIParameterInterceptor.class);

    public URIParameterInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
        addBefore(XMLMessageInInterceptor.class.getName());
    }

    public void handleMessage(Message message) {
        String path = (String)message.get(Message.PATH_INFO);
        String method = (String)message.get(Message.HTTP_REQUEST_METHOD);
        String contentType = (String)message.get(Message.CONTENT_TYPE);

        LOG.info("URIParameterInterceptor handle message on path [" + path 
                 + "] with Content-Type ["  + contentType + "]");
        
        BindingOperationInfo op = message.getExchange().get(BindingOperationInfo.class);

        URIMapper mapper = (URIMapper)message.getExchange().get(Service.class).get(URIMapper.class.getName());
        String location = mapper.getLocation(op);

        List<MessagePartInfo> parts = op.getOperationInfo().getInput().getMessageParts();

        if (parts.size() == 0) {
            message.setContent(Object.class, Collections.EMPTY_LIST);
            return;
        }

        if (parts.size() > 1) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("SINGLE_PART_REQUIRED", BUNDLE));
        }

        message.getInterceptorChain().add(new XMLMessageInInterceptor());

        MessagePartInfo part = parts.get(0);

        List<Param> params = null;
        if ("application/x-www-form-urlencoded.".equals(contentType)) {
            params = IriDecoderHelper.decode(path, location, message.getContent(InputStream.class));
        } else if ("application/xml".equals(contentType)) {
            params = IriDecoderHelper.decodeIri(path, location);
        } else if ("text/xml".equals(contentType)) {
            params = IriDecoderHelper.decodeIri(path, location);
        } else if ("multipart/form-data".equals(contentType)) {
            // TODO
        } else {
            params = IriDecoderHelper.decodeIri(path, location);
        }

        mergeParams(message, path, method, part, params);
    }

    private void mergeParams(Message message, String path, String method, MessagePartInfo part,
                             List<Param> params) {
        // TODO: If its a POST/PUT operation we probably need to merge the
        // incoming doc
        Document doc;
        if ("POST".equals(method) || "PUT".equals(method)) {
            XMLStreamReader reader = StaxUtils.createXMLStreamReader(message.getContent(InputStream.class));
            try {
                doc = StaxUtils.read(reader);
            } catch (XMLStreamException e) {
                throw new Fault(e);
            }
            doc = IriDecoderHelper.interopolateParams(doc, (XmlSchemaElement)part.getXmlSchema(), params);
        } else {
            doc = IriDecoderHelper.buildDocument((XmlSchemaElement)part.getXmlSchema(), params);
        }

        // try {
        // DOMUtils.writeXml(doc, System.out);
        // } catch (TransformerException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }

        XMLStreamReader reader = StaxUtils.createXMLStreamReader(new DOMSource(doc));
        try {
            reader.next();
        } catch (XMLStreamException e) {
            throw new Fault(e);
        }
        message.setContent(XMLStreamReader.class, reader);
    }
}
