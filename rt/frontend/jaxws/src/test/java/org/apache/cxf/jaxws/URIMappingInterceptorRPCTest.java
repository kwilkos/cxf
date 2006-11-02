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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.interceptor.URIMappingInterceptor;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.hello_world_soap_http.RPCLitGreeterImpl;

public class URIMappingInterceptorRPCTest extends AbstractCXFTest {
    
    Message message;
    String ns = "http://apache.org/hello_world_rpclit";
    
    public void setUp() throws Exception {
        super.setUp();
        BindingFactoryManager bfm = getBus().getExtension(BindingFactoryManager.class);
        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", 
                                   new SoapBindingFactory());
        message = new MessageImpl();
        message.put(Message.HTTP_REQUEST_METHOD, "GET");
        message.put(Message.BASE_PATH, "/SOAPServiceRPCLit/SoapPort/");
        
        Exchange exchange = new ExchangeImpl();
        message.setExchange(exchange);        


        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        URL resource = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);
        bean.setBus(getBus());
        bean.setServiceClass(RPCLitGreeterImpl.class);
        RPCLitGreeterImpl greeter = new RPCLitGreeterImpl();
        BeanInvoker invoker = new BeanInvoker(greeter);
        bean.setInvoker(invoker);

        Service service = bean.create();
        
        EndpointInfo endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, "SoapPortRPCLit"));
        Endpoint endpoint = new EndpointImpl(getBus(), service, endpointInfo);
        exchange.put(Service.class, service);
        exchange.put(Endpoint.class, endpoint);
    }
    
    public void testGetSayHiFromPath() throws Exception {
        message.put(Message.PATH_INFO, "/SOAPServiceRPCLit/SoapPort/sayHi");       
        
        URIMappingInterceptor interceptor = new URIMappingInterceptor();
        interceptor.handleMessage(message);
        
        assertNull(message.getContent(Exception.class));
        
        Object parameters = message.getContent(List.class);
        assertNotNull(parameters);
        assertEquals(0, ((List)parameters).size());
        BindingOperationInfo boi = message.getExchange().get(BindingOperationInfo.class);
        assertNotNull(boi);
        assertEquals(new QName(ns, "sayHi"), boi.getName());
    }
    
    public void testGetGreetMeFromPath() throws Exception {
        message.put(Message.PATH_INFO, "/SOAPServiceRPCLit/SoapPort/greetMe/me/king+author");
        
        URIMappingInterceptor interceptor = new URIMappingInterceptor();        
        interceptor.handleMessage(message);
        
        assertNull(message.getContent(Exception.class));
        
        Object parameters = message.getContent(List.class);
        assertNotNull(parameters);
        assertEquals(1, ((List)parameters).size());
        String value = (String) ((List)parameters).get(0);
        assertEquals("king author", value);
    }
    
    public void testGetSayHiFromQuery() throws Exception {
        message.put(Message.PATH_INFO, "/SOAPServiceRPCLit/SoapPort/greetMe");
        message.put(Message.QUERY_STRING, "?me=king");
        
        URIMappingInterceptor interceptor = new URIMappingInterceptor();
        interceptor.handleMessage(message);
        
        assertNull(message.getContent(Exception.class));
        
        Object parameters = message.getContent(List.class);
        assertNotNull(parameters);
        assertEquals(1, ((List)parameters).size());
        String value = (String) ((List)parameters).get(0);
        assertEquals("king", value);
    }
    
    public void testGetSayHiFromQueryEncoded() throws Exception {
        message.put(Message.PATH_INFO, "/SOAPServiceRPCLit/SoapPort/greetMe");
        message.put(Message.QUERY_STRING, "?me=king+author");
        
        URIMappingInterceptor interceptor = new URIMappingInterceptor();
        interceptor.handleMessage(message);
        
        assertNull(message.getContent(Exception.class));
        
        Object parameters = message.getContent(List.class);
        assertNotNull(parameters);
        assertEquals(1, ((List)parameters).size());
        String value = (String) ((List)parameters).get(0);        
        assertEquals("king author", value);
    }
}
