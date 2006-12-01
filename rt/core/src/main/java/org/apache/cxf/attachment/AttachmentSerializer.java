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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;


public class AttachmentSerializer {

    private static final String BODY_ATTACHMENT_ID = "root.message@cxf.apache.org";
    private Message message;
    private String bodyBoundary;
    private OutputStreamWriter writer;
    private OutputStream out;
    
    public AttachmentSerializer(Message messageParam) {
        message = messageParam;
    }

    /**
     * Serialize the beginning of the attachment which includes the MIME 
     * beginning and headers for the root message.
     */
    public void writeProlog() throws IOException {
        // Create boundary for body
        bodyBoundary = AttachmentUtil.getUniqueBoundaryValue(0);
        
        String bodyCt = (String) message.get(Message.CONTENT_TYPE);
        String enc = (String) message.get(Message.ENCODING);
        if (enc == null) {
            enc = "UTF-8";
        }
        
        // Set transport mime type
        StringBuilder ct = new StringBuilder();
        ct.append("multipart/related; ")
            .append("type=\"application/xop+xml\"; ")
            .append("boundary=\"")
            .append(bodyBoundary)
            .append("\"; ")
            .append("start=\"<")
            .append(BODY_ATTACHMENT_ID)
            .append(">\"; ")
            .append("start-info=\"")
            .append(bodyCt)
            .append("; charset=")
            .append(enc)
            .append("\"");
        
        message.put(Message.CONTENT_TYPE, ct.toString());

        // 2. write headers
        out = message.getContent(OutputStream.class);
        String encoding = (String) message.get(Message.ENCODING);
        if (encoding == null) {
            encoding = "UTF-8";
        }
        writer = new OutputStreamWriter(out, encoding);
        writer.write("\r\n");
        writer.write("--");
        writer.write(bodyBoundary);
        
        StringBuilder mimeBodyCt = new StringBuilder();
        mimeBodyCt.append("application/xop+xml; charset=")
            .append(enc)
            .append("; type=\"")
            .append(bodyCt)
            .append("; charset=")
            .append(enc)
            .append("\"");
        
        writeHeaders(mimeBodyCt.toString(), BODY_ATTACHMENT_ID);
    }

    private void writeHeaders(String contentType, String attachmentId) throws IOException {
        writer.write("\r\n");
        writer.write("Content-Type: ");
        writer.write(contentType);
        writer.write("\r\n");

        writer.write("Content-Transfer-Encoding: binary\r\n");

        writer.write("Content-ID: <");
        writer.write(attachmentId);
        writer.write(">\r\n\r\n");
        writer.flush();
    }

    /**
     * Write the end of the body boundary and any attachments included.
     * @throws IOException
     */
    public void writeAttachments() throws IOException {
        for (Attachment a : message.getAttachments()) {
            
            writer.write("\r\n");
            writer.write("--");
            writer.write(bodyBoundary);

            writeHeaders(a.getDataHandler().getContentType(), a.getId());
            
            IOUtils.copy(a.getDataHandler().getInputStream(), out);
        }
        
        writer.write("\r\n");
        writer.write("--");
        writer.write(bodyBoundary);
        writer.write("--");
        
        writer.flush();
    }
}
