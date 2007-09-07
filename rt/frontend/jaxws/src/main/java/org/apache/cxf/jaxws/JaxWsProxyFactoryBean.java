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

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;

public class JaxWsProxyFactoryBean extends ClientProxyFactoryBean {
    public JaxWsProxyFactoryBean() {
        super();
        setClientFactoryBean(new JaxWsClientFactoryBean());
    }

    @Override
    protected ClientProxy clientClientProxy(Client c) {
        return new JaxWsClientProxy(c, ((JaxWsEndpointImpl)  c.getEndpoint()).getJaxwsBinding());
    }

    protected Class[] getImplementingClasses() {
        Class cls = getClientFactoryBean().getServiceClass();
        return new Class[] {cls, BindingProvider.class};
    }
}