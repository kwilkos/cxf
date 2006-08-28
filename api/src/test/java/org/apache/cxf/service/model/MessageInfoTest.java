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

public class MessageInfoTest extends TestCase {
    
    private MessageInfo messageInfo;
    
    public void setUp() throws Exception {
        messageInfo = new MessageInfo(null, new QName(
            "http://apache.org/hello_world_soap_http", "testMessage"));
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertEquals(messageInfo.getName().getLocalPart(), "testMessage");
        assertEquals(messageInfo.getName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http");
    }
    
    public void testMessagePartInfo() throws Exception {
        QName qname = new QName(
                                "http://apache.org/hello_world_soap_http", "testMessagePart");
        
        messageInfo.addMessagePart(qname);
        assertEquals(messageInfo.getMessageParts().size(), 1);
        MessagePartInfo messagePartInfo = messageInfo.getMessagePart(qname);
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart");
        assertEquals(messagePartInfo.getName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http");
        assertEquals(messagePartInfo.getMessageInfo(), messageInfo);
        messagePartInfo = new MessagePartInfo(new QName(
             "http://apache.org/hello_world_soap_http", "testMessagePart"), messageInfo);
        messageInfo.addMessagePart(messagePartInfo);
        //add two same part, so size is still 1
        assertEquals(messageInfo.getMessageParts().size(), 1);
        messagePartInfo = new MessagePartInfo(new QName(
            "http://apache.org/hello_world_soap_http", "testMessagePart2"), messageInfo);
        messageInfo.addMessagePart(messagePartInfo);
        assertEquals(messageInfo.getMessageParts().size(), 2);
    }

}
