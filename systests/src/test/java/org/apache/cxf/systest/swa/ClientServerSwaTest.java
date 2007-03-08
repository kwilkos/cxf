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

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;


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
        ((SOAPBinding) ((BindingProvider)port).getBinding()).setMTOMEnabled(true);
        
        Holder<DataStruct> structHolder = new Holder<DataStruct>();
        Holder<DataHandler> data = new Holder<DataHandler>();
        
        ByteArrayDataSource source = new ByteArrayDataSource("foobar".getBytes(), "application/octet-stream");
        DataHandler handler = new DataHandler(source);
        
        DataStruct struct = new DataStruct();
        struct.setDataRef(handler);
        data.value = handler;
        
        structHolder.value = struct;
        // TODO: Fails currently...
//         port.echoData(structHlder, data);
        
    }

}
