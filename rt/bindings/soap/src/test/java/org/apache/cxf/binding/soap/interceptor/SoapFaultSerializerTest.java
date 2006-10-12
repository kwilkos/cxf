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

package org.apache.cxf.binding.soap.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.test.AbstractCXFTest;

public class SoapFaultSerializerTest extends AbstractCXFTest {
    public void testSoap11Out() throws Exception {
        String faultString = "Hadrian caused this Fault!";
        SoapFault fault = new SoapFault(faultString, Soap11.getInstance().getSender());

        SoapMessage m = new SoapMessage(new MessageImpl());
        m.setContent(Exception.class, fault);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(out);
        writer.writeStartDocument();
        writer.writeStartElement("Body");

        m.setContent(XMLStreamWriter.class, writer);

        Soap11FaultOutInterceptor interceptor = new Soap11FaultOutInterceptor();
        interceptor.handleMessage(m);

        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        Document faultDoc = DOMUtils.readXml(new ByteArrayInputStream(out.toByteArray()));
        assertValid("//s:Fault/faultcode[text()='ns1:Client']", faultDoc);
        assertValid("//s:Fault/faultstring[text()='" + faultString + "']", faultDoc);

        XMLStreamReader reader = StaxUtils.createXMLStreamReader(new ByteArrayInputStream(out.toByteArray()));
        m.setContent(XMLStreamReader.class, reader);

        reader.nextTag();

        Soap11FaultInInterceptor inInterceptor = new Soap11FaultInInterceptor();
        inInterceptor.handleMessage(m);

        SoapFault fault2 = (SoapFault)m.getContent(Exception.class);
        assertNotNull(fault2);
        assertEquals(fault.getMessage(), fault2.getMessage());
        assertEquals(Soap11.getInstance().getSender(), fault2.getFaultCode());
    }
    
    public void testSoap12Out() throws Exception {
        String faultString = "Hadrian caused this Fault!";
        SoapFault fault = new SoapFault(faultString, Soap12.getInstance().getSender());
        
        QName qname = new QName("http://cxf.apache.org/soap/fault", "invalidsoap", "cxffaultcode");
        fault.setSubCode(qname);

        SoapMessage m = new SoapMessage(new MessageImpl());
        m.setVersion(Soap12.getInstance());
        
        m.setContent(Exception.class, fault);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(out);
        writer.writeStartDocument();
        writer.writeStartElement("Body");

        m.setContent(XMLStreamWriter.class, writer);

        Soap12FaultOutInterceptor interceptor = new Soap12FaultOutInterceptor();
        interceptor.handleMessage(m);

        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        Document faultDoc = DOMUtils.readXml(new ByteArrayInputStream(out.toByteArray()));
        
        assertValid("//soap12env:Fault/Code/Value[text()='ns1:Sender']", faultDoc);
        assertValid("//soap12env:Fault/Code/Subcode[text()='ns2:invalidsoap']", faultDoc);
        assertValid("//soap12env:Fault/Reason/Text[@xml:lang='en']", faultDoc);
        assertValid("//soap12env:Fault/Reason/Text[text()='" + faultString + "']", faultDoc);

        XMLStreamReader reader = StaxUtils.createXMLStreamReader(new ByteArrayInputStream(out.toByteArray()));
        m.setContent(XMLStreamReader.class, reader);

        reader.nextTag();

        Soap12FaultInInterceptor inInterceptor = new Soap12FaultInInterceptor();
        inInterceptor.handleMessage(m);

        SoapFault fault2 = (SoapFault)m.getContent(Exception.class);
        assertNotNull(fault2);
        assertEquals(fault.getMessage(), fault2.getMessage());
        assertEquals(Soap12.getInstance().getSender(), fault2.getFaultCode());
    }
    
}
