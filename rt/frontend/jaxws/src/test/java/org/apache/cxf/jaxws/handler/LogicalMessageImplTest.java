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

import junit.framework.TestCase;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.handlers.types.AddNumbers;

public class LogicalMessageImplTest extends TestCase {
    AddNumbers req;
    List<Object> args;

    public void setUp() {
        req = new AddNumbers();        
        req.setArg0(10);
        req.setArg1(20);
        args = new ArrayList<Object>();
        args.add(req);
    }

    public void tearDown() {
    }

    public void xtestGetPayloadOfJAXB() throws Exception {
        //using Dispatch
        Message message = new MessageImpl();
        message.setContent(Object.class, req);
        LogicalMessageContextImpl lmci = new LogicalMessageContextImpl(message);
        
        LogicalMessageImpl lmi = new LogicalMessageImpl(lmci);
        JAXBContext ctx = JAXBContext.newInstance(AddNumbers.class);
        
        Object obj = lmi.getPayload(ctx);
        assertEquals(req, obj);        
    }
    
    public void testGetPayloadOfList() throws Exception {
        Message message = new MessageImpl();
        message.setContent(List.class, args);
        LogicalMessageContextImpl lmci = new LogicalMessageContextImpl(message);
        
        LogicalMessageImpl lmi = new LogicalMessageImpl(lmci);
        
        JAXBContext ctx = JAXBContext.newInstance(AddNumbers.class);        
        Object obj = lmi.getPayload(ctx);
        assertEquals(req, obj);        
    }    
}
