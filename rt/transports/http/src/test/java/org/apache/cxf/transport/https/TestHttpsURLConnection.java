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

package org.apache.cxf.transport.https;

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

class TestHttpsURLConnection extends HttpsURLConnection {

    protected TestHttpsURLConnection(URL arg0) {
        super(arg0);
    }

    public String getCipherSuite() {
        return null;
    }
    
    public void disconnect() {
       
    }

    @Override
    public boolean usingProxy() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void connect() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Certificate[] getLocalCertificates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        // TODO Auto-generated method stub
        return null;
    }
}
