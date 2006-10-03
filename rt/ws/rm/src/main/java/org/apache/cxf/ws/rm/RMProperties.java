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

import java.util.Collection;

/**
 * Abstraction of Reliable Messaging Properties. 
 */

public interface RMProperties {
    
    /**
     * Accessor for the <b>Sequence</b> property.
     * @return current value of Sequence property
     */
    SequenceType getSequence();
    
    /**
     * Mutator for the <b>Sequence</b> property.
     * @param st new value for Sequence property
     */
    void setSequence(SequenceType st);
    
    /**
     * Accessor for the <b>Acks</b> property.
     * @return current value of Acks property
     */
    Collection<SequenceAcknowledgement> getAcks();
    
    /**
     * Mutator for the <b>Acks</b> property.
     * @param acks new value for Acks property
     */
    void setAcks(Collection<SequenceAcknowledgement> acks);
    
    /**
     * Accessor for the <b>AcksRequested</b> property.
     * @return current value of AcksRequested property
     */
    Collection<AckRequestedType> getAcksRequested();
    
    /**
     * Mutator for the <b>AcksRequested</b> property.
     * @param acks new value for AcksRequested property
     */
    void setAcksRequested(Collection<AckRequestedType> acks);    

}
