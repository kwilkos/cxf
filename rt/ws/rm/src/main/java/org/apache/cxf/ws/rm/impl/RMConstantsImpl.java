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


import javax.xml.namespace.QName;

import org.apache.cxf.ws.rm.RMConstants;

/**
 * Encapsulation of version-specific WS-RM constants.
 */
public class RMConstantsImpl implements RMConstants {

    RMConstantsImpl() {
    }
    
    public String getNamespaceURI() {
        return Names.WSRM_NAMESPACE_NAME;
    }
     
    public String getRMPolicyNamespaceURI() {
        return Names.WSRMP_NAMESPACE_NAME;
    }



    public String getWSDLNamespaceURI() {
        return Names.WSRM_WSDL_NAMESPACE_NAME;
    }
    
    public String getCreateSequenceAction() {
        return Names.WSRM_CREATE_SEQUENCE_ACTION;
    }

    public String getCreateSequenceResponseAction() {
        return Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION;
    }
    
    public String getTerminateSequenceAction() {
        return Names.WSRM_TERMINATE_SEQUENCE_ACTION;
    }
    
    public String getLastMessageAction() {
        return Names.WSRM_LAST_MESSAGE_ACTION;
    }
    
    public String getSequenceAcknowledgmentAction() {
        return Names.WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION;
    }
    
    public String getSequenceInfoAction() {
        return Names.WSRM_SEQUENCE_INFO_ACTION;
    }
    
    public QName getUnknownSequenceFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_UNKNOWN_SEQUENCE_FAULT_CODE);
    }
        
    public QName getSequenceTerminatedFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_SEQUENCE_TERMINATED_FAULT_CODE);
    }
        
    public QName getInvalidAcknowledgmentFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_INVALID_ACKNOWLEDGMENT_FAULT_CODE);
    }
  
    public QName getMessageNumberRolloverFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_MESSAGE_NUMBER_ROLLOVER_FAULT_CODE);
    }
    
    public QName getCreateSequenceRefusedFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_CREATE_SEQUENCE_REFUSED_FAULT_CODE);
    }
    
    public QName getLastMessageNumberExceededFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_LAST_MESSAGE_NUMBER_EXCEEDED_FAULT_CODE);
    }
    
}
