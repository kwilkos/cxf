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
package org.apache.cxf.systest.mtom;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.EndpointInvocationHandler;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.binding.soap.SOAPBindingImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.mime.TestMtom;
import org.apache.cxf.mtom_xop.TestMtomImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.cxf.transport.ChainInitiationObserver;
import org.apache.cxf.transport.MessageObserver;

public class ClientMtomXopTest extends ClientServerTestBase {

    public static final QName MTOM_PORT = new QName("http://cxf.apache.org/mime", "TestMtomPort");
    public static final QName MTOM_SERVICE = new QName("http://cxf.apache.org/mime", "TestMtomService");

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new TestMtomImpl();
            String address = "http://localhost:9036/mime-test";
            try {
                Bus bus = BusFactoryHelper.newInstance().getDefaultBus();
                JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(implementor.getClass());
                AbstractServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean(implInfo);
                serviceFactory.setBus(bus);
                Service service = serviceFactory.create();
                QName endpointName = implInfo.getEndpointName();
                EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
                service.setInvoker(new JAXWSMethodInvoker(implementor));
                org.apache.cxf.endpoint.EndpointImpl endpoint = new JaxWsEndpointImpl(bus, service, ei);
                SOAPBinding jaxWsSoapBinding = new SOAPBindingImpl(ei.getBinding()); 
                jaxWsSoapBinding.setMTOMEnabled(true);

                endpoint.getInInterceptors().add(new TestMultipartMessageInterceptor());
                endpoint.getOutInterceptors().add(new TestAttachmentOutInterceptor());
                
                endpoint.getEndpointInfo().setAddress(address);
                MessageObserver observer = new ChainInitiationObserver(endpoint, bus);
                ServerImpl server = new ServerImpl(bus, endpoint, observer);
                server.start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }            
        }

        public static void main(String args[]) {
            try {
                Server s = new Server();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClientMtomXopTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }
    /*
    public void testMtomSWA() throws Exception {
        TestMtom mtomPort = createPort(MTOM_SERVICE, MTOM_PORT, TestMtom.class);
        try {
            InputStream pre = this.getClass().getResourceAsStream("/wsdl/mtom_xop.wsdl");
            long fileSize = 0;
            for (int i = pre.read(); i != -1; i = pre.read()) {
                fileSize++;
            }

            ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass().getResourceAsStream(
                    "/wsdl/mtom_xop.wsdl"), "application/octet-stream");
            DataHandler dh = new DataHandler(bads);
            DataHandler dhResp = mtomPort.testSWA(dh);
            DataSource ds = dhResp.getDataSource();
            InputStream in = ds.getInputStream();

            long count = 0;
            for (int i = in.read(); i != -1; i = in.read()) {
                count++;
            }
            assertEquals("attachemnt length different", fileSize, count);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }
    */
    public void testMtomXop() throws Exception {
        TestMtom mtomPort = createPort(MTOM_SERVICE, MTOM_PORT, TestMtom.class);
        try {
            InputStream pre = this.getClass().getResourceAsStream("/wsdl/mtom_xop.wsdl");
            long fileSize = 0;
            for (int i = pre.read(); i != -1; i = pre.read()) {
                fileSize++;
            }
            Holder<byte[]> param = new Holder<byte[]>();
            param.value = new byte[(int) fileSize];
            this.getClass().getResourceAsStream("/wsdl/mtom_xop.wsdl").read(param.value);
            String target = new String(param.value);
            Holder<String> name = new Holder<String>("call detail");
            mtomPort.testXop(name, param);
            assertEquals("name unchanged", "return detail + call detail", name.value);
            assertEquals("attachinfo changed", target, new String(param.value));
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    private static <T> T createPort(QName serviceName, QName portName, Class<T> serviceEndpointInterface)
        throws Exception {
        Bus bus = BusFactoryHelper.newInstance().getDefaultBus();
        JaxWsServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean();
        serviceFactory.setBus(bus);
        serviceFactory.setServiceName(serviceName);
        serviceFactory.setServiceClass(serviceEndpointInterface);
        Service service = serviceFactory.create();
        ServiceInfo si = service.getServiceInfo();
        EndpointInfo ei = null;
        ei = si.getEndpoint(portName);
        JaxWsEndpointImpl jaxwsEndpoint = new JaxWsEndpointImpl(bus, service, ei);
        SOAPBinding jaxWsSoapBinding = new SOAPBindingImpl(ei.getBinding());
        jaxWsSoapBinding.setMTOMEnabled(true);
        
        jaxwsEndpoint.getBinding().getInInterceptors().add(new TestMultipartMessageInterceptor());
        jaxwsEndpoint.getBinding().getOutInterceptors().add(new TestAttachmentOutInterceptor());
        
        Client client = new ClientImpl(bus, jaxwsEndpoint);
        InvocationHandler ih = new EndpointInvocationHandler(client, jaxwsEndpoint.getJaxwsBinding());
        Object obj = Proxy.newProxyInstance(serviceEndpointInterface.getClassLoader(), new Class[] {
            serviceEndpointInterface, BindingProvider.class }, ih);
        return serviceEndpointInterface.cast(obj);
    }
}
