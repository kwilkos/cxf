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

package org.apache.cxf.jaxws.handler.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.jaxws.handler.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;

public class SOAPHandlerInterceptor extends
        AbstractProtocolHandlerInterceptor<SoapMessage> implements
        SoapInterceptor {
    public static final String SAAJ_ENABLED = "saaj.enabled";
    private static final ResourceBundle BUNDLE = BundleUtils
            .getBundle(SOAPHandlerInterceptor.class);

    public SOAPHandlerInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.PRE_PROTOCOL);
        addAfter((new StaxOutInterceptor()).getId());
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }

    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();
        for (Handler h : getBinding().getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                Set<QName> headers = CastUtils.cast(((SOAPHandler) h).getHeaders());
                if (headers != null) {
                    understood.addAll(headers);
                }
            }
        }
        return understood;
    }

    public void handleMessage(SoapMessage message) {

        boolean saajEnabled = Boolean.TRUE.equals(message.getContextualProperty(SAAJ_ENABLED));
        if (getInvoker(message).getProtocolHandlers().isEmpty() && !saajEnabled) {
            return;
        }
        
        if (getInvoker(message).isOutbound()) {
            XMLStreamWriter origWriter = message.getContent(XMLStreamWriter.class);
            
            try {
                // Replace stax writer with DomStreamWriter
                W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
                message.setContent(XMLStreamWriter.class, writer);
            } catch (ParserConfigurationException e) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                    "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), e,
                    message.getVersion().getSender());
            }
            
            message.getInterceptorChain().doInterceptInSubChain(message);

            super.handleMessage(message);

            try {
                // Stream SOAPMessage back to output stream if necessary
                SOAPMessage soapMessage = message.getContent(SOAPMessage.class);

                if (soapMessage != null) {
                    OutputStream os = message.getContent(OutputStream.class);
                    soapMessage.writeTo(os);
                    os.flush();
                } else {
                    XMLStreamWriter xtw = message.getContent(XMLStreamWriter.class);
                    Document doc = ((W3CDOMStreamWriter)xtw).getDocument();
                    StaxUtils.writeDocument(doc, origWriter, false);
                }
                
            } catch (IOException ioe) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), ioe,
                        message.getVersion().getSender());
            } catch (SOAPException soape) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), soape,
                        message.getVersion().getSender());
            } catch (XMLStreamException e) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), e,
                        message.getVersion().getSender());
            }
        } else {
            super.handleMessage(message);

            //SOAPMessageContextImpl ctx = (SOAPMessageContextImpl) createProtocolMessageContext(message);
            //ctx.getMessage();

            try {
                SOAPMessage soapMessage = message.getContent(SOAPMessage.class);

                if (soapMessage == null) {
                    if (saajEnabled) {
                        soapMessage = new SOAPMessageContextImpl(message).getMessage();
                        message.setContent(SOAPMessage.class, soapMessage);
                    } else {
                        return;
                    }
                }

                // soapMessage is not null means stax reader has been consumed
                // by SAAJ, we need to replace stax reader with a new stax reader
                // built from the content streamed from SAAJ SOAPBody.
                SOAPBody body = soapMessage.getSOAPBody();

                CachedStream outStream = new CachedStream();
                XMLUtils.writeTo(body, outStream);

                XMLStreamReader reader = null;
                reader = XMLInputFactory.newInstance().createXMLStreamReader(
                        outStream.getInputStream());
                // skip the start element of soap body.
                if (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                    reader.getName();
                }
                reader.next();
                message.setContent(XMLStreamReader.class, reader);
                
                //replace header element if necessary
                if (message.hasHeaders(Element.class)) {
                    Element headerElements = message.getHeaders(Element.class);
                    headerElements = soapMessage.getSOAPHeader();    
                    message.setHeaders(Element.class, headerElements);
                }
            } catch (IOException ioe) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), ioe,
                        message.getVersion().getSender());
            } catch (SOAPException soape) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), soape,
                        message.getVersion().getSender());
            } catch (XMLStreamException e) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), e, message
                        .getVersion().getSender());
            }

        }
    }

    @Override
    protected MessageContext createProtocolMessageContext(Message message) {
        return new SOAPMessageContextImpl(message);
    }

    public void handleFault(SoapMessage message) {
    }

    private class CachedStream extends AbstractCachedOutputStream {
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
        }

        protected void onWrite() throws IOException {
        }
    }

}
