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

package org.apache.cxf.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.hello_world_soap_http.BadRecordLitFault;
import org.apache.hello_world_soap_http.GreeterImpl;

public class JaxWsClientTest extends AbstractJaxWsTest {

    static String responseMessage;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        ei.setAddress("http://localhost:9000/SoapContext/SoapPort");

        Destination d = localTransport.getDestination(ei);
        d.setMessageObserver(new EchoObserver());
    }

    public void testCreate() throws Exception {
        javax.xml.ws.Service s = javax.xml.ws.Service
            .create(new QName("http://apache.org/hello_world_soap_http", "SoapPort"));
        assertNotNull(s);

        try {
            s = javax.xml.ws.Service.create(new URL("file:/does/not/exist.wsdl"),
                                            new QName("http://apache.org/hello_world_soap_http", "SoapPort"));
        } catch (ServiceConstructionException sce) {
            // ignore, this is expected
        }
    }

    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);
        bean.setBus(getBus());
        bean.setServiceClass(GreeterImpl.class);
        GreeterImpl greeter = new GreeterImpl();
        BeanInvoker invoker = new BeanInvoker(greeter);
        bean.setInvoker(invoker);

        Service service = bean.create();

        String namespace = "http://apache.org/hello_world_soap_http";
        EndpointInfo ei = service.getServiceInfo().getEndpoint(new QName(namespace, "SoapPort"));
        JaxWsEndpointImpl endpoint = new JaxWsEndpointImpl(getBus(), service, ei);

        ClientImpl client = new ClientImpl(getBus(), endpoint);

        BindingOperationInfo bop = ei.getBinding().getOperation(new QName(namespace, "sayHi"));
        assertNotNull(bop);
        bop = bop.getUnwrappedOperation();
        assertNotNull(bop);

        responseMessage = "sayHiResponse.xml";
        Object ret[] = client.invoke(bop, new Object[0], null);
        assertNotNull(ret);
        assertEquals("Wrong number of return objects", 1, ret.length);

        // test fault handling
        bop = ei.getBinding().getOperation(new QName(namespace, "testDocLitFault"));
        responseMessage = "testDocLitFault.xml";
        try {
            client.invoke(bop, new Object[] {"BadRecordLitFault"}, null);
            fail("Should have returned a fault!");
        } catch (BadRecordLitFault fault) {
            assertEquals("foo", fault.getFaultInfo().trim());
            assertEquals("Hadrian did it.", fault.getMessage());
        }
        
        try {
            client.getEndpoint().getOutInterceptors().add(new FaultThrower());
            client.invoke(bop, new Object[] {"BadRecordLitFault"}, null);
            fail("Should have returned a fault!");
        } catch (Fault fault) {
            assertEquals("Foo", fault.getCode());
            assertEquals("Foo", fault.getMessage());
        } 
    }

    public static class FaultThrower extends AbstractPhaseInterceptor<Message> {
        
        public FaultThrower() {
            super();
            setPhase(Phase.PRE_LOGICAL);
        }

        public void handleMessage(Message message) throws Fault {
            throw new Fault(new org.apache.cxf.common.i18n.Message("Foo", (ResourceBundle)null));
        }

    }

    static class EchoObserver implements MessageObserver {

        public void onMessage(Message message) {
            try {
                Conduit backChannel = message.getDestination().getBackChannel(message, null, null);

                backChannel.send(message);

                InputStream in = message.getContent(InputStream.class);
                while (in.available() > 0) {
                    in.read();
                }

                OutputStream out = message.getContent(OutputStream.class);
                assertNotNull(out);
                in = getClass().getResourceAsStream(responseMessage);
                copy(in, out, 2045);

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
}
