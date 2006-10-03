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

package org.apache.cxf.ws.rm;

import javax.xml.namespace.QName;


public interface RMConstants {
    /**
     * @return namespace defined by the normative WS-RM schema
     */
    String getNamespaceURI();
    
    /**
     * @return namespace defined by the WS-RM Policy schema
     */
    String getRMPolicyNamespaceURI();
    
    /**
     * @return namespace defined by the normative WS-RM WSDL bindings
     * schema
     */
    String getWSDLNamespaceURI();
    
    /**
     * @return CreateSequence Action
     */
    String getCreateSequenceAction();
    
    
    /**
     * @return CreateSequenceResponse Action
     */
    String getCreateSequenceResponseAction();
    
    /**
     * @return TerminateSequence Action
     */
    String getTerminateSequenceAction();
    
    /**
     * @return LastMessage Action
     */
    String getLastMessageAction();
    
    /**
     * @return SequenceAcknowledgment Action
     */
    String getSequenceAcknowledgmentAction();
    
    
    /**
     * @return SequenceInfo Action
     */
    String getSequenceInfoAction();
    
    /**
     * @return UnknownSequence fault code
     */
    QName getUnknownSequenceFaultCode();
        
    /**
     * @return SequenceTerminated fault code
     */
    QName getSequenceTerminatedFaultCode();
        
    /**
     * @return InvalidAcknowledgemt fault code
     */
    QName getInvalidAcknowledgmentFaultCode();
    
    /**
     * @return CreateSequenceRefused fault code
     */
    QName getCreateSequenceRefusedFaultCode();
    
    /**
     * @return MessageNumberRollover fault code
     */
    QName getMessageNumberRolloverFaultCode();
    
    
    /**
     * @return LastMessageNumberExceeded fault code
     */
    QName getLastMessageNumberExceededFaultCode();
  
}
