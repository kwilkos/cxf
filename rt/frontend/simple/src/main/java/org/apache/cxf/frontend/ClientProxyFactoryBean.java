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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;

/**
 * This class will create a client for you which implements the specified
 * service class. Example:
 * <pre>
 * ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
 * factory.setServiceClass(YourServiceInterface.class);
 * YourServiceInterface client = (YourServiceInterface) factory.create();
 * </pre>
 * To access the underlying Client object:
 * <pre>
 * Client cxfClient = ClientProxy.getClient(client);
 * </pre>
 */
public class ClientProxyFactoryBean {
    private ClientFactoryBean clientFactoryBean;
    private String username;
    private String password;
    private Map<String, Object> properties;
    private Bus bus;
    
    public ClientProxyFactoryBean() {
        super();
        this.clientFactoryBean = new ClientFactoryBean();
    }

    public Object create() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        
        if (username != null) {
            AuthorizationPolicy authPolicy = new AuthorizationPolicy();
            authPolicy.setUserName(username);
            authPolicy.setPassword(password);
            properties.put(AuthorizationPolicy.class.getName(), authPolicy);
        }
        
        clientFactoryBean.setProperties(properties);
        
        if (bus != null) {
            clientFactoryBean.setBus(bus);
        }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Class getServiceClass() {
        return clientFactoryBean.getServiceClass();
    }

    public void setServiceClass(Class serviceClass) {
        clientFactoryBean.setServiceClass(serviceClass);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public URL getWsdlURL() {
        return clientFactoryBean.getServiceFactory().getWsdlURL();
    }

    public void setWsdlURL(URL wsdlURL) {
        clientFactoryBean.getServiceFactory().setWsdlURL(wsdlURL);
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
