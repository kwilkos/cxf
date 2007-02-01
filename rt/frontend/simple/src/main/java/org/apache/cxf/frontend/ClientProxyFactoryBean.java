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
package org.apache.cxf.frontend;

import java.lang.reflect.Proxy;

import org.apache.cxf.endpoint.Client;

public class ClientProxyFactoryBean {
    private ClientFactoryBean clientFactoryBean;

    public ClientProxyFactoryBean() {
        super();
        this.clientFactoryBean = new ClientFactoryBean();
    }

    public Object create() {
        Client c = clientFactoryBean.create();

        ClientProxy handler = new ClientProxy(c);

        Class cls = clientFactoryBean.getServiceClass();
        Object obj = Proxy.newProxyInstance(cls.getClassLoader(), new Class[] {cls}, handler);

        return obj;
    }

    public ClientFactoryBean getClientFactoryBean() {
        return clientFactoryBean;
    }

    public void setClientFactoryBean(ClientFactoryBean clientFactoryBean) {
        this.clientFactoryBean = clientFactoryBean;
    }
    
    
}
