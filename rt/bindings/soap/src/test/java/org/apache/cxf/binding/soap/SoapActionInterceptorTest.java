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

package org.apache.cxf.binding.soap;

import java.util.List;
import java.util.Map;

import org.apache.cxf.binding.soap.interceptor.SoapActionInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

public class SoapActionInterceptorTest extends Assert {

    
    @Test
    public void testSoapAction() throws Exception {
        Message message = new MessageImpl();
        message.setExchange(new ExchangeImpl());
        message.getExchange().setOutMessage(message);
        SoapBinding sb = new SoapBinding(null);
        message = sb.createMessage(message);
        assertNotNull(message);
        assertTrue(message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals(Soap11.getInstance(), soapMessage.getVersion());
        (new SoapActionInterceptor()).handleMessage(soapMessage);
        Map<String, List<String>> reqHeaders = CastUtils.cast((Map)soapMessage.get(Message.PROTOCOL_HEADERS));
        assertNotNull(reqHeaders);
        assertEquals("\"\"", reqHeaders.get("SOAPAction").get(0));

        sb.setSoapVersion(Soap12.getInstance());
        soapMessage = (SoapMessage) sb.createMessage(soapMessage);
        (new SoapActionInterceptor()).handleMessage(soapMessage);
        reqHeaders = CastUtils.cast((Map)message.get(Message.PROTOCOL_HEADERS));
        assertNotNull(reqHeaders);
        assertEquals("\"\"", reqHeaders.get("action").get(0));
    }

}
