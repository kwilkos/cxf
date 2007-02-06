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

package org.apache.cxf.jaxws;

import java.net.URL;

import javax.xml.ws.Provider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.endpoint.EndpointPublisher;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;

public class EndpointPublisherImpl implements EndpointPublisher {
    private EndpointImpl ep;

    @SuppressWarnings("unchecked")
    public void buildEndpoint(Bus bus,
                              String implName,
                              String serviceName,
                              URL wsdl,
                              String portName) 
        throws BusException {
        try {
            Class cls = ClassLoaderUtils.loadClass(implName, getClass());
            Object impl = cls.newInstance();
    
            JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(cls);
            JaxWsServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean();
            serviceFactory.setBus(bus);
            if (implInfo.isWebServiceProvider()) {
                serviceFactory.setInvoker(new ProviderInvoker((Provider<?>)impl));
            } else {
                serviceFactory.setInvoker(new JAXWSMethodInvoker(impl));
            }
            serviceFactory.setServiceClass(impl.getClass());
            
            if (null != wsdl) {
                serviceFactory.setWsdlURL(wsdl);
            }
            
            ep = new EndpointImpl(bus, impl, serviceFactory);        
                
        } catch (ClassNotFoundException ex) {
            throw new BusException(ex);
        } catch (InstantiationException ex) {
            throw new BusException(ex);
        } catch (IllegalAccessException ex) {
            throw new BusException(ex);
        }
        
    }

    public void publish(String address) throws BusException {        
        ep.publish(address);        
    }

}
