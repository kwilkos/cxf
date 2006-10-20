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

package org.apache.cxf.binding.soap;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.message.AbstractWrappedMessage;
import org.apache.cxf.message.Message;

public class SoapMessage extends AbstractWrappedMessage {
    private static final DocumentBuilder BUILDER = DOMUtils.createDocumentBuilder();

    
    private Map<Class<?>, Object> headers = new HashMap<Class<?>, Object>(); 
    
    private SoapVersion version = Soap11.getInstance();

    public SoapMessage(Message message) {
        super(message);
    }

    public SoapVersion getVersion() {
        return version;
    }

    public void setVersion(SoapVersion v) {
        this.version = v;
    }
    
    public <T> boolean hasHeaders(Class<T> format) {
        return headers.containsKey(format);
    }
    
    public <T> T getHeaders(Class<T> format) {
        T t = format.cast(headers.get(format));
        if (t == null && Element.class.equals(format)) {
            Document doc = BUILDER.newDocument();
            Element header = doc.createElementNS(version.getNamespace(),
                                                 version.getHeader().getLocalPart());
            header.setPrefix(version.getPrefix());
            setHeaders(Element.class, header);
            t = format.cast(header);
        }
        return t;
    }  

    public <T> void setHeaders(Class<T> format, T content) {
        headers.put(format, content);
    }
    
    public String getAttachmentMimeType() {
        return version.getSoapMimeType();
    }
    
}
