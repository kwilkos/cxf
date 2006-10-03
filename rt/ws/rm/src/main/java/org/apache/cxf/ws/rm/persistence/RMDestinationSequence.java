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

package org.apache.cxf.ws.rm.persistence;

import java.io.InputStream;
import java.math.BigInteger;

import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;

public interface RMDestinationSequence {
    
    /**
     * @return the sequence identifier
     */
    Identifier getIdentifier();
    
    /**
     * @return the acksTo address for the sequence
     */
    EndpointReferenceType getAcksTo();
    
    /**
     * @return the message number of the last message or null if the last message had not been received.
     */
    BigInteger getLastMessageNr();
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination 
     */
    SequenceAcknowledgement getAcknowledgment();
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination
     * as an input stream 
     */
    InputStream getAcknowledgmentAsStream();
    
    /**
     * @return the identifier of the rm destination
     */
    String getEndpointIdentifier(); 
    
    
}
