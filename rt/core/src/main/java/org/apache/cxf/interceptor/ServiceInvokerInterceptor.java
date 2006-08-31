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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.Invoker;

/**
 * Invokes a Binding's invoker with the <code>INVOCATION_INPUT</code> from
 * the Exchange.
 * @author Dan Diephouse
 */
public class ServiceInvokerInterceptor extends AbstractPhaseInterceptor<Message> {
   
    
    public ServiceInvokerInterceptor() {
        super();
        setPhase(Phase.INVOKE);
    }

    public void handleMessage(final Message message) {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = exchange.get(Endpoint.class);
        final Service service = endpoint.getService();
        final Invoker invoker = service.getInvoker();        

        getExecutor(endpoint).execute(new Runnable() {

            public void run() {
                Object result = invoker.invoke(message.getExchange(), 
                    message.getContent(List.class));

                if (result != null) {
                    if (!(result instanceof List)) {
                        if (result.getClass().isArray()) {
                            result = Arrays.asList((Object[])result);
                        } else {
                            List<Object> o = new ArrayList<Object>();
                            o.add(result);
                            result = o;
                        }
                    }
                    exchange.getOutMessage().setContent(List.class, result);
                }
            }

        });
    }

    /**
     * Get the Executor for this invocation.
     * @param endpoint
     * @return
     */
    private Executor getExecutor(final Endpoint endpoint) {
        return endpoint.getService().getExecutor();
    }
}
