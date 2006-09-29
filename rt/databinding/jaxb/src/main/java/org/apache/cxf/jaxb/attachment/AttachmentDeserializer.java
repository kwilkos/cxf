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
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import org.apache.cxf.binding.attachment.AttachmentDataSource;
import org.apache.cxf.binding.attachment.AttachmentImpl;
import org.apache.cxf.binding.attachment.CachedOutputStream;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class AttachmentDeserializer {

    public static final String ATTACHMENT_DIRECTORY = "attachment-directory";

    public static final String ATTACHMENT_MEMORY_THRESHOLD = "attachment-memory-threshold";

    public static final int THRESHHOLD = 1024 * 100;

    private PushbackInputStream stream;

    private String boundary;

    private String contentType;

    private List<CachedOutputStream> cache;

    private Message message;

    public AttachmentDeserializer(Message messageParam) {
        message = messageParam;
    }

    public boolean preprocessMessage() {
        InputStream input;
        Map httpHeaders;
        // processing message if its multi-part/form-related
        try {
            httpHeaders = (Map) message.get(Message.PROTOCOL_HEADERS);
            if (httpHeaders == null) {
                return false;
            } else {
                List ctList = (List) httpHeaders.get("Content-Type");
                if (ctList != null) {
                    for (int x = 0; x < ctList.size(); x++) {
                        if (x == 0) {
                            contentType = (String) ctList.get(x);
                        } else {
                            contentType += "; " + (String) ctList.get(x);
                        }
                    }
                }
                if (contentType == null) { 
                    return false;
                }
                input = message.getContent(InputStream.class);
                if (input == null) {
                    return false;
                }
            }
            //printStream(input);
            if (contentType.toLowerCase().indexOf("multipart/related") != -1) {
                cache = new ArrayList<CachedOutputStream>();
                int i = contentType.indexOf("boundary=\"");
                int end;
                int len;
                if (i == -1) {
                    i = contentType.indexOf("boundary=");
                    end = contentType.indexOf(";", i + 9);
                    if (end == -1) {
                        end = contentType.length();
                    }
                    len = 9;
                } else {
                    end = contentType.indexOf("\"", i + 10);
                    len = 10;
                }
                if (i == -1 || end == -1) {
                    throw new IOException("Invalid content type: missing boundary! " + contentType);
                }
                boundary = "--" + contentType.substring(i + len, end);
                stream = new PushbackInputStream(input, boundary.length());
                if (!readTillFirstBoundary(stream, boundary.getBytes())) {
                    throw new IOException("Couldn't find MIME boundary: " + boundary);
                }
                processSoapBody();
                return true;
            }
        } catch (IOException ioe) {
            message.setContent(Exception.class, ioe);
        } catch (MessagingException me) {
            message.setContent(Exception.class, me);
        }
        return false;
    }

    /**
     * release the resource
     */
    public void dispose() {
        if (cache != null) {
            for (CachedOutputStream cos : cache) {
                cos.dispose();
            }
        }
    }

    public void process() throws MessagingException, IOException {
        processSoapBody();
        processAttachments();
    }

    /**
     * construct the primary soap body part and attachments
     */
    public void processSoapBody() throws MessagingException, IOException {

        Attachment soapMimePart = readMimePart();
        message.setContent(Attachment.class, soapMimePart);        
        InputStream in = soapMimePart.getDataHandler().getInputStream();
        message.setContent(InputStream.class, in);
    }

    public void processAttachments() throws MessagingException, IOException {
        Collection<Attachment> attachments = message.getAttachments();
        Attachment att = readMimePart();
        while (att != null && att.getId() != null) {
            attachments.add(att);
            att = readMimePart();
        }
    }

    public Attachment getAttachment(String cid) throws MessagingException, IOException {
        Collection<Attachment> attachments = message.getAttachments();
        for (Attachment att : attachments) {
            if (att.getId().equals(cid)) {
                return att;
            }
        }
        Attachment att = readMimePart();
        while (att != null && att.getId() != null) {
            attachments.add(att);
            String convertId = cid.substring(0, 4).equals("cid:") ? cid.substring(4) : cid;
            if (att.getId().equals(convertId)) {
                return att;
            }
            att = readMimePart();
        }
        return null;
    }

    /**
     * Move the read pointer to the begining of the first part read till the end
     * of first boundary
     * 
     * @param pushbackInStream
     * @param boundary
     * @throws MessagingException
     */
    private static boolean readTillFirstBoundary(PushbackInputStream pbs, byte[] bp) throws IOException {

        // work around a bug in PushBackInputStream where the buffer isn't
        // initialized
        // and available always returns 0.
        int value = pbs.read();
        pbs.unread(value);
        while (value != -1) {
            value = pbs.read();
            if ((byte) value == bp[0]) {
                int boundaryIndex = 0;
                while (value != -1 && (boundaryIndex < bp.length) && ((byte) value == bp[boundaryIndex])) {

                    value = pbs.read();
                    if (value == -1) {
                        throw new IOException("Unexpected End while searching for first Mime Boundary");
                    }
                    boundaryIndex++;
                }
                if (boundaryIndex == bp.length) {
                    // boundary found
                    pbs.read();
                    return true;
                }
            }
        }
        return false;
    }

    private Attachment readMimePart() throws MessagingException, IOException {

        int v = stream.read();
        if (v == -1) {
            return null;
        }
        stream.unread(v);
        InternetHeaders headers;
        headers = new InternetHeaders(stream);
        MimeBodyPartInputStream partStream = new MimeBodyPartInputStream(stream, boundary.getBytes());
        final CachedOutputStream cos = new CachedOutputStream();
        cos.setThreshold(THRESHHOLD);
        AbstractCachedOutputStream.copyStream(partStream, cos, THRESHHOLD);
        cos.close();
        final String ct = headers.getHeader("Content-Type", null);
        cache.add(cos);
        DataSource source = new AttachmentDataSource(ct, cos);
        DataHandler dh = new DataHandler(source);
        String id = headers.getHeader("Content-ID", null);
        if (id != null && id.startsWith("<")) {
            id = id.substring(1, id.length() - 1);
        }
        AttachmentImpl att = new AttachmentImpl(id, dh);
        for (Enumeration<?> e = headers.getAllHeaders(); e.hasMoreElements();) {
            Header header = (Header) e.nextElement();
            if (header.getName().equalsIgnoreCase("Content-Transfer-Encoding")
                            && header.getValue().equalsIgnoreCase("binary")) {
                att.setXOP(true);
            }
            att.setHeader(header.getName(), header.getValue());
        }
        return att;
    }

    private class MimeBodyPartInputStream extends InputStream {

        PushbackInputStream inStream;

        boolean boundaryFound;

        byte[] boundary;

        public MimeBodyPartInputStream(PushbackInputStream inStreamParam, byte[] boundaryParam) {
            super();
            this.inStream = inStreamParam;
            this.boundary = boundaryParam;
        }

        public int read() throws IOException {
            if (boundaryFound) {
                return -1;
            }

            // read the next value from stream
            int value = inStream.read();
            // A problem occured because all the mime parts tends to have a /r/n
            // at the end. Making it hard to transform them to correct
            // DataSources.
            // This logic introduced to handle it
            if (value == 13) {
                value = inStream.read();
                if (value != 10) {
                    inStream.unread(value);
                    return 13;
                } else {
                    value = inStream.read();
                    if ((byte) value != boundary[0]) {
                        inStream.unread(value);
                        inStream.unread(10);
                        return 13;
                    }
                }
            } else if ((byte) value != boundary[0]) {
                return value;
            }
            // read value is the first byte of the boundary. Start matching the
            // next characters to find a boundary
            int boundaryIndex = 0;
            while ((boundaryIndex < boundary.length) && ((byte) value == boundary[boundaryIndex])) {
                value = inStream.read();
                boundaryIndex++;
            }
            if (boundaryIndex == boundary.length) {
                // boundary found
                boundaryFound = true;
                // read the end of line character
                if (inStream.read() == 45 && value == 45) {
                    // Last mime boundary should have a succeeding "--"
                    // as we are on it, read the terminating CRLF
                    inStream.read();
                    inStream.read();
                }
                return -1;
            }
            // Boundary not found. Restoring bytes skipped.
            // write first skipped byte, push back the rest
            if (value != -1) {
                // Stream might have ended
                inStream.unread(value);
            }
            inStream.unread(boundary, 1, boundaryIndex - 1);
            return boundary[0];
        }
    }

    protected static void printStream(InputStream in) throws IOException {
        for (int i = in.read(); i != -1; i = in.read()) {
            System.out.write(i);
        }
        System.out.println("print stream");
    }
}
