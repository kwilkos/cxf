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

package org.apache.cxf.jaxws;

import javax.xml.ws.Provider;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.Invoker;

public class ProviderInvoker<T> implements Invoker {

    private Provider<T> provider;

    public ProviderInvoker(Provider<T> p) {
        super();
        this.provider = p;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Exchange exchange, Object o) {
        MessageContext ctx = new WrappedMessageContext(exchange.getInMessage());
        WebServiceContextImpl.setMessageContext(ctx);
 
        if (provider != null) {
            return (T)provider.invoke((T)o);
        } else {
            System.err.println("TODO: Should return fault instead of null");
            return null;
        }
    }
}
