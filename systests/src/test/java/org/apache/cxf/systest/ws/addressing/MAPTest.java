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

package org.apache.cxf.systest.ws.addressing;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.systest.ws.util.ConnectionHelper;
import org.apache.hello_world_soap_http.BadRecordLitFault;
import org.junit.Test;


/**
 * Tests the addition of WS-Addressing Message Addressing Properties.
 */
public class MAPTest extends MAPTestBase {

    private static final String CONFIG;
    static {
        CONFIG = "org/apache/cxf/systest/ws/addressing/cxf" 
            + ("HP-UX".equals(System.getProperty("os.name")) ? "-hpux" : "")
            + ".xml";
    }
    
    public String getConfigFileName() {
        return CONFIG;
    }
    
    @Test
    public void foo() {
        
    }
    
    @Test
    public void testUsingKeepAliveConnection() throws Exception {
        if (!"HP-UX".equals(System.getProperty("os.name"))) {
            return;
        }
        int n = 100;
        for (int i = 0; i < n; i++) {
            greeter.greetMeOneWay("oneway on keep-alive connection");
        }
        for (int i = 0; i < n; i++) {
            assertNotNull(greeter.greetMe("twoway on keep-alive connection"));
        }
        for (int i = 0; i < 0; i++) {
            try {
                greeter.testDocLitFault("BadRecordLitFault");
                fail("expected fault from service");
            } catch (BadRecordLitFault brlf) {
                //checkVerification();
            } catch (UndeclaredThrowableException ex) {
                throw (Exception)ex.getCause();
            }
        }
    }

    /**
     * On HP-UX, the server seems to close the connection by the time the
     * thread servicing the requests terminates and therefore possibly before 
     * the client has had a chance to read the response (the client throws
     * a SocketException: Broken pipe). This may be a bug
     * in Jetty or in the HP-UX JDK. It can be worked around by 
     * adding a sleep to the end of method handle in 
     * org.apache.cxf.transport.http_jetty.JettyHTTPHandler or,
     * preferrably, by ensuring the client uses keep alive
     * connections.
     */
    @Test
    public void testDelayReadingPartialResponse() throws Exception {
        if (!"HP-UX".equals(System.getProperty("os.name"))) {
            return;
        }

        assertTrue(ConnectionHelper.isKeepAliveConnection(greeter));
        ConnectionHelper.setKeepAliveConnection(greeter, false);

        class DelayInterceptor extends AbstractPhaseInterceptor<Message> {
            long delay = 100L;
            DelayInterceptor() {
                super(Phase.RECEIVE);
            }
            public void handleMessage(Message msg) {
                try {
                    Thread.sleep(delay);
                } catch (Exception ex) {
                    // ignore
                } finally {
                    if (delay < 1000L) {  
                        delay += 100L;
                    }
                }
            }
        }
        DelayInterceptor interceptor = new DelayInterceptor();
        staticBus.getInInterceptors().add(interceptor);

        int n = 100;
        try {
            for (int i = 0; i < n; i++) {
                greeter.greetMeOneWay("delay reading partial reponse");
            }
            fail("Expected SocketException not thrown");
        } catch (Exception ex) {
            Throwable t = ex.getCause();
            while (null != t.getCause()) {
                t = t.getCause();
            }
            assertTrue("Unexpected exception type: " + t.getClass().getName(),
                t instanceof SocketException);
        } finally {
            // need to reset to Keep-Alive for subsequenct tests
            // (all tests are using the same conduit selector, and
            // thus the same conduit)
            ConnectionHelper.setKeepAliveConnection(greeter, true);
            staticBus.getInInterceptors().remove(interceptor);
        }
    }
}

