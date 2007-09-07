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

package demo.mtom.client;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.mime.TestMtom;
import org.apache.cxf.mime.TestMtomService;

public final class Client {

    private static final QName SERVICE_NAME = new QName("http://cxf.apache.org/mime", "TestMtomService");

    private static final QName PORT_NAME = new QName("http://cxf.apache.org/mime", "TestMtomPort");

    private Client() {
    }

    public static void main(String args[]) throws Exception {

        Client client = new Client();

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }
        URL wsdlURL;
        File wsdlFile = new File(args[0]);

        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURL();
        } else {
            wsdlURL = new URL(args[0]);
        }
        System.out.println(wsdlURL);

        TestMtomService tms = new TestMtomService(wsdlURL, SERVICE_NAME);
        TestMtom port = (TestMtom) tms.getPort(PORT_NAME, TestMtom.class);
        Binding binding = ((BindingProvider)port).getBinding();
        ((SOAPBinding)binding).setMTOMEnabled(true);

        InputStream pre = client.getClass().getResourceAsStream("me.bmp");
        long fileSize = 0;
        for (int i = pre.read(); i != -1; i = pre.read()) {
            fileSize++;
        }
        Holder<byte[]> param = new Holder<byte[]>();
        param.value = new byte[(int) fileSize];
        System.out.println("Start test without Mtom enable!");
        System.out.println("Sending out the me.bmp Image content to server, data size is " + fileSize);

        InputStream in = client.getClass().getResourceAsStream("me.bmp");
        in.read(param.value);
        Holder<String> name = new Holder<String>("call detail");
        port.testXop(name, param);
        System.out.println("received byte[] back from server, the size is " + param.value.length);

        Image image = ImageIO.read(new ByteArrayInputStream(param.value));
        System.out.println("build image with the returned byte[] back from server successfully, hashCode="
                + image.hashCode());
        System.out.println("Successfully run demo without mtom enable");

        System.out.println("Start test with Mtom enable!");        
        System.out.println("Sending out the me.bmp Image content to server, data size is " + fileSize);
        Holder<DataHandler> handler = new Holder<DataHandler>();
        byte[] data = new byte[(int) fileSize];
        client.getClass().getResourceAsStream("me.bmp").read(data);
        handler.value = new DataHandler(new ByteArrayDataSource(data, "application/octet-stream"));
        port.testMtom(name, handler);
        InputStream mtomIn = handler.value.getInputStream();
        fileSize = 0;
        
        for (int i = mtomIn.read(); i != -1; i = mtomIn.read()) {
            fileSize++;
        }

        System.out.println("received DataHandler back from server, the size is " + fileSize);
        System.out.println("Successfully run demo with mtom enable");
        System.exit(0);
    }

    private static InputStream getResourceStream(File file) throws Exception {
        InputStream in = new FileInputStream(file);
        return in;
    }
}
