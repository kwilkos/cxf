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

package org.apache.cxf.ws.rm.policy;

import java.math.BigInteger;
import java.util.Collection;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.cxf.ws.rm.RMConstants;

/**
 * 
 */
public final class PolicyUtils {
    
    /**
     * Prevents instantiation.
     *
     */
    private PolicyUtils() {        
    }


    /**
     * Returns the base retransmission interval for the specified message.
     * This is obtained as the minimum base retransmission interval in all RMAssertions pertaining
     * to the message, or null if there are no such policy assertions.
     * @param message the message
     * @return the base retransmission interval for the message
     */
    public static BigInteger getBaseRetransmissionInterval(Message message) {
        AssertionInfoMap amap =  message.get(AssertionInfoMap.class);
        BigInteger result = null;
        if (null != amap) {
            Collection<AssertionInfo> ais = amap.get(RMConstants.getRMAssertionQName());
            if (null != ais) {
                for (AssertionInfo ai : ais) {
                    JaxbAssertion<RMAssertion> ja = getAssertion(ai);
                    RMAssertion rma = ja.getData();
                    RMAssertion.BaseRetransmissionInterval bri = rma.getBaseRetransmissionInterval();
                    if (null == bri) {
                        continue;
                    }
                    BigInteger val = bri.getMilliseconds();
                    if (null == result || val.compareTo(result) < 0) {
                        result = val;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Determines if exponential backoff should be used in repeated attempts to
     * resend the specified message. Returns false if there is at least one
     * RMAssertion for this message indicating that no exponential backoff
     * algorithm should be used, or true otherwise.
     * 
     * @param message the message
     * @return true iff the exponential backoff algorithm should be used for the
     *         message
     */
    public static boolean useExponentialBackoff(Message message) {
        AssertionInfoMap amap = message.get(AssertionInfoMap.class);
        if (null != amap) {
            Collection<AssertionInfo> ais = amap.get(RMConstants.getRMAssertionQName());
            if (null != ais) {
                for (AssertionInfo ai : ais) {
                    JaxbAssertion<RMAssertion> ja = getAssertion(ai);
                    RMAssertion rma = ja.getData();
                    if (null == rma.getExponentialBackoff()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Returns the acknowledgment interval for the specified message.
     * This is obtained as the minimum acknowledgment interval in all RMAssertions pertaining
     * to the message, or null of if there are no such policy assertions.
     * @param message the message
     * @return the base retransmission interval for the message
     */
    public static BigInteger getAcknowledgmentInterval(Message message) {
        AssertionInfoMap amap =  message.get(AssertionInfoMap.class);
        BigInteger result = null;
        if (null != amap) {
            Collection<AssertionInfo> ais = amap.get(RMConstants.getRMAssertionQName());
            if (null != ais) {
                for (AssertionInfo ai : ais) {
                    JaxbAssertion<RMAssertion> ja = getAssertion(ai);
                    RMAssertion rma = ja.getData();
                    RMAssertion.AcknowledgementInterval interval = rma.getAcknowledgementInterval();
                    if (null == interval) {
                        continue;
                    }
                    BigInteger val = interval.getMilliseconds();
                    if (null == result || val.compareTo(result) < 0) {
                        result = val;
                    }
                }
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private static JaxbAssertion<RMAssertion> getAssertion(AssertionInfo ai) {
        return (JaxbAssertion<RMAssertion>)ai.getAssertion();
    }
}
