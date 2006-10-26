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

package org.apache.cxf.jaxws.interceptors;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Service;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.wsdl.WSDLConstants;

public class DispatchOutInterceptor extends AbstractOutDatabindingInterceptor {

    public DispatchOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) throws Fault {
        Service.Mode m = message.getExchange().get(Service.Mode.class);

        try {
            OutputStream os = message.getContent(OutputStream.class);
            Object obj = message.getContent(Object.class);
            if (message instanceof SoapMessage) {
                if (m == Service.Mode.MESSAGE) {
                    if (obj instanceof SOAPMessage) {
                        ((SOAPMessage)obj).writeTo(os);
                    } else if (obj instanceof Source) {
                        doTransform(obj, os);
                    } else if (obj instanceof DataSource) {
                        throw new RuntimeException(obj.getClass()
                                                   + " is not valid in Message mode for SOAP/HTTP");
                    }
                } else if (m == Service.Mode.PAYLOAD) {
                    SOAPMessage msg = initSOAPMessage();
                    DataWriter<SOAPBody> dataWriter = getDataWriter(message, SOAPBody.class);
                    if (obj instanceof Source || obj instanceof Object) {
                        dataWriter.write(obj, msg.getSOAPBody());
                    } else if (obj instanceof SOAPMessage || obj instanceof DataSource) {
                        throw new RuntimeException(obj.getClass()
                                                   + " is not valid in PAYLOAD mode with SOAP/HTTP");
                    }
                    msg.writeTo(os);
                }
            } else if (message instanceof XMLMessage) {
                if (m == Service.Mode.MESSAGE
                    && obj instanceof SOAPMessage) {
                    throw new RuntimeException("SOAPMessage is not valid in MESSAGE mode with XML/HTTP");
                } else if (m == Service.Mode.PAYLOAD
                           && (obj instanceof SOAPMessage
                               || obj instanceof DataSource)) {
                    throw new RuntimeException(obj.getClass()
                                               + " is not valid in PAYLOAD mode with XML/HTTP");
                }
                doTransform(obj, os);
            }
            // Finish the message processing, do flush
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SOAPMessage initSOAPMessage() throws SOAPException {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSD,
                                                                WSDLConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSI,
                                                                WSDLConstants.NU_SCHEMA_XSI);

        return msg;
    }

    private void doTransform(Object obj, OutputStream os) throws TransformerException, IOException {
        if (obj instanceof Source) {
            Transformer transformer = XMLUtils.newTransformer();
            transformer.transform((Source)obj, new StreamResult(os));
        }
        if (obj instanceof DataSource) {
            IOUtils.copy(((DataSource)obj).getInputStream(), os);
        }
    }

}
