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
package org.apache.cxf.systest.ws.rm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class InMessageRecorder extends AbstractPhaseInterceptor<Message> {

    private List<SOAPMessage> inbound;

    public InMessageRecorder() {
        inbound = new ArrayList<SOAPMessage>();
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
            MessageFactory mf = MessageFactory.newInstance();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            SOAPMessage sm = mf.createMessage(null, bis);
            inbound.add(sm);
            bis = new ByteArrayInputStream(bos.toByteArray());
            message.setContent(InputStream.class, bis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected List<SOAPMessage> getInboundMessages() {
        return inbound;
    } 
}
