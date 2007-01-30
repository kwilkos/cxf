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

package org.apache.cxf.systest.jaxws;

import javax.xml.ws.Endpoint;

import org.apache.cxf.anonymous_complex_type.AnonymousComplexTypeImpl;
import org.apache.cxf.jaxb_element_test.JaxbElementTestImpl;
import org.apache.cxf.ordered_param_holder.OrderedParamHolderImpl;
import org.apache.cxf.systest.common.TestServerBase;


public class ServerMisc extends TestServerBase {

    protected void run() {
        Object implementor1 = new AnonymousComplexTypeImpl();
        String address = "http://localhost:9000/anonymous_complex_typeSOAP";
        Endpoint.publish(address, implementor1);

        Object implementor2 = new JaxbElementTestImpl();
        address = "http://localhost:9001/jaxb_element_test";
        Endpoint.publish(address, implementor2);

        Object implementor3 = new OrderedParamHolderImpl();
        address = "http://localhost:9002/ordered_param_holder/";
        Endpoint.publish(address, implementor3);

    }

    public static void main(String[] args) {
        try {
            ServerMisc s = new ServerMisc();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
