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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;

public class MtomServerTest extends AbstractCXFTest {
    public void testMtomRequest() throws Exception {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceClass(EchoService.class);
        sf.setBus(getBus());
        String address = "http://localhost:9036/EchoService";
        sf.setAddress(address);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Message.MTOM_ENABLED, Boolean.TRUE);
        sf.setProperties(props);
        sf.create();

        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/wsdl/http");
        ei.setAddress(address);

        ConduitInitiatorManager conduitMgr = getBus().getExtension(ConduitInitiatorManager.class);
        ConduitInitiator conduitInit = conduitMgr.getConduitInitiator("http://schemas.xmlsoap.org/soap/http");
        Conduit conduit = conduitInit.getConduit(ei);

        TestMessageObserver obs = new TestMessageObserver();
        conduit.setMessageObserver(obs);

        Message m = new MessageImpl();
        String ct = "multipart/related; type=\"application/xop+xml\"; "
                    + "start=\"<soap.xml@xfire.codehaus.org>\"; "
                    + "start-info=\"text/xml; charset=utf-8\"; "
                    + "boundary=\"----=_Part_4_701508.1145579811786\"";

        m.put(Message.CONTENT_TYPE, ct);
        conduit.send(m);

        OutputStream os = m.getContent(OutputStream.class);
        InputStream is = getResourceAsStream("request");
        if (is == null) {
            throw new RuntimeException("Could not find resource " + "request");
        }

        IOUtils.copy(is, os);

        os.flush();
        is.close();
        os.close();

        byte[] res = obs.getResponseStream().toByteArray();
        MessageImpl resMsg = new MessageImpl();
        resMsg.setContent(InputStream.class, new ByteArrayInputStream(res));
        resMsg.put(Message.CONTENT_TYPE, obs.getResponseContentType());
        AttachmentDeserializer deserializer = new AttachmentDeserializer(resMsg);
        deserializer.initializeAttachments();

        Collection<Attachment> attachments = resMsg.getAttachments();
        assertNotNull(attachments);
        assertEquals(1, attachments.size());

        Attachment inAtt = attachments.iterator().next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(inAtt.getDataHandler().getInputStream(), out);
        out.close();
        assertEquals(37448, out.size());
    }

    @Override
    protected Bus createBus() throws BusException {
        return BusFactory.getDefaultBus();
    }

}
