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
package demo.jaxrs.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;
// import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@UriTemplate("/customers/")
public class CustomerService {
    @HttpContext UriInfo uriInfo;

    long currentId = 123;
    Map<Long, Customer> customers = new HashMap<Long, Customer>();

    public CustomerService() {
        Customer customer = createCustomer();
        customers.put(customer.getId(), customer);
    }

    @HttpMethod("GET")
    @UriTemplate("/customers/{id}/")
    Customer getCustomer(@UriParam("id") String id) {
        System.out.println("----invoking getCustomer, Customer id is: " + id);
        Customer c = customers.get(id);
        return c;
    }

    @HttpMethod("PUT")
    @UriTemplate("/customers/")
    public Response updateCustomer(Customer customer) {
        System.out.println("----invoking updateCustomer, Customer name is: " + customer.getName());
        Customer c = customers.get(customer.getId());
        Response r;
        if (c != null) {
            customers.put(customer.getId(), customer);
            r = Response.Builder.ok().build();
        } else {
            r = Response.Builder.notModified().build();
        }

        return r;
    }

    @HttpMethod("POST")
    @UriTemplate("/customers")
    public Response addCustomer(Customer customer) {
        System.out.println("----invoking addCustomer, customer name is: " + customer.getName());
        customer.setId(++currentId);

        customers.put(customer.getId(), customer);

        return Response.Builder.ok(customer).build();
    }

    @HttpMethod("DELETE")
    @UriTemplate("/customers/{id}/")
    public Response deleteCustomer(@UriParam("id") String id) {
        System.out.println("----invoking deleteCustomer with id: " + id);
        long idNumber = Long.parseLong(id);
        boolean found = false;
        Customer c = customers.get(idNumber);

        Response r;
        if (c != null) {
            r = Response.Builder.ok().build();
            customers.remove(idNumber);
        } else {
            r = Response.Builder.notModified().build();
        }

        return r;
    }

    final Customer createCustomer() {
        Customer c = new Customer();
        c.setName("John");
        c.setId(123);
        return c;
    }

}
