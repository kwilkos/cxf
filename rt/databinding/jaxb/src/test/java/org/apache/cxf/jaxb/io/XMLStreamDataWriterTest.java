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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.apache.hello_world_doc_lit_bare.types.TradePriceData;
import org.apache.hello_world_rpclit.GreeterRPCLit;
import org.apache.hello_world_rpclit.types.MyComplexStruct;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.types.GreetMe;
import org.apache.hello_world_soap_http.types.GreetMeResponse;

public class XMLStreamDataWriterTest extends TestCase {

    private ByteArrayOutputStream baos;
    private XMLStreamWriter streamWriter;
    private XMLInputFactory inFactory;

    public void setUp() throws Exception {
        baos =  new ByteArrayOutputStream();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        streamWriter = factory.createXMLStreamWriter(baos);
        assertNotNull(streamWriter);
        inFactory = XMLInputFactory.newInstance();
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteRPCLit1() throws Exception {
        JAXBDataWriterFactory wf = getTestWriterFactory(GreeterRPCLit.class);
        
        DataWriter<XMLStreamWriter> dw = wf.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);
        
        String val = new String("TESTOUTPUTMESSAGE");
        QName elName = new QName("http://apache.org/hello_world_rpclit/types", 
                                 "in");
        
        dw.write(val, elName, streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "in"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }

    public void testWriteRPCLit2() throws Exception {
        JAXBDataWriterFactory wf = getTestWriterFactory(GreeterRPCLit.class);
        
        DataWriter<XMLStreamWriter> dw = wf.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);
        
        MyComplexStruct val = new MyComplexStruct();
        val.setElem1("This is element 1");
        val.setElem2("This is element 2");
        val.setElem3(1);
        
        QName elName = new QName("http://apache.org/hello_world_rpclit/types", 
                                 "in");
        
        dw.write(val, elName, streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "in"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "elem1"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("This is element 1", reader.getText());
    }

    public void testWriteBare() throws Exception {
        JAXBDataWriterFactory wf = getTestWriterFactory(PutLastTradedPricePortType.class);
        
        DataWriter<XMLStreamWriter> dw = wf.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);
        
        TradePriceData val = new TradePriceData();
        val.setTickerSymbol("This is a symbol");
        val.setTickerPrice(1.0f);
        
        dw.write(val,
                 new QName("http://apache.org/hello_world_doc_lit_bare/types", "inout"),
                 streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_doc_lit_bare/types", "inout"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_doc_lit_bare/types", "tickerSymbol"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("This is a symbol", reader.getText());
    }
    
    public void testWriteWrapper() throws Exception {
        JAXBDataWriterFactory wf = getTestWriterFactory(Greeter.class);
        
        DataWriter<XMLStreamWriter> dw = wf.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);

        GreetMe val = new GreetMe();
        val.setRequestType("Hello");
        
        dw.write(val, streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_soap_http/types", "greetMe"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_soap_http/types", "requestType"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("Hello", reader.getText());
    }

    public void testWriteWrapperReturn() throws Exception {
        JAXBDataWriterFactory wf = getTestWriterFactory(Greeter.class);
        
        DataWriter<XMLStreamWriter> dw = wf.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);

        GreetMeResponse retVal = new GreetMeResponse();
        retVal.setResponseType("TESTOUTPUTMESSAGE");
        
        dw.write(retVal, streamWriter);
        streamWriter.flush();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_soap_http/types", "greetMeResponse"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://apache.org/hello_world_soap_http/types", "responseType"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }

    private JAXBDataWriterFactory getTestWriterFactory(Class clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataWriterFactory writerFactory = new JAXBDataWriterFactory();
        writerFactory.setJAXBContext(ctx);
        return writerFactory;
    }
}
