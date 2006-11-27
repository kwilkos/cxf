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

package org.apache.cxf.binding.attachment;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;


public class AttachmentSerializer {

    private Message message;

    private InputStream in;

    private OutputStream out;

    public AttachmentSerializer(Message messageParam, InputStream inParam, OutputStream outParam) {
        message = messageParam;
        in = inParam;
        out = outParam;
    }

    /**
     * Using result in soapMessage & attachment to write to output stream
     * 
     * @param soapMessage
     * @param in
     *            input stream contain the attachment
     * @param out
     * @throws CxfRioException
     */

    public String serializeMultipartMessage() {

        String soapPartId;
        String boundary = AttachmentUtil.getUniqueBoundaryValue(0);
        try {
            soapPartId = AttachmentUtil.createContentID(null);
        } catch (UnsupportedEncodingException e) {
            throw new Fault(e);
        }
        try {
            Map<String, List<String>> headers = CastUtils.cast((Map<?, ?>) message
                            .get(Message.PROTOCOL_HEADERS));
            if (headers == null) {
                // this is the case of server out (response)
                headers = new HashMap<String, List<String>>();
                message.put(Message.PROTOCOL_HEADERS, headers);
            }
            AttachmentUtil.setMimeRequestHeader(headers, message, soapPartId,
                            "soap message with attachments", boundary);
            //finish prepare header, call flush to flush headers and resetOut to get wire stream
            out.flush();
            
            String soapHeader = AttachmentUtil.getSoapPartHeader(message, soapPartId, "");
            out.write(("--" + boundary + "\r\n").getBytes());
            out.write(soapHeader.getBytes());                                   
            AbstractCachedOutputStream.copyStream(in, out, 64 * 1024);
            for (Attachment att : message.getAttachments()) {
                soapHeader = AttachmentUtil.getAttchmentPartHeader(att);
                out.write(("--" + boundary + "\r\n").getBytes());
                out.write(soapHeader.getBytes());                
                Object content = att.getDataHandler().getContent();
                if (content instanceof InputStream) {
                    InputStream insAtt = (InputStream) content;
                    AbstractCachedOutputStream.copyStream(insAtt, out, 64 * 1024);
                } else {                    
                    ObjectOutputStream oos = new ObjectOutputStream(out);
                    oos.writeObject(content);
                }
                out.write("\r\n".getBytes());
            }
            out.write(("--" + boundary + "--\r\n").getBytes());
            out.flush();            
            // build contentType string for return
            List<String> contentType = (List<String>) headers.get(HttpHeaderHelper.getHeaderKey(
                    HttpHeaderHelper.CONTENT_TYPE));
            StringBuffer sb = new StringBuffer(120);
            for (String s : contentType) {
                sb.append(s);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new Fault(e);
        }

    }
}
