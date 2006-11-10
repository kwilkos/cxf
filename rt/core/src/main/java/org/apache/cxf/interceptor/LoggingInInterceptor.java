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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;
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
        super();
        setPhase(Phase.RECEIVE);
    }

    public void handleMessage(Message message) throws Fault {
        InputStream is = message.getContent(InputStream.class);

        if (is == null) {
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(is, bos);

            is.close();
            bos.close();

            LOG.info("Message: " + bos.toString());

            message.setContent(InputStream.class, new ByteArrayInputStream(bos.toByteArray()));

        } catch (IOException e) {
            throw new Fault(e);
        }
    }
    
    public static void copy(final InputStream input,
                            final OutputStream output,
                            final int bufferSize)
        throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        n = input.read(buffer);
        while (-1 != n) {
            output.write(buffer, 0, n);
            n = input.read(buffer);
        }
    }
}
