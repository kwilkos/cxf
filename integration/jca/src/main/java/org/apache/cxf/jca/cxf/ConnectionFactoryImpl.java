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


import java.io.Serializable;
import java.net.URL;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.namespace.QName;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.connector.CXFConnectionFactory;
import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;

public class ConnectionFactoryImpl implements CXFConnectionFactory, 
                                              Referenceable, 
                                              Serializable {
    private static final Logger LOG = LogUtils.getL7dLogger(ConnectionFactoryImpl.class);
    private ManagedConnectionFactory managedConnectionFactory;
    private ConnectionManager connectionManager;
    private Reference reference;

    public ConnectionFactoryImpl(ManagedConnectionFactory aMCF, ConnectionManager aCM) {
        managedConnectionFactory = aMCF;
        connectionManager = aCM;
        LOG.info("this=" + this);
    }

    public void setReference(Reference ref) {
        LOG.info("Reference : " + ref + " is set");
        reference = ref;
    }

    public Reference getReference() throws NamingException {
        LOG.info("Reference : " + reference + " is returned");
        return reference;
    }
    
    public Object getConnection(Class iface, URL wsdlLocation, QName serviceName) throws ResourceException {
        return getConnection(iface, wsdlLocation, serviceName, null);
    }

    public Object getConnection(Class iface, QName serviceName, QName portName) throws ResourceException {
        return getConnection(iface, null, serviceName, portName);
    }

    public Object getConnection(Class iface, QName serviceName) throws ResourceException {
        return getConnection(iface, null, serviceName, null);
    }
  

    public Object getBus() { 
        return ((ManagedConnectionFactoryImpl)managedConnectionFactory).getBus();
    }

    public Object getConnection(Class iface, URL wsdlLocation, QName serviceName, QName portName)
        throws ResourceException {
        
        if (!iface.isInterface()) {
            throw new ResourceAdapterInternalException(
                    "The first argument to getConnection must be an Interface",
                    new IllegalArgumentException(iface.toString() + " is not an Interface."));
        }

        LOG.info("connecting to: " + iface);

        CXFConnectionRequestInfo reqInfo = 
            new CXFConnectionRequestInfo(iface, wsdlLocation, serviceName, portName);

        if (connectionManager == null) {
            // non-managed, null Subject
            ManagedConnection connection = managedConnectionFactory.createManagedConnection(null, reqInfo);
            return connection.getConnection(null, reqInfo);
        } else {
            return connectionManager.allocateConnection(managedConnectionFactory, reqInfo);
        }
    }
}

















