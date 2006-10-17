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

package org.apache.cxf.ws.rm.impl;


import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.cxf.CXFBusFactory;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class ProxyTest extends TestCase {

    public void testConstruction() {
        Bus bus = new CXFBusFactory().createBus();
        IMocksControl control = EasyMock.createNiceControl();
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        Proxy proxy = new Proxy(bus, rme);
        Service service = proxy.getService();
        ServiceInfo si = service.getServiceInfo();
        assertNotNull("service info is null", si);

        InterfaceInfo intf = si.getInterface();
        
        assertEquals(3, intf.getOperations().size());
        
        String ns = si.getName().getNamespaceURI();
        OperationInfo opi = intf.getOperation(new QName(ns, "createSequence"));
        assertEquals("createSequence", opi.getInput().getName().getLocalPart());
        
        List<MessagePartInfo> messageParts = opi.getInput().getMessageParts();
        assertEquals(1, messageParts.size());
        MessagePartInfo mpi = messageParts.get(0);
        System.out.println("name of first part of input message: " + mpi.getName().getLocalPart());
    }
}
