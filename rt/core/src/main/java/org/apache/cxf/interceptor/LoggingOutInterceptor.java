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

package org.apache.cxf.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * 
 */
public class LoggingOutInterceptor extends AbstractPhaseInterceptor {
   
    private static final Logger LOG = LogUtils.getL7dLogger(LoggingOutInterceptor.class); 
    private Set<String> before = Collections.singleton(StaxOutInterceptor.class.getName());

    public LoggingOutInterceptor() {
        setPhase(Phase.PRE_PROTOCOL);
    }
    
    public void handleMessage(Message message) throws Fault {
        OutputStream os = message.getContent(OutputStream.class);
        if (os == null) {
            return;
        }
        if (os instanceof AbstractCachedOutputStream) {
            ((AbstractCachedOutputStream)os).registerCallback(new LoggingCallback());
        }
    }

    @Override
    public Set<String> getBefore() {
        return before;
    }

    class LoggingCallback implements CachedOutputStreamCallback {

        private boolean flushed;

        public void onFlush(AbstractCachedOutputStream cos) {  
            if (!flushed) {
                OutputStream os = cos.getOut();
                if (os instanceof ByteArrayOutputStream) {
                    ByteArrayOutputStream bos = (ByteArrayOutputStream)os;
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Outbound message: " + bos.toString());
                    }
                }
                flushed = true;
                // any further changes will not be logged
            }
        }
        
        public void onClose(AbstractCachedOutputStream cos) {
        }
        
    } 
    
}
