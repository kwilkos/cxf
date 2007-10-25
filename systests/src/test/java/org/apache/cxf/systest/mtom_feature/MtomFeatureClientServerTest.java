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

package org.apache.cxf.systest.mtom_feature;

import java.awt.Image;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class MtomFeatureClientServerTest extends AbstractBusClientServerTestBase {

    private final QName serviceName = new QName("http://apache.org/cxf/systest/mtom_feature",
                                                "HelloService");
    private Hello port = getPort();

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    @Test
    public void testDetail() throws Exception {
        Holder<byte[]> photo = new Holder<byte[]>("CXF".getBytes());
        Holder<Image> image = new Holder<Image>(getImage("/java.jpg"));
        port.detail(photo, image);
        assertEquals("CXF", new String(photo.value));
        assertNotNull(image.value);
    }
    
    @Test
    public void testEcho() throws Exception {
        byte[] bytes = ImageHelper.getImageBytes(getImage("/java.jpg"), "image/jpeg");
        Holder<byte[]> image = new Holder<byte[]>(bytes);
        port.echoData(image);
        assertNotNull(image);
    }

    private Image getImage(String name) throws Exception {
        return ImageIO.read(getClass().getResource(name));
    }

    private Hello getPort() {
        URL wsdl = getClass().getResource("/wsdl/mtom.wsdl");
        assertNotNull("WSDL is null", wsdl);

        HelloService service = new HelloService(wsdl, serviceName);
        assertNotNull("Service is null ", service);
        //return service.getHelloPort();        
        return service.getHelloPort(new MTOMFeature());
    }
}
