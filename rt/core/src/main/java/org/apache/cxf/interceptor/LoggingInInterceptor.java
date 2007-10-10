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
package org.apache.cxf.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * A simple logging handler which outputs the bytes of the message to the
 * Logger.
 */
public class LoggingInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(LoggingInInterceptor.class);

    public LoggingInInterceptor() {
        super(Phase.RECEIVE);
    }

    public void handleMessage(Message message) throws Fault {

        if (LOG.isLoggable(Level.INFO)) {
            StringBuilder buffer = new StringBuilder(2048);
            
            buffer.append("Inbound Message\n")
                .append("--------------------------------------");
            
            String encoding = (String)message.get(Message.ENCODING);
            if (encoding != null) {
                buffer.append("\nEncoding: " + encoding);
            }
            Object headers = message.get(Message.PROTOCOL_HEADERS);
            if (headers != null) {
                buffer.append("\nHeaders: " + headers);
            }
            
            InputStream is = message.getContent(InputStream.class);
            if (is != null) {
                CachedOutputStream bos = new CachedOutputStream();
                try {
                    IOUtils.copy(is, bos);

                    bos.flush();
                    is.close();

                    message.setContent(InputStream.class, bos.getInputStream());
                    if (bos.getTempFile() != null) {
                        //large thing on disk...
                        buffer.append("\nMessage (saved to tmp file):\n");
                        buffer.append("Filename: " + bos.getTempFile().getAbsolutePath() + "\n");
                    } else {            
                        buffer.append("\nMessage:\n");
                    }
                    bos.writeCacheTo(buffer);
                    bos.close();
                } catch (IOException e) {
                    throw new Fault(e);
                }
            }
            buffer.append("\n--------------------------------------");
            LOG.info(buffer.toString());
        }
    }

}
