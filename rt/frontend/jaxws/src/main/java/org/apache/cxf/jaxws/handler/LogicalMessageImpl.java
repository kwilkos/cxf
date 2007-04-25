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

package org.apache.cxf.jaxws.handler;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;


import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.BareOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;


public class LogicalMessageImpl implements LogicalMessage {

    private final LogicalMessageContextImpl msgContext;
    
    public LogicalMessageImpl(LogicalMessageContextImpl lmctx) {
        msgContext = lmctx;
    }

    public Source getPayload() {
        Source source = msgContext.getWrappedMessage().getContent(Source.class);
        if (source == null) {
            //need to convert
            Exception ex = msgContext.getWrappedMessage().getContent(Exception.class);
            if (ex instanceof SOAPFaultException) {
                List<Object> list = new ArrayList<Object>();
                list.add(((SOAPFaultException)ex).getFault());
                msgContext.getWrappedMessage().setContent(List.class, list);
            }
            
            W3CDOMStreamWriter writer;
            try {
                writer = new W3CDOMStreamWriter();
            } catch (ParserConfigurationException e) {
                throw new WebServiceException(e);
            }
            XMLStreamWriter orig = msgContext.getWrappedMessage().getContent(XMLStreamWriter.class);
            try {
                msgContext.getWrappedMessage().setContent(XMLStreamWriter.class, writer);
                BareOutInterceptor bi = new BareOutInterceptor();
                bi.handleMessage(msgContext.getWrappedMessage());
            } finally {
                msgContext.getWrappedMessage().setContent(XMLStreamWriter.class, orig); 
            }
            
            source = new DOMSource(writer.getDocument().getDocumentElement());
            msgContext.getWrappedMessage().setContent(Source.class, source);
        }
        return source;
    }

    public void setPayload(Source s) {
        msgContext.getWrappedMessage().setContent(Source.class, null);
        XMLStreamReader orig = msgContext.getWrappedMessage().getContent(XMLStreamReader.class);
        try {
            XMLStreamReader reader = StaxUtils.createXMLStreamReader(s);
            msgContext.getWrappedMessage().setContent(XMLStreamReader.class, reader);
            BareInInterceptor bin = new BareInInterceptor();
            bin.handleMessage(msgContext.getWrappedMessage());
        } finally {
            msgContext.getWrappedMessage().setContent(XMLStreamReader.class, orig);
        }
    }

    public Object getPayload(JAXBContext arg0) {
        Message msg = msgContext.getWrappedMessage();
        if (msg.getContent(List.class) != null) {
            return msg.getContent(List.class).get(0);
        } else if (msg.getContent(Object.class) != null) {
            return msg.getContent(Object.class);
        }
        // TODO - what to do with JAXB context?
        return null;
    }

    public void setPayload(Object arg0, JAXBContext arg1) {
        // TODO - what to do with JAXB context?
        List<Object> l = new ArrayList<Object>();
        l.add(arg0);        
        msgContext.getWrappedMessage().setContent(List.class, l);
    }

   
}
