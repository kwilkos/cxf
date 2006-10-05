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

package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

public class EndpointInfo extends AbstractPropertiesHolder {
    final String transportId;
    ServiceInfo service;
    BindingInfo binding;
    QName name;
    String address;
    
    public EndpointInfo(ServiceInfo serv, String ns) {
        transportId = ns;
        service = serv;
    }
    public String getTransportId() {
        return transportId;
    }    
    public InterfaceInfo getInterface() {
        return service.getInterface();
    }
    public ServiceInfo getService() {
        return service;
    }
    
    public QName getName() {
        return name;
    }
    public void setName(QName n) {
        name = n;
    }

    public BindingInfo getBinding() {
        return binding;
    }
    public void setBinding(BindingInfo b) {
        binding = b;
    }    
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String a) {
        address = a;
    }
}
