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

package org.apache.cxf.management.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.management.ManagementConstants;


public final class ManagementConsole {
    private static MBeanServerConnection mbsc;
    private static final String DEFAULT_JMXSERVICE_URL = 
        "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
    private static final Logger LOG = LogUtils.getL7dLogger(ManagementConsole.class);
    
    String jmxServerURL;
    String portName;
    String serviceName;
    String operationName;
    
    ManagementConsole() {
        
    } 
    
    public void getManagedObjectAttributes(ObjectName name) throws Exception {

        if (mbsc == null) {
            LOG.log(Level.SEVERE , "NO_MBEAN_SERVER");
            return;
        }
        MBeanInfo info = mbsc.getMBeanInfo(name);
        MBeanAttributeInfo[] attrs = info.getAttributes();
        if (attrs == null) {
            return;
        }
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isReadable()) {
                try {
                    Object o = mbsc.getAttribute(name, attrs[i].getName());
                    System.out.println("\t\t" + attrs[i].getName() + " = " + o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
  
    
    void connectToMBserver() throws IOException {
        jmxServerURL = jmxServerURL == null ? DEFAULT_JMXSERVICE_URL : jmxServerURL; 
        JMXServiceURL url = new JMXServiceURL(jmxServerURL);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        mbsc = jmxc.getMBeanServerConnection();
    }
    
    @SuppressWarnings("unchecked")
    void listAllManagedEndpoint() {        
        try {
            ObjectName queryEndpointName = new ObjectName(ManagementConstants.DEFAULT_DOMAIN_NAME 
                                                          + ":type=Bus.Service.Endpoint,*");
            Set<ObjectName> endpointNames = mbsc.queryNames(queryEndpointName, null);
            System.out.println("The endpoints are : ");
            for (ObjectName oName : endpointNames) {
                System.out.println(oName);
                getManagedObjectAttributes(oName);
            }        
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FAIL_TO_LIST_ENDPOINTS", new Object[]{e});
        }
    }
    
    ObjectName getEndpointObjectName() 
        throws MalformedObjectNameException, NullPointerException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(ManagementConstants.DEFAULT_DOMAIN_NAME + ":type=Bus.Service.Endpoint,");
        buffer.append(ManagementConstants.SERVICE_NAME_PROP + "='" + serviceName + "',");
        buffer.append(ManagementConstants.PORT_NAME_PROP + "='" + portName + "',*");
        return new ObjectName(buffer.toString());
    }
    
    @SuppressWarnings("unchecked")
    private void invokeEndpoint(String operation) {
        ObjectName endpointName = null;
        ObjectName queryEndpointName;
        try {
            queryEndpointName = getEndpointObjectName();
            Set<ObjectName> endpointNames = mbsc.queryNames(queryEndpointName, null);
            // now get the ObjectName with the busId 
            Iterator it = endpointNames.iterator();
        
            if (it.hasNext()) {
                // only deal with the first endpoint object which retrun from the list.
                endpointName = (ObjectName)it.next();
                mbsc.invoke(endpointName, operation, new Object[0], new String[0]);
            }
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FAIL_TO_LIST_ENDPOINTS", new Object[]{endpointName, operation, e}); 
        } 
    }
    
    void startEndpoint() {
        invokeEndpoint("start");        
    }
    
    void stopEndpoint() {
        invokeEndpoint("stop");
    }
    
    void restartEndpoint() {
        invokeEndpoint("stop");
        invokeEndpoint("start");
    }
    
    
    void parserArguments(String[] args) {
        portName = "";
        serviceName = "";
        jmxServerURL = "";
        
        int i;
        String arg;
        for (i = 0; i < args.length; i++) {
            arg = args[i];
            if ("-port".equals(arg)) {
                portName = args[++i];
                continue;
            }
            if ("-service".equals(arg)) {
                serviceName = args[++i];
                continue;
            }
            if ("-jmx".equals(arg)) {
                jmxServerURL = args[++i];
                continue;
            }
            if ("-operation".equals(arg)) {
                operationName = args[++i];
                continue;
            }
        }    
    }
    
    public void doManagement() {
        try {
            connectToMBserver();
            if ("listall".equalsIgnoreCase(operationName)) {
                listAllManagedEndpoint();
            }
            if ("start".equalsIgnoreCase(operationName)) {
                startEndpoint();
            }
            if ("stop".equalsIgnoreCase(operationName)) {
                stopEndpoint();
            }
            if ("restart".equalsIgnoreCase(operationName)) {
                restartEndpoint();
            }            
        } catch (IOException e) {            
            LOG.log(Level.SEVERE, "FAIL_TO_CONNECT_TO_MBEAN_SERVER", new Object[]{jmxServerURL}); 
        } 
        
    }
  
    /**
     * @param args
     */
    public static void main(String[] args) {
        ManagementConsole mc = new ManagementConsole();
        mc.parserArguments(args);
        mc.doManagement();
    }
   

}
