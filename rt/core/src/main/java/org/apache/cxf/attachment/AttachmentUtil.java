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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.UUID;

import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.message.Attachment;

public final class AttachmentUtil {
    private static volatile int counter;
    private static final String ATT_UUID = UUID.randomUUID().toString();
    
    private static final Random BOUND_RANDOM = new Random();
    
    private AttachmentUtil() {

    }

    /**
     * @param ns
     * @return
     */
    public static String createContentID(String ns) throws UnsupportedEncodingException {
        // tend to change
        String cid = "http://cxf.apache.org/";
        
        String name = ATT_UUID + "-" + String.valueOf(++counter);
        if (ns != null && (ns.length() > 0)) {
            try {
                URI uri = new URI(ns);
                String host = uri.toURL().getHost();
                cid = host;
            } catch (URISyntaxException e) {
                cid = ns;
            } catch (MalformedURLException e) {
                cid = ns;
            }
        }
        return URLEncoder.encode(name, "UTF-8") + "@" + URLEncoder.encode(cid, "UTF-8");
    }

    public static String getUniqueBoundaryValue() {
        //generate a random UUID.
        //we don't need the cryptographically secure random uuid that
        //UUID.randomUUID() will produce.  Thus, use a faster
        //pseudo-random thing
        long leastSigBits = 0;
        long mostSigBits = 0;
        synchronized (BOUND_RANDOM) {
            mostSigBits = BOUND_RANDOM.nextLong();
            leastSigBits = BOUND_RANDOM.nextLong();
        }
        
        mostSigBits &= 0xFFFFFFFFFFFF0FFFL;  //clear version
        mostSigBits |= 0x0000000000004000L;  //set version

        leastSigBits &= 0x3FFFFFFFFFFFFFFFL; //clear the variant
        leastSigBits |= 0x8000000000000000L; //set to IETF variant
        
        UUID result = new UUID(mostSigBits, leastSigBits);
        
        return "uuid:" + result.toString();
    }

    public static String getAttchmentPartHeader(Attachment att) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append(HttpHeaderHelper.getHeaderKey(HttpHeaderHelper.CONTENT_TYPE) + ": "
                + att.getDataHandler().getContentType() + ";\r\n");
        if (att.isXOP()) {
            buffer.append("Content-Transfer-Encoding: binary\r\n");
        }
        String id = att.getId();
        if (id.charAt(0) == '<') {
            id = id.substring(1, id.length() - 1);
        }
        buffer.append("Content-ID: <" + id + ">\r\n\r\n");
        return buffer.toString();
    }

}
