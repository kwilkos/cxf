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
package org.apache.cxf.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import junit.framework.TestCase;

import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;

public class AttachmentSerializerTest extends TestCase {
    public void testMessageWrite() throws Exception {
        MessageImpl msg = new MessageImpl();
        
        Collection<Attachment> atts = new ArrayList<Attachment>();
        AttachmentImpl a = new AttachmentImpl("test.xml");
        
        InputStream is = getClass().getResourceAsStream("my.wav");
        ByteArrayDataSource ds = new ByteArrayDataSource(is, "application/octet-stream");
        a.setDataHandler(new DataHandler(ds));
        
        atts.add(a);
        
        msg.setAttachments(atts);
        
        // Set the SOAP content type
        msg.put(Message.CONTENT_TYPE, "application/soap+xml");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.setContent(OutputStream.class, out);
        
        AttachmentSerializer serializer = new AttachmentSerializer(msg);
        
        serializer.writeProlog();
        
        String ct = (String) msg.get(Message.CONTENT_TYPE);
        assertTrue(ct.indexOf("multipart/related; boundary=") == 0);
        assertTrue(ct.indexOf("start=\"<root.message@cxf.apache.org>\"") > -1);
        assertTrue(ct.indexOf("start-info=\"application/soap+xml\"") > -1);
        
        out.write("<soap:Body/>".getBytes());
        
        serializer.writeAttachments();
        
        out.flush();
        
        MessageImpl in = new MessageImpl();
        in.put(Message.CONTENT_TYPE, ct);
        in.setContent(InputStream.class, new ByteArrayInputStream(out.toByteArray()));
        
        AttachmentDeserializer deserializer = new AttachmentDeserializer(in);
        deserializer.initializeAttachments();
        
        Collection<Attachment> inAtts = in.getAttachments();
        assertNotNull(inAtts);
        assertEquals(1, inAtts.size());
        
        Attachment inAtt = inAtts.iterator().next();
        assertEquals("test.xml", inAtt.getId());
    }
}
