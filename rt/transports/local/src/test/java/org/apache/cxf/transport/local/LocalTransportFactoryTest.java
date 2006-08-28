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

package org.apache.cxf.transport.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class LocalTransportFactoryTest extends TestCase {
    public void testTransportFactory() throws Exception {
        LocalTransportFactory factory = new LocalTransportFactory();
        
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        AddressType a = new AddressType();
        a.setLocation("http://localhost/test");
        ei.addExtensor(a);

        Destination d = factory.getDestination(ei);
        d.setMessageObserver(new EchoObserver());
        
        
        Conduit conduit = factory.getConduit(ei);
        TestMessageObserver obs = new TestMessageObserver();
        conduit.setMessageObserver(obs);
        
        Message m = new MessageImpl();
        conduit.send(m);

        OutputStream out = m.getContent(OutputStream.class);
        out.write("hello".getBytes());
        out.close();

        
        assertEquals("hello", obs.getResponseStream().toString());
    }

    static class EchoObserver implements MessageObserver {

        public void onMessage(Message message) {
            try {
                Conduit backChannel = message.getDestination().getBackChannel(message, null, null);

                backChannel.send(message);

                OutputStream out = message.getContent(OutputStream.class);
                assertNotNull(out);
                InputStream in = message.getContent(InputStream.class);
                assertNotNull(in);
                
                copy(in, out, 1024);

                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void copy(final InputStream input, final OutputStream output, final int bufferSize)
        throws IOException {
        try {
            final byte[] buffer = new byte[bufferSize];

            int n = input.read(buffer);
            while (-1 != n) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    
    
    class TestMessageObserver implements MessageObserver {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        boolean written;
        
        public synchronized ByteArrayOutputStream getResponseStream() throws Exception {
            if (!written) {
                wait();
            }
            return response;
        }
        

        public synchronized void onMessage(Message message) {
            try {
                copy(message.getContent(InputStream.class), response, 1024);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            } finally {
                written = true;
                notifyAll();
            }
        }
    }
}
