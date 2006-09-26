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
import java.lang.reflect.UndeclaredThrowableException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.mime.Hello;
import org.apache.cxf.mime.HelloService;
import org.apache.cxf.mtom_xop.HelloImpl;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;

public class ClientServerMtomXopTest extends ClientServerTestBase {

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new HelloImpl();
            String address = "http://localhost:9036/mime-test";
            Endpoint.publish(address, implementor);
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
        TestSuite suite = new TestSuite(ClientServerMtomXopTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testMtomSWA() throws Exception {
        HelloService hs = new HelloService();
        Hello hello = hs.getPort(Hello.class);
        try {
            InputStream pre = this.getClass().getResourceAsStream("/wsdl/mtom_xop.wsdl");
            long fileSize = 0;
            for (int i = pre.read(); i != -1; i = pre.read()) {
                fileSize++;
            }

            ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass().getResourceAsStream(
                            "/wsdl/mtom_xop.wsdl"), "application/octet-stream");
            DataHandler dh = new DataHandler(bads);
            DataHandler dhResp = hello.claimForm(dh);
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

    public void testMtomXop() throws Exception {
        HelloService hs = new HelloService();
        Hello hello = hs.getPort(Hello.class);
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
            hello.detail(name, param);
            assertEquals("name unchanged", "return detail + call detail", name.value);
            assertEquals("attachinfo changed", target, new String(param.value));
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

}
