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

import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.SequenceFault;
import org.apache.cxf.ws.rm.SequenceFaultType;

/**
 * 
 */

public class SequenceFaultFactory { 

    private static final Logger LOG = LogUtils.getL7dLogger(SequenceFaultFactory.class);

    SequenceFault createUnknownSequenceFault(Identifier sid) {
        SequenceFaultType sf = RMUtils.getWSRMFactory().createSequenceFaultType();
        sf.setFaultCode(RMUtils.getRMConstants().getUnknownSequenceFaultCode());
        Message msg = new Message("UNKNOWN_SEQUENCE_EXC", LOG, sid.getValue());
        return new SequenceFault(msg.toString(), sf);
    }    
}
