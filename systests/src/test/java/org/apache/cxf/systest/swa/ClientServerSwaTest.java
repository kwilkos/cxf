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
package org.apache.cxf.systest.swa;

import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;

import org.apache.cxf.swa.SwAService;
import org.apache.cxf.swa.SwAServiceInterface;
import org.apache.cxf.swa.types.DataStruct;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientServerSwaTest extends AbstractBusClientServerTestBase {

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }
    
    @Test
    public void testSwa() throws Exception {
        SwAService service = new SwAService();
        
        SwAServiceInterface port = service.getSwAServiceHttpPort();
//        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
//                                                        "http://localhost:9037/swa");
        
        Holder<String> textHolder = new Holder<String>();
        Holder<DataHandler> data = new Holder<DataHandler>();
        
        ByteArrayDataSource source = new ByteArrayDataSource("foobar".getBytes(), "application/octet-stream");
        DataHandler handler = new DataHandler(source);
        
        data.value = handler;
        
        textHolder.value = "Hi";

        port.echoData(textHolder, data);
        InputStream bis = null;
        bis = data.value.getDataSource().getInputStream();
        byte b[] = new byte[10];
        bis.read(b, 0, 10);
        String string = new String(b);
        assertEquals("testfoobar", string);
        assertEquals("Hi", textHolder.value);
    }
    
    @Test
    public void testSwaDataStruct() throws Exception {
        SwAService service = new SwAService();
        
        SwAServiceInterface port = service.getSwAServiceHttpPort();
//        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
//                                                        "http://localhost:9037/swa");
        
        Holder<DataStruct> structHolder = new Holder<DataStruct>();
        
        ByteArrayDataSource source = new ByteArrayDataSource("foobar".getBytes(), "application/octet-stream");
        DataHandler handler = new DataHandler(source);
        
        DataStruct struct = new DataStruct();
        struct.setDataRef(handler);
        structHolder.value = struct;

        port.echoDataRef(structHolder);

        handler = structHolder.value.getDataRef();
        InputStream bis = null;
        bis = handler.getDataSource().getInputStream();
        byte b[] = new byte[10];
        bis.read(b, 0, 10);
        String string = new String(b);
        assertEquals("testfoobar", string);
    }
}
