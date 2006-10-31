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

package org.apache.cxf.jaxws.handler.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.jaxws.handler.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class SOAPHandlerInterceptor extends
        AbstractProtocolHandlerInterceptor<SoapMessage> implements
        SoapInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils
            .getBundle(SOAPHandlerInterceptor.class);

    public SOAPHandlerInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.PRE_PROTOCOL);
        addBefore((new StaxOutInterceptor()).getId());
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }

    @SuppressWarnings("unchecked")
    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();
        for (Handler h : getBinding().getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                Set<QName> headers = ((SOAPHandler) h).getHeaders();
                if (headers != null) {
                    understood.addAll(headers);
                }
            }
        }
        return understood;
    }

    public void handleMessage(SoapMessage message) {
        if (getInvoker(message).isOutbound()) {
            OutputStream os = message.getContent(OutputStream.class);
            CachedStream cs = new CachedStream();
            message.setContent(OutputStream.class, cs);

            if (message.getInterceptorChain().doInterceptInSubChain(message) 
                && message.getContent(Exception.class) != null) {
                throw new Fault(message.getContent(Exception.class));
            }

            super.handleMessage(message);

            // TODO: Stream SOAPMessage to output stream if SOAPMessage has been
            // changed

            try {
                cs.flush();
                AbstractCachedOutputStream.copyStream(cs.getInputStream(), os,
                        64 * 1024);
                cs.close();
                os.flush();
                message.setContent(OutputStream.class, os);
            } catch (IOException ioe) {
                throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                        "SOAPHANDLERINTERCEPTOR_OUTBOUND_IO", BUNDLE), ioe,
                        message.getVersion().getSender());
            }
        } else {
            super.handleMessage(message);           
        }
    }

    @Override
    protected MessageContext createProtocolMessageContext(Message message) {
        return new SOAPMessageContextImpl(message);
    }

    public void handleFault(SoapMessage message) {
    }

    private class CachedStream extends AbstractCachedOutputStream {
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
        }

        protected void onWrite() throws IOException {
        }
    }

}
