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

package org.apache.cxf.transport.http;

import java.io.OutputStream;

import org.mortbay.http.HttpResponse;

/**
 * EasyMock does not seem able to properly mock calls to HttpResponse -
 * expectations set seem to be ignored.
 */
class TestHttpResponse extends HttpResponse {
    private OutputStream os;
    private int[] callCounts = {0, 0, 0, 0, 0};
    
    TestHttpResponse(OutputStream o) {
        os = o;
    }
    
    public void commit() {
        callCounts[0]++;
    }
    
    int getCommitCallCount() {
        return callCounts[0];
    }
    
    public OutputStream getOutputStream() {
        callCounts[1]++;
        return os;
    }

    int getOutputStreamCallCount() {
        return callCounts[1];
    }
    
    public void sendRedirect(String url) {
        callCounts[2]++;
    }
    
    int getSendRedirectCallCount() {
        return callCounts[2];
    }
    
    public void setStatus(int s) {
        super.setStatus(s);
        callCounts[3]++;
    }
    
    int getStatusCallCount() {
        return callCounts[3];
    }
    
    public void addField(String name, String value) {
        super.addField(name, value);
        callCounts[4]++;
    }
    
    int getAddFieldCallCount() {
        return callCounts[4];
    }
}

