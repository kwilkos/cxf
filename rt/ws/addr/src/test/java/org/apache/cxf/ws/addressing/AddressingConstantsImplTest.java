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

package org.apache.cxf.ws.addressing;


import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class AddressingConstantsImplTest extends TestCase {
    private AddressingConstants constants;

    public void setUp() {
        constants = new AddressingConstantsImpl();
    }

    public void testGetNamespaceURI() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing",
                     constants.getNamespaceURI());
    }

    public void testGetNamespacePrefix() throws Exception {
        assertEquals("unexpected constant",
                     "wsa",
                     constants.getNamespacePrefix());
    }

    public void testGetWSDLNamespaceURI() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing/wsdl",
                     constants.getWSDLNamespaceURI());
    }

    public void testGetWSDLNamespacePrefix() throws Exception {
        assertEquals("unexpected constant",
                     "wsaw",
                     constants.getWSDLNamespacePrefix());
    }

    public void testGetWSDLExtensibility() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing/wsdl",
                               "UsingAddressing"),
                     constants.getWSDLExtensibilityQName());
    }

    public void testGetWSDLActionQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing/wsdl",
                               "Action"),
                     constants.getWSDLActionQName());
    }

    public void testGetAnonymousURI() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing/anonymous",
                     constants.getAnonymousURI());
    }

    public void testGetNoneURI() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing/none",
                     constants.getNoneURI());
    }

    public void testGetFromQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "From"),
                     constants.getFromQName());
    }

    public void testGetToQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "To"),
                     constants.getToQName());
    }

    public void testGetReplyToQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "ReplyTo"),
                     constants.getReplyToQName());
    }

    public void testGetFaultToQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "FaultTo"),
                     constants.getFaultToQName());
    }

    public void testGetActionQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "Action"),
                     constants.getActionQName());
    }

    public void testGetMessageIDQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "MessageID"),
                     constants.getMessageIDQName());
    }

    public void testGetRelationshipReply() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing/reply",
                     constants.getRelationshipReply());
    }

    public void testGetRelatesToQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "RelatesTo"),
                     constants.getRelatesToQName());
    }

    public void testGetRelationshipTypeQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "RelationshipType"),
                     constants.getRelationshipTypeQName());
    }

    public void testGetMetadataQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "Metadata"),
                     constants.getMetadataQName());
    }

    public void testGetAddressQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "Address"),
                     constants.getAddressQName());
    }

    public void testGetPackageName() throws Exception {
        assertEquals("unexpected constant",
                     "org.apache.cxf.ws.addressing",
                     constants.getPackageName());
    }

    public void testGetIsReferenceParameterQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "IsReferenceParameter"),
                     constants.getIsReferenceParameterQName());
    }

    public void testGetInvalidMapQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "InvalidMessageAddressingProperty"),
                     constants.getInvalidMapQName());
    }

    public void testMapRequiredQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "MessageAddressingPropertyRequired"),
                     constants.getMapRequiredQName());
    }

    public void testDestinationUnreachableQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "DestinationUnreachable"),
                     constants.getDestinationUnreachableQName());
    }

    public void testActionNotSupportedQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "ActionNotSupported"),
                     constants.getActionNotSupportedQName());
    }

    public void testEndpointUnavailableQName() throws Exception {
        assertEquals("unexpected constant",
                     new QName("http://www.w3.org/2005/08/addressing",
                               "EndpointUnavailable"),
                     constants.getEndpointUnavailableQName());
    }

    public void testDefaultFaultAction() throws Exception {
        assertEquals("unexpected constant",
                     "http://www.w3.org/2005/08/addressing/fault",
                     constants.getDefaultFaultAction());
    }

    public void testActionNotSupportedText() throws Exception {
        assertEquals("unexpected constant",
                     "Action {0} not supported",
                     constants.getActionNotSupportedText());
    }

    public void testDestinationUnreachableText() throws Exception {
        assertEquals("unexpected constant",
                     "Destination {0} unreachable",
                     constants.getDestinationUnreachableText());
    }

    public void testEndpointUnavailableText() throws Exception {
        assertEquals("unexpected constant",
                     "Endpoint {0} unavailable",
                     constants.getEndpointUnavailableText());
    }

    public void testGetInvalidMapText() throws Exception {
        assertEquals("unexpected constant",
                     "Invalid Message Addressing Property {0}",
                     constants.getInvalidMapText());
    }


    public void testMapRequiredText() throws Exception {
        assertEquals("unexpected constant",
                     "Message Addressing Property {0} required",
                     constants.getMapRequiredText());
    }

    public void testDuplicateMessageIDText() throws Exception {
        assertEquals("unexpected constant",
                     "Duplicate Message ID {0}",
                     constants.getDuplicateMessageIDText());
    }
}
