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


public class TransportServerCounters {
    private static final String[] COUNTER_NAMES = {"RequestsTotal",
                                                   "RequestsOneWay",
                                                   "TotalError"};
    private Counter[] counters;
    
    
    private String owner;
    
    public TransportServerCounters(String o) {
        owner = o;
        counters = new Counter[COUNTER_NAMES.length];
        initCounters();
    }
    public String getOwner() {
        return owner;
    }
           
    public Counter getRequestTotal() {
        return counters[0];
    }
    
    public Counter getRequestOneWay() {
        return counters[1];        
    }
    
    public Counter getTotalError() {
        return counters[2];
    
    }
    
    private void initCounters() {
        for (int i = 0; i < COUNTER_NAMES.length; i++) {
            Counter c = new Counter(COUNTER_NAMES[i]);
            counters[i] = c;
        }   
    }
    
    public void resetCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i].reset();
        }
    } 
    
    public void stopCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i].stop();
        }
    }
    
    
}
