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

package org.apache.cxf.jaxb.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.binding.attachment.AttachmentUtil;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.interceptor.Fault;
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
            PrintWriter pw = new PrintWriter(out);
            Map<String, List<String>> headers = (Map<String, List<String>>) message
                            .get(Message.PROTOCOL_HEADERS);
            if (headers == null) {
                // this is the case of server out (response)
                headers = new HashMap<String, List<String>>();
                message.put(Message.PROTOCOL_HEADERS, headers);
            }
            AttachmentUtil.setMimeRequestHeader(headers, message, soapPartId,
                            "soap message with attachments", boundary);

            String soapHeader = AttachmentUtil.getSoapPartHeader(message, soapPartId, "");
            pw.println("--" + boundary);
            pw.println(soapHeader);
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
            for (String s = lnr.readLine(); s != null; s = lnr.readLine()) {
                pw.println(s);
            }
            pw.println();
            for (Attachment att : message.getAttachments()) {
                soapHeader = AttachmentUtil.getAttchmentPartHeader(att);
                pw.println("--" + boundary);
                pw.println(soapHeader);
                Object content = att.getDataHandler().getContent();
                if (content instanceof InputStream) {
                    InputStream ins = (InputStream) content;
                    if (!att.isXOP()) {
                        byte[] buffer = new byte[4096];
                        int pos = 0;
                        for (int len = ins.read(buffer); len != -1; len = ins.read(buffer)) {
                            Base64Utility.encode(buffer, pos, len, pw);
                            pos = pos + len;
                        }
                    } else {
                        for (int i = ins.read(); i != -1; i = ins.read()) {
                            pw.write(i);
                        }
                    }
                } else {
                    pw.flush();
                    ObjectOutputStream oos = new ObjectOutputStream(out);
                    oos.writeObject(content);
                }
                pw.println();
            }
            pw.println("--" + boundary);
            pw.flush();
            List<String> contentType = (List<String>) headers.get("Content-Type");
            StringBuffer sb = new StringBuffer(120);
            for (String s : contentType) {
                sb.append(s);
            }
            return sb.toString();
        } catch (IOException ioe) {
            throw new Fault(ioe);
        } catch (Base64Exception be) {
            throw new Fault(be);
        }

    }
}
