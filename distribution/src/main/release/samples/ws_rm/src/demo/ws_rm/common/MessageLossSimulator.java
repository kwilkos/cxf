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

package demo.ws_rm.common;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RMProperties;

/**
 * 
 */
public class MessageLossSimulator extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = Logger.getLogger(MessageLossSimulator.class.getName());
    private int appMessageCount; 
    
    public MessageLossSimulator() {
        super();
        setPhase(Phase.PREPARE_SEND);
        addBefore(MessageSenderInterceptor.class.getName());
    }

    /**
      * Simulate loss of every second application message by replacing the stream normally 
      * provided by the transport (in the MessageSenderInterceptor)/ 
     */ 
    public void handleMessage(Message message) throws Fault {
        AddressingProperties maps =
            RMContextUtils.retrieveMAPs(message, false, true);
        RMContextUtils.ensureExposedVersion(maps);
        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        if (RMContextUtils.isRMProtocolMessage(action)) { 
            return;
        }
        appMessageCount++;
        if (0 != (appMessageCount % 2)) {
            return;
        }
        
        InterceptorChain chain = message.getInterceptorChain();
        ListIterator it = chain.getIterator();
        while (it.hasNext()) {
            PhaseInterceptor pi = (PhaseInterceptor)it.next();
            if (MessageSenderInterceptor.class.getName().equals(pi.getId())) {
                chain.remove(pi);
                LOG.fine("Removed MessageSenderInterceptor from interceptor chain.");
                break;
            }
        }
        
        message.setContent(OutputStream.class, new WrappedOutputStream(message));     
    }
    
    private class WrappedOutputStream extends AbstractWrappedOutputStream {

        public WrappedOutputStream(Message m) {
            super(m);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void doClose() throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected void doFlush() throws IOException {
            boolean af = alreadyFlushed();
            if (!af) {
                if (LOG.isLoggable(Level.INFO)) {
                    RMProperties props = RMContextUtils.retrieveRMProperties(outMessage, true);
                    if (props != null && props.getSequence() != null) {
                        BigInteger nr = props.getSequence().getMessageNumber();
                        LOG.info("Losing message " + nr);
                    }
                }
                resetOut(new DummyOutputStream(), true);
            }
        }

        @Override
        protected void onWrite() throws IOException {
            // TODO Auto-generated method stub
            
        } 
        
    }
    
    private class DummyOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
    
}
