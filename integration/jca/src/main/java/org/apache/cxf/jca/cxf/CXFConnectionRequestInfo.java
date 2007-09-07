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
package org.apache.cxf.jca.cxf;


import java.net.URL;
import javax.resource.spi.ConnectionRequestInfo;
import javax.xml.namespace.QName;


public class CXFConnectionRequestInfo implements ConnectionRequestInfo {
    private Class iface;
    private URL wsdlLocation;
    private QName serviceName;
    private QName portName;

    public CXFConnectionRequestInfo(Class aIface, URL aWsdlLocation, 
                                       QName aServiceName, QName aPortName) {
        this.iface = aIface;
        this.wsdlLocation = aWsdlLocation;
        this.serviceName = aServiceName;
        this.portName = aPortName;
    }

    public Class<?> getInterface() {
        return iface;
    }

    public URL getWsdlLocationUrl() {
        return wsdlLocation;
    }

    public QName getServiceQName() {
        return serviceName;
    }

    public QName getPortQName() {
        return portName;
    }



    public boolean equals(java.lang.Object other) {
        boolean ret = this == other; 

        if (!ret && other instanceof CXFConnectionRequestInfo) {
            CXFConnectionRequestInfo cri = (CXFConnectionRequestInfo)other; 

            ret = areEquals(iface, cri.iface) && areEquals(wsdlLocation, cri.wsdlLocation)
                   && areEquals(serviceName, cri.serviceName) && areEquals(portName, cri.portName);
           
        }
        return ret;
    }
  
    public int hashCode() {
        return getInterface().hashCode() + (serviceName != null ? serviceName.hashCode() : 0);
    }  

    public String toString() {
        StringBuffer buf = new StringBuffer(256);

        buf.append(iface).append(":").append(portName).append(":").append(serviceName).append("@").append(
                wsdlLocation);

        return buf.toString();
    }


    private boolean areEquals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj1 == obj2; 
        } else {
            return obj1.equals(obj2);
        }            
    }
   
}
