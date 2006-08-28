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

package org.apache.cxf.service.model;


import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class MessagePartInfoTest extends TestCase {
    
        
    private MessagePartInfo messagePartInfo;
        
    public void setUp() throws Exception {
        
        messagePartInfo = new MessagePartInfo(new QName(
            "http://apache.org/hello_world_soap_http", "testMessagePart"), null);
        messagePartInfo.setIsElement(true);
        messagePartInfo.setElementQName(new QName(
            "http://apache.org/hello_world_soap_http/types", "testElement"));
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart");
        assertEquals(messagePartInfo.getName().getNamespaceURI()
                     , "http://apache.org/hello_world_soap_http");
        messagePartInfo.setName(new QName(
            "http://apache.org/hello_world_soap_http1", "testMessagePart1"));
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart1");
        assertEquals(messagePartInfo.getName().getNamespaceURI()
                     , "http://apache.org/hello_world_soap_http1");
        
    }

    public void testElement() {
        assertTrue(messagePartInfo.isElement());
        assertEquals(messagePartInfo.getElementQName().getLocalPart(), "testElement");
        assertEquals(messagePartInfo.getElementQName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http/types");
        assertNull(messagePartInfo.getTypeQName());
    }
    
    public void testType() {
        messagePartInfo.setTypeQName(new QName(
            "http://apache.org/hello_world_soap_http/types", "testType"));
        assertNull(messagePartInfo.getElementQName());
        assertFalse(messagePartInfo.isElement());
        assertEquals(messagePartInfo.getTypeQName().getLocalPart(), "testType");
        assertEquals(messagePartInfo.getTypeQName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http/types");
    }
}
