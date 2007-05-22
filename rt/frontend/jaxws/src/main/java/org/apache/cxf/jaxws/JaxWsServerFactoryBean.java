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

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.binding.soap.JaxWsSoapBindingConfiguration;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingInfo;

/**
 * Bean to help easily create Server endpoints for JAX-WS. Example:
 * <pre>
 * JaxWsServerFactoryBean sf = JaxWsServerFactoryBean();
 * sf.setServiceClass(MyService.class);
 * sf.setAddress("http://acme.com/myService");
 * sf.create();
 * </pre>
 * This will start a server for you and register it with the ServerManager. 
 */
public class JaxWsServerFactoryBean extends ServerFactoryBean {
    public JaxWsServerFactoryBean() {
        this(new JaxWsServiceFactoryBean());
    }
    public JaxWsServerFactoryBean(JaxWsServiceFactoryBean serviceFactory) {
        setServiceFactory(serviceFactory);
        JaxWsSoapBindingConfiguration defConfig 
            = new JaxWsSoapBindingConfiguration(serviceFactory);
        setBindingConfig(defConfig);
    }

    @Override
    protected Invoker createInvoker() {
        return new JAXWSMethodInvoker(getServiceBean());
    }

    @Override
    protected BindingInfo createBindingInfo() {
        JaxWsServiceFactoryBean sf = (JaxWsServiceFactoryBean)getServiceFactory(); 
        
        JaxWsImplementorInfo implInfo = sf.getJaxWsImplementorInfo();
        String jaxBid = implInfo.getBindingType();
        String binding = getBindingId();
        if (binding == null) {
            binding = jaxBid;
            setBindingId(binding);
        }
        
        if (binding.equals(SOAPBinding.SOAP11HTTP_BINDING) 
            || binding.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING)
            //|| binding.equals(SOAPBinding.SOAP12HTTP_BINDING) 
            || binding.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            binding = "http://schemas.xmlsoap.org/soap/";
            setBindingId(binding);
            if (getBindingConfig() == null) {
                setBindingConfig(new JaxWsSoapBindingConfiguration(sf));
            }
        }
            
        boolean messageMode = implInfo.getServiceMode().equals(javax.xml.ws.Service.Mode.MESSAGE);
        
        if (getBindingConfig() instanceof JaxWsSoapBindingConfiguration) {
            JaxWsSoapBindingConfiguration conf = (JaxWsSoapBindingConfiguration)getBindingConfig();
            
            if (jaxBid.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
                conf.setVersion(Soap12.getInstance());
            }
            
            if (jaxBid.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                conf.setVersion(Soap12.getInstance());
                conf.setMtomEnabled(true);
            }
            if (jaxBid.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
                conf.setMtomEnabled(true);
            }
            
            conf.setJaxWsServiceFactoryBean(sf);
        }
        
        BindingInfo bindingInfo = super.createBindingInfo();
            
        // This disables a bunch of unwanted interceptors for the Provider scenario. 
        // Not ideal, but it works.
        if (implInfo.isWebServiceProvider()) {
            bindingInfo.setProperty(AbstractBindingFactory.DATABINDING_DISABLED, Boolean.TRUE);
            
            if ((bindingInfo instanceof SoapBindingInfo) 
                && messageMode
                && !implInfo.getProviderParameterType().equals(SOAPMessage.class)) {
                bindingInfo.setProperty(SoapBindingFactory.MESSAGE_PROCESSING_DISABLED, Boolean.TRUE);
            }
        }
            
        return bindingInfo;
    }
    
    
}
