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
package org.apache.cxf.connector;

import java.net.URL;
import javax.resource.ResourceException;
import javax.xml.namespace.QName;

/**
 * Provides methods to create a {@link Connection} object that represents a Web
 * service defined from the supplied parameters. This interface is returned from
 * an environment naming context lookup by a J2EE component.
 */

public interface CXFConnectionFactory {

    /**
     * Creates a client proxy based on the given WSDL information.
     * 
     * @param iface The interface class implemented by the returned proxy.
     * @param wsdlLocation The URL to the WSDL that defines the service.
     * @param serviceName The QName that identifies the service.
     * @param portName The port to connect to; services may include multiple
     *            ports.
     * @return a proxy object that implements both the given <code>iface</code>
     *         and the {@link Connection} interface. It represents the Web
     *         service associated with the specified service and port.
     * @throws ResourceException If there is an error creating the connection.
     */
    Object getConnection(Class iface, URL wsdlLocation, QName serviceName, QName portName)
        throws ResourceException;

    /**
     * Creates a client proxy based on the given WSDL information. If the
     * service contains more than one port the first one will be used.
     * 
     * @param iface The interface class implemented by the returned proxy.
     * @param wsdlLocation The URL to the WSDL that defines the service.
     * @param serviceName The QName that identifies the service.
     * @return A proxy object that implements both the given <code>iface</code>
     *         and the {@link Connection} interface. It represents the Web
     *         service associated with the specified service.
     * @throws ResourceException If there is an error creating the connection.
     */
    Object getConnection(Class iface, URL wsdlLocation, QName serviceName) throws ResourceException;

    /**
     * Creates a client proxy based on the given WSDL information. The WSDL
     * location will be obtained from Bus configuration using the
     * <code>serviceName</code>.
     * 
     * @param iface The interface class implemented by the returned proxy.
     * @param serviceName The QName that identifies the service.
     * @param portName The port to connect to; services may include multiple
     *            ports.
     * @return A proxy object that implements both the given <code>iface</code>
     *         and the {@link Connection} interface. It represents the Web
     *         service associated with the specified service and port.
     * @throws ResourceException If there is an error creating the connection.
     */
    Object getConnection(Class iface, QName serviceName, QName portName) throws ResourceException;

    /**
     * Creates a client proxy based on the given WSDL information. If the
     * service contains more than one port the first one will be used as no port
     * name is passed. The WSDL location will be obtained from Bus configuration
     * using the <code>serviceName</code>.
     * 
     * @param iface The interface class implemented by the returned proxy.
     * @param serviceName The QName that identifies the service..
     * @return A proxy object that implements both the given <code>iface</code>
     *         and the {@link Connection} interface. It represents the Web
     *         service associated with the specified service.
     * @throws ResourceException If there is an error creating the connection.
     */
    Object getConnection(Class iface, QName serviceName) throws ResourceException;

    /**
     * Returns the underlying {@link Bus} for this connection factory. In some
     * J2EE environments, for example Weblogic, the {@link Bus} and dependent
     * classes are not available to the J2EE application. In this case the CXF
     * runtime jar:
     * <code>cxf-install-dir/cxf/lib/cxf-rt-version.jar</code> should
     * be added to the classpath of the application server. Once, the
     * {@link Bus} class is available on the system classpath, then the returned
     * object may be cast to {@link Bus}. In other environments, this cast
     * should be safe without having to modify the classpath <code>
     *    org.apache.cxf.Bus = (org.apache.cxf.Bus)connectionFactory.getBus();
     * </code>
     * 
     * @return the connection factory&amp;s {@link Bus}
     */
    Object getBus();
}
