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
import java.io.InputStream;
import java.io.OutputStream;
//import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.staxutils.StaxUtils;

public class SOAPMessageContextImpl extends WrappedMessageContext implements SOAPMessageContext {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SOAPMessageContextImpl.class);

    SOAPMessageContextImpl(Message m) {
        super(m);
    }

    public void setMessage(SOAPMessage message) {
        getWrappedMessage().setContent(SOAPMessage.class, message);
    }

    public SOAPMessage getMessage() {
        SOAPMessage message = getWrappedMessage().getContent(SOAPMessage.class);
        if (null == message) {

            try {
                Boolean outboundProperty = (Boolean)get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

                if (outboundProperty) {
                    MessageFactory factory = MessageFactory.newInstance();
                    MimeHeaders mhs = null;
                    //Safe to do this cast as SOAPHandlerInterceptor has explictly 
                    //replaced OutputStream with AbstractCachedOutputStream.
                    AbstractCachedOutputStream out = (AbstractCachedOutputStream)getWrappedMessage()
                        .getContent(OutputStream.class);
                    InputStream is = out.getInputStream();
                    message = factory.createMessage(mhs, is);
                } else {
                    CachedStream cs = new CachedStream();
                    XMLStreamWriter writer = StaxUtils.getXMLOutputFactory().createXMLStreamWriter(cs);
                    XMLStreamReader xmlReader = getWrappedMessage().getContent(XMLStreamReader.class);

                    //Create a mocked inputStream to feed SAAJ, 
                    //only SOAPBody is from real data                    
                    //REVISIT: soap version here is not important, we just use soap11.
                    SoapVersion soapVersion = Soap11.getInstance();
                    writer.setPrefix(soapVersion.getPrefix(), soapVersion.getNamespace());
                    writer.writeStartElement(soapVersion.getPrefix(), soapVersion.getEnvelope()
                        .getLocalPart(), soapVersion.getNamespace());
                    writer.writeNamespace(soapVersion.getPrefix(), soapVersion.getNamespace());
                    
                    //Write headers                    
                    if (getWrappedSoapMessage().hasHeaders(Element.class)) {
                        Element headerElements = getWrappedSoapMessage().getHeaders(Element.class);
                        StaxUtils.writeElement(headerElements, writer, true);
                    }
                    
                    writer.writeStartElement(soapVersion.getPrefix(), soapVersion.getBody().getLocalPart(),
                                             soapVersion.getNamespace());
                    
                    //Write soap body
                    StaxUtils.copy(xmlReader, writer);

                    xmlReader.close();
                    writer.close();
                    cs.doFlush();

                    InputStream newIs = cs.getInputStream();
                    MessageFactory factory = MessageFactory.newInstance();
                    MimeHeaders mhs = null;
                    message = factory.createMessage(mhs, newIs);
                }

                
/*                System.out.println("11111------------------");
                PrintStream out = System.out;
                message.writeTo(out);
                out.println();
                System.out.println("11111------------------");
*/
                getWrappedMessage().setContent(SOAPMessage.class, message);

            } catch (IOException ioe) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("SOAPHANDLERINTERCEPTOR_EXCEPTION",
                                                                       BUNDLE), ioe);
            } catch (SOAPException soape) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("SOAPHANDLERINTERCEPTOR_EXCEPTION",
                                                                       BUNDLE), soape);
            } catch (XMLStreamException e) {
                e.printStackTrace();
                throw new Fault(new org.apache.cxf.common.i18n.Message("SOAPHANDLERINTERCEPTOR_EXCEPTION",
                                                                       BUNDLE), e);
            }
        }

        return message;
    }

    // TODO: handle the boolean parameter
    public Object[] getHeaders(QName name, JAXBContext context, boolean allRoles) {
        Element headerElements = getWrappedSoapMessage().getHeaders(Element.class);
        if (headerElements == null) {
            return null;
        }
        Collection<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < headerElements.getChildNodes().getLength(); i++) {
            if (headerElements.getChildNodes().item(i) instanceof Element) {
                Element e = (Element)headerElements.getChildNodes().item(i);
                if (name.equals(e.getNamespaceURI())) {
                    try {
                        objects.add(context.createUnmarshaller().unmarshal(e));
                    } catch (JAXBException ex) {
                        // do something
                    }
                }
            }
        }
        Object[] headerObjects = new Object[objects.size()];
        return objects.toArray(headerObjects);
    }

    public Set<String> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    private SoapMessage getWrappedSoapMessage() {
        return (SoapMessage)getWrappedMessage();
    }

    private class CachedStream extends AbstractCachedOutputStream {
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
            currentStream.close();
        }

        protected void onWrite() throws IOException {
        }
    }
}
