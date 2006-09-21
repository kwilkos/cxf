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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public final class AttachmentUtil {

    private AttachmentUtil() {

    }

    /**
     * @param ns
     * @return
     */
    public static String createContentID(String ns) throws UnsupportedEncodingException {
        // tend to change
        String cid = "cxf.apache.org";
        String name = UUID.randomUUID() + "@";
        if (ns != null && (ns.length() > 0)) {
            try {
                URI uri = new URI(ns);
                String host = uri.toURL().getHost();
                cid = host;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                cid = URLEncoder.encode(ns, "UTF-8");
            }
        }
        return name + cid;
    }

    public static String getUniqueBoundaryValue(int part) {
        StringBuffer s = new StringBuffer();
        // Unique string is ----=_Part_<part>_<hashcode>.<currentTime>
        s.append("----=_Part_").append(part++).append("_").append(s.hashCode()).append('.').append(
                        System.currentTimeMillis());
        return s.toString();
    }

    public static String getSoapPartHeader(Message message, String soapPartId, String action) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("Content-Type: application/xop+xml; charset=utf-8; ");
        buffer.append("type=\"" + message.getAttachmentMimeType());
        if (action != null) {
            buffer.append("; action=" + action + "\"\n");
        } else {
            buffer.append("\"\n");
        }
        buffer.append("Content-Transfer-Encoding: binary\n");        
        buffer.append("Content-ID: <" + soapPartId + ">\n");
        return buffer.toString();
    }

    public static String getAttchmentPartHeader(Attachment att) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("Content-Type: " + att.getDataHandler().getContentType() + ";\n");
        if (att.isXOP()) {
            buffer.append("Content-Transfer-Encoding: binary\n");
        }
        buffer.append("Content-ID: <" + att.getId() + ">\n");
        return buffer.toString();
    }

    public static void setMimeRequestHeader(Map<String, List<String>> reqHeaders, Message message,
                    String soapPartId, String contentDesc, String boundary) {
        List<String> header1 = new ArrayList<String>();
        header1.add("1.0");
        reqHeaders.put("MIME-Version", header1);
        List<String> header2 = new ArrayList<String>();
        header2.add("Multipart/" + getMimeSubType(message, soapPartId, boundary));
        reqHeaders.put("Content-Type", header2);
        List<String> header3 = new ArrayList<String>();
        header3.add(contentDesc);
        reqHeaders.put("Content-Description", header3);

    }

    public static String getMimeSubType(Message message, String soapPartId, String boundary) {
        StringBuffer ct = new StringBuffer();
        ct.append("related; boundary=" + boundary + "; ");
        ct.append("type=\"application/xop+xml\"; ");
        ct.append("start=\"<" + soapPartId + ">\"; ");
        ct.append("start-info=\"" + message.getAttachmentMimeType() + "\"");
        return ct.toString();
    }
}
