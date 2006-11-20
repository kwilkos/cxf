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

package org.apache.cxf.systest.ws.rm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 * 
 */
public class OutMessageRecorder extends AbstractPhaseInterceptor {
    
    private static final Logger LOG = Logger.getLogger(OutMessageRecorder.class.getName());
    private List<byte[]> outbound;
    private Set<String> before = Collections.singleton(StaxOutInterceptor.class.getName());

    public OutMessageRecorder() {
        outbound = new ArrayList<byte[]>();
        setPhase(Phase.PRE_PROTOCOL);
        // setPhase(Phase.POST_STREAM);
    }
    
    public void handleMessage(Message message) throws Fault {
        OutputStream os = message.getContent(OutputStream.class);
        if (null == os) {
            return;
        }
        if (os instanceof AbstractCachedOutputStream) {
            ((AbstractCachedOutputStream)os).registerCallback(new RecorderCallback());
        }
        /*
        ForkOutputStream fos = new ForkOutputStream(os);
        message.setContent(OutputStream.class, fos);
        */
    }
   
    @Override
    public Set<String> getBefore() {
        return before;
    }


    protected List<byte[]> getOutboundMessages() {
        return outbound;
    } 

    /**
      * Output stream that multicasts its data to several underlying output streams.
     */
    class ForkOutputStream extends OutputStream {

        final OutputStream original;
        ByteArrayOutputStream bos;
    
        public ForkOutputStream(OutputStream o) {
            original = o;
            bos = new ByteArrayOutputStream();
        }
    
        @Override
        public void close() throws IOException {            
            original.close();
            bos.close();
            outbound.add(bos.toByteArray());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("outbound: " + bos.toString());
            }
        }

        @Override
        public void flush() throws IOException {            
            original.flush();
            bos.flush();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {            
            original.write(b, off, len);
            bos.write(b, off, len);
        }
    
        @Override
        public void write(byte[] b) throws IOException {            
            original.write(b);
            bos.write(b);
        }
    
        @Override
        public void write(int b) throws IOException {
            original.write(b);
            bos.write(b);
        }    
    }
    
    class RecorderCallback implements CachedOutputStreamCallback {

        public void onFlush(AbstractCachedOutputStream cos) {  
            // LOG.fine("flushing wrapped output stream: " + cos.getOut().getClass().getName());
            OutputStream os = cos.getOut();
            if (os instanceof ByteArrayOutputStream) {
                ByteArrayOutputStream bos = (ByteArrayOutputStream)os;
                outbound.add(bos.toByteArray());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("outbound: " + bos.toString());
                }
            }
            
        }
        
        public void onClose(AbstractCachedOutputStream cos) {
            // bytes were already copied after flush
        }
        
    }

}
