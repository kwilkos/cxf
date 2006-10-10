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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;


/**
 * Holder for WS-RM names (of headers, namespaces etc.).
 */
public final class Names {
   
    static final String WSRM_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm";
    
    static final String WSRMP_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";
    
    static final String WSA_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    static final String WSRM_NAMESPACE_PREFIX = "wsrm";
    
    static final String WSRM_WSDL_NAMESPACE_NAME = 
        WSRM_NAMESPACE_NAME + "/wsdl";
    
    static final String CELTIX_WSRM_NAMESPACE_NAME = 
        "http://celtix.objectweb.org/ws/rm";
    
    static final String CELTIX_WSRM_WSDL_NAMESPACE_NAME = 
        CELTIX_WSRM_NAMESPACE_NAME + "/wsdl";
    
    static final String WSRM_CREATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequence";
    
    static final String WSRM_CREATE_SEQUENCE_RESPONSE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequenceResponse";
    
    static final String WSRM_TERMINATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/TerminateSequence";
    
    static final String WSRM_LAST_MESSAGE_ACTION =
        WSRM_NAMESPACE_NAME + "/LastMessage";
    
    static final String WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION =
        WSRM_NAMESPACE_NAME + "/SequenceAcknowledgement";
    
    static final String WSRM_SEQUENCE_INFO_ACTION =
        CELTIX_WSRM_NAMESPACE_NAME + "/SequenceInfo";
    
    static final String WSRM_UNKNOWN_SEQUENCE_FAULT_CODE =
        "UnknownSequence";
    
    static final String WSRM_SEQUENCE_TERMINATED_FAULT_CODE =
        "SequenceTerminated";
    
    static final String WSRM_INVALID_ACKNOWLEDGMENT_FAULT_CODE =
        "InvalidAcknowledgement";
    
    static final String WSRM_MESSAGE_NUMBER_ROLLOVER_FAULT_CODE =
        "MessageNumberRollover";
    
    static final String WSRM_CREATE_SEQUENCE_REFUSED_FAULT_CODE =
        "CreateSequenceRefused";
    
    static final String WSRM_LAST_MESSAGE_NUMBER_EXCEEDED_FAULT_CODE =
        "LastMessageNumberExceeded";
    
    static final String WSRM_SEQUENCE_NAME =
        "Sequence";
    
    static final QName WSRM_SEQUENCE_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_SEQUENCE_NAME);
    
    static final String WSRM_SEQUENCE_ACK_NAME =
        "SequenceAcknowledgement";
    
    static final QName WSRM_SEQUENCE_ACK_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_SEQUENCE_ACK_NAME);
    
    static final String WSRM_ACK_REQUESTED_NAME =
        "AckRequested";
    
    static final QName WSRM_ACK_REQUESTED_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_ACK_REQUESTED_NAME);
    
    static final String WSA_ANONYMOUS_ADDRESS = 
        WSA_NAMESPACE_NAME + "/anonymous";
    static final String WSA_NONE_ADDRESS =
        WSA_NAMESPACE_NAME + "/none";
    
    
    /**
     * The set of headers understood by the protocol binding.
     */
    static final Set<QName> HEADERS;
    static {
        Set<QName> headers = new HashSet<QName>();
        headers.add(WSRM_SEQUENCE_QNAME);
        headers.add(WSRM_SEQUENCE_ACK_QNAME);
        headers.add(WSRM_ACK_REQUESTED_QNAME);
        HEADERS = Collections.unmodifiableSet(headers);
    }
    
    private Names() {
    }
}
