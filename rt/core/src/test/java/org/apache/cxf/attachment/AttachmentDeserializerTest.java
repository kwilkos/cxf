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
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;

public class AttachmentDeserializerTest extends TestCase {
    public void testDeserializerMtom() throws Exception {
        InputStream is = getClass().getResourceAsStream("mimedata");
        String ct = "multipart/related; type=\"application/xop+xml\"; "
                    + "start=\"<soap.xml@xfire.codehaus.org>\"; "
                    + "start-info=\"text/xml; charset=utf-8\"; "
                    + "boundary=\"----=_Part_4_701508.1145579811786\"";
        
        MessageImpl msg = new MessageImpl();
        msg.put(Message.CONTENT_TYPE, ct);
        msg.setContent(InputStream.class, is);
        
        AttachmentDeserializer deserializer = new AttachmentDeserializer(msg);
        deserializer.initializeAttachments();
        
        InputStream attBody = msg.getContent(InputStream.class);
        assertTrue(attBody != is);
        assertTrue(attBody instanceof DelegatingInputStream);
        
        Collection<Attachment> atts = msg.getAttachments();
        assertNotNull(atts);
        
        Iterator<Attachment> itr = atts.iterator();
        assertTrue(itr.hasNext());
        
        Attachment a = itr.next();
        assertNotNull(a);
        
        InputStream attIs = a.getDataHandler().getInputStream();
        
        assertTrue(((DelegatingInputStream) attIs).getInputStream() instanceof MimeBodyPartInputStream);
        assertTrue(((DelegatingInputStream) attBody).getInputStream() instanceof ByteArrayInputStream);
        
        // check the cached output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(attBody, out);
        assertTrue(out.toString().startsWith("<env:Envelope"));
        
        // try streaming a character off the wire
        assertTrue(attIs.read() == '/');
        assertTrue(attIs.read() == '9');
        
//        Attachment invalid = atts.get("INVALID");
//        assertNull(invalid.getDataHandler().getInputStream());
//        
//        assertTrue(attIs instanceof ByteArrayInputStream);
    }
    
    public void testDeserializerSwA() throws Exception {
        InputStream is = getClass().getResourceAsStream("swadata");
        String ct = "multipart/related; type=\"text/xml\"; "
            + "start=\"<86048FF3556694F7DA1918466DDF8143>\";    "
            + "boundary=\"----=_Part_0_14158819.1167275505862\"";
        
        MessageImpl msg = new MessageImpl();
        msg.put(Message.CONTENT_TYPE, ct);
        msg.setContent(InputStream.class, is);
        
        AttachmentDeserializer deserializer = new AttachmentDeserializer(msg);
        deserializer.initializeAttachments();
        
        InputStream attBody = msg.getContent(InputStream.class);
        assertTrue(attBody != is);
        assertTrue(attBody instanceof DelegatingInputStream);
        
        Collection<Attachment> atts = msg.getAttachments();
        assertNotNull(atts);
        
        Iterator<Attachment> itr = atts.iterator();
        assertTrue(itr.hasNext());
        
        Attachment a = itr.next();
        assertNotNull(a);
        
        InputStream attIs = a.getDataHandler().getInputStream();
        
        assertTrue(((DelegatingInputStream) attIs).getInputStream() instanceof MimeBodyPartInputStream);
        assertTrue(((DelegatingInputStream) attBody).getInputStream() instanceof ByteArrayInputStream);
        
        // check the cached output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(attBody, out);
        assertTrue(out.toString().startsWith("<?xml"));
        
        // try streaming a character off the wire
        assertTrue(attIs.read() == 'f');
        assertTrue(attIs.read() == 'o');
        assertTrue(attIs.read() == 'o');
        assertTrue(attIs.read() == 'b');
        assertTrue(attIs.read() == 'a');
        assertTrue(attIs.read() == 'r');
        assertTrue(attIs.read() == -1);
    }
}