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
package demo.restful.client;

import org.apache.cxf.binding.http.HttpBindingFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import demo.restful.server.CustomerService;
import demo.restful.server.Customers;
import demo.restful.server.Customer;

public final class ClientMain {
    private ClientMain() { }

    public static void main(String[] args) throws Exception {
        JaxWsProxyFactoryBean sf = new JaxWsProxyFactoryBean();
        sf.setServiceClass(CustomerService.class);

        // Turn off wrapped mode to make our xml prettier
        sf.getServiceFactory().setWrapped(false);

        // Use the HTTP Binding which understands the Java Rest Annotations
        sf.getClientFactoryBean().setBindingId(HttpBindingFactory.HTTP_BINDING_ID);
        sf.setAddress("http://localhost:8080/xml/");
        CustomerService cs = (CustomerService)sf.create();

        Customers customers = cs.getCustomers();
        for (Customer c : customers.getCustomer()) {
            System.out.println("Found customer " + c.getName());
        }
    }

}
