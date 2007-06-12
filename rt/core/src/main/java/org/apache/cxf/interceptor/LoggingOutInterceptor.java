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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * 
 */
public class LoggingOutInterceptor extends AbstractPhaseInterceptor {
   
    private static final Logger LOG = LogUtils.getL7dLogger(LoggingOutInterceptor.class); 

    public LoggingOutInterceptor() {
        super(Phase.PRE_STREAM);
        addBefore(StaxOutInterceptor.class.getName());
    }
    
    public void handleMessage(Message message) throws Fault {
        final OutputStream os = message.getContent(OutputStream.class);
        if (os == null) {
            return;
        }
        
        // Write the output while caching it for the log message
        final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
        message.setContent(OutputStream.class, newOut);
        newOut.registerCallback(new LoggingCallback());
    }

    class LoggingCallback implements CachedOutputStreamCallback {

        public void onFlush(CachedOutputStream cos) {  
            
        }
        
        public void onClose(CachedOutputStream cos) {
            OutputStream os = cos.getOut();
            if (os instanceof ByteArrayOutputStream && LOG.isLoggable(Level.INFO)) {
                // TODO - make this work with large messages
                LOG.info("Outbound Message \n"
                         + "--------------------------------------\n"
                         + os.toString()
                         + "\n--------------------------------------");
            }
        }
        
    } 
}
