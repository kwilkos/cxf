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

package org.apache.cxf.management.counters;

/** class for the performance couter */
public class Counter {
    
    private String discription;
    private int value;
    private float rate;
    private Boolean runFlag;
    
    Counter(String disc) {
        discription = disc;
        runFlag = false;
    }
    
    public void reset() {
        value = 0;
        runFlag = true;
    }
    
    public int add(int i) {
        value = value + i;
        return value;
    }
    
    public final void increase() {
        if (runFlag) {
            value++;
        }
    }
    
    public String getDiscription() {
        return discription;
    }
    
    float getRate() {
        return rate;
    }
    
    public int getValue() {
        return value;
    }
    
    public void stop() {
        value = 0;
        runFlag = false;
    }
    
    void setRate(float r) {
        if (rate < 1 && rate > 0) {
            rate = r;
        }
        // else do nothing           
    }
    
    
}
