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
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.mime.Hello;
import org.apache.cxf.mime.HelloService;

public final class Client {

    private static final QName SERVICE_NAME = new QName("http://cxf.apache.org/mime", "HelloService");

    private static final QName PORT_NAME = new QName("http://cxf.apache.org/mime", "HelloPort");

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

        HelloService ss = new HelloService(wsdlURL, SERVICE_NAME);
        Hello port = (Hello) ss.getPort(PORT_NAME, Hello.class);
        Binding binding = ((BindingProvider)port).getBinding();
        ((SOAPBinding)binding).setMTOMEnabled(true);

        ByteArrayDataSource bads = new ByteArrayDataSource(getResourceStream(wsdlFile),
                "Application/octet-stream");
        DataHandler dh = new DataHandler(bads);
        System.out.println("Start test the Soap Message with Attachment!");
        System.out.println("sending out the Client.java file content as attachment to server");
        DataHandler dhResp = port.claimForm(dh);
        DataSource ds = dhResp.getDataSource();
        InputStream in = ds.getInputStream();
        System.out.println("get back the mtom_xop.wsdl file content as attachment back from server");
        System.out.println("start print the Client.java content:");
        for (int i = in.read(); i != -1; i = in.read()) {
            System.out.write(i);
        }
        System.out.println("finished print the mtom_xop.wsdl content.");

        InputStream pre = client.getClass().getResourceAsStream("me.bmp");
        long fileSize = 0;
        for (int i = pre.read(); i != -1; i = pre.read()) {
            fileSize++;
        }
        Holder<byte[]> param = new Holder<byte[]>();
        param.value = new byte[(int) fileSize];
        System.out.println("Start test the XML-binary Optimized Packaging!");
        System.out.println("Sending out the me.bmp Image content to server, data size is " + fileSize);

        in = client.getClass().getResourceAsStream("me.bmp");
        in.read(param.value);
        Holder<String> name = new Holder<String>("call detail");
        port.detail(name, param);
        System.out.println("received byte[] back from server, the size is " + param.value.length);

        Image image = ImageIO.read(new ByteArrayInputStream(param.value));
        System.out.println("build image with the returned byte[] back from server successfully, hashCode="
                + image.hashCode());

        System.exit(0);
    }

    private static InputStream getResourceStream(File file) throws Exception {
        InputStream in = new FileInputStream(file);
        return in;
    }
}
