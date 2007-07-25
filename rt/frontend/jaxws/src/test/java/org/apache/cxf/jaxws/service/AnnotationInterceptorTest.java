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
package org.apache.cxf.jaxws.service;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.AbstractJaxWsTest;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.junit.Before;
import org.junit.Test;

public class AnnotationInterceptorTest extends AbstractJaxWsTest {
     
    private ServerFactoryBean fb;
    
    private JaxWsServerFactoryBean jfb;
    
    @Before
    public void setUp() {
        fb = new ServerFactoryBean();
        fb.setAddress("http://localhost");
        fb.setBus(getBus());
        
        jfb = new JaxWsServerFactoryBean();
        jfb.setAddress("http://localhost");
        jfb.setBus(getBus());
    }
    
    @Test
    public void testSimpleFrontend() throws Exception {
        fb.setServiceClass(HelloService.class);        
        HelloService hello = new HelloServiceImpl();
        fb.setServiceBean(hello);
        fb.create();
        
        List<Interceptor> interceptors = fb.getServer().getEndpoint().getInInterceptors();
        assertTrue(hasTestInterceptor(interceptors));
    }
    
    @Test
    public void testSimpleFrontendWithNoInterceptor() throws Exception {
        fb.setServiceClass(HelloService.class);        
        HelloService hello = new HelloServiceImplNoAnnotation();
        fb.setServiceBean(hello);
        fb.create();
        
        List<Interceptor> interceptors = fb.getServer().getEndpoint().getInInterceptors();
        assertFalse(hasTestInterceptor(interceptors));
    }
    
    
    @Test
    public void testJaxwsFrontendWithNoInterceptor() throws Exception {
        jfb.setServiceClass(SayHi.class);
        jfb.setServiceBean(new SayHiNoInterceptor());
        
        jfb.create();
        List<Interceptor> interceptors = jfb.getServer().getEndpoint().getInInterceptors();
        assertFalse(hasTestInterceptor(interceptors));
    }
    
    @Test
    public void testJaxwsFrontendWithImpl() throws Exception {
        jfb.setServiceClass(SayHi.class);
        SayHi implementor = new SayHiImplementation();
        jfb.setServiceBean(implementor);
        
        jfb.create();
        List<Interceptor> interceptors = jfb.getServer().getEndpoint().getInInterceptors();
        assertTrue(hasTestInterceptor(interceptors));
    }
    
    @Test
    public void testJaxWsFrontendWithInterceptorInSEI() throws Exception {
        jfb.setServiceClass(SayHiInterface.class);
        jfb.setServiceBean(new SayHiInterfaceImpl());
        jfb.create();
        
        List<Interceptor> interceptors = jfb.getServer().getEndpoint().getInInterceptors();
        assertTrue(hasTestInterceptor(interceptors));
    }
    
    @Test
    public void testJaxWsFrontend() throws Exception {
        jfb.setServiceClass(SayHiInterface.class);
        jfb.setServiceBean(new SayHiInterfaceImpl2());
        jfb.create();
        
        List<Interceptor> interceptors = jfb.getServer().getEndpoint().getInInterceptors();
        assertFalse(hasTestInterceptor(interceptors));
        assertTrue(hasTest2Interceptor(interceptors));
    }
    
    
    private boolean hasTestInterceptor(List<Interceptor> interceptors) {
        boolean flag = false;
        for (Interceptor it : interceptors) {
            if (it instanceof TestInterceptor) {
                flag = true;
            }
        }
        return flag;
    }
    
    private boolean hasTest2Interceptor(List<Interceptor> interceptors) {
        boolean flag = false;
        for (Interceptor it : interceptors) {
            if (it instanceof Test2Interceptor) {
                flag = true;
            }
        }
        return flag;
    }
    
    @InInterceptors(interceptors = "org.apache.cxf.jaxws.service.TestInterceptor")
    public class HelloServiceImpl implements HelloService {
        public String sayHi() {
            return "HI";
        }
    }
    
    public class HelloServiceImplNoAnnotation implements HelloService {
        public String sayHi() {
            return "HI";
        }
    }
    
    @WebService(serviceName = "SayHiService", 
                portName = "HelloPort",
                targetNamespace = "http://mynamespace.com/",
                endpointInterface = "org.apache.cxf.jaxws.service.SayHi")
    @InInterceptors (interceptors = {"org.apache.cxf.jaxws.service.TestInterceptor" })
    public class SayHiImplementation implements SayHi {
        public long sayHi(long arg) {
            return arg;
        }
        public void greetMe() {
            
        }
        public String[] getStringArray(String[] strs) {
            String[] strings = new String[2];
            strings[0] = "Hello" + strs[0];
            strings[1] = "Bonjour" + strs[1];
            return strings;
        }
        public List<String> getStringList(List<String> list) {
            List<String> ret = new ArrayList<String>();
            ret.add("Hello" + list.get(0));
            ret.add("Bonjour" + list.get(1));
            return ret;
        }        
    }
    
    @WebService(serviceName = "SayHiService", 
                portName = "HelloPort",
                targetNamespace = "http://mynamespace.com/",
                endpointInterface = "org.apache.cxf.jaxws.service.SayHi")
    public class SayHiNoInterceptor implements SayHi {
        public long sayHi(long arg) {
            return arg;
        }
        public void greetMe() {
            
        }
        public String[] getStringArray(String[] strs) {
            String[] strings = new String[2];
            strings[0] = "Hello" + strs[0];
            strings[1] = "Bonjour" + strs[1];
            return strings;
        }
        public List<String> getStringList(List<String> list) {
            List<String> ret = new ArrayList<String>();
            ret.add("Hello" + list.get(0));
            ret.add("Bonjour" + list.get(1));
            return ret;
        }        
    }
    
    @WebService(endpointInterface = "org.apache.cxf.jaxws.service.SayHiInterface")
    public class SayHiInterfaceImpl implements SayHiInterface {
        public String sayHi(String s) {
            return "HI";
        } 
    }
    
    @WebService(endpointInterface = "org.apache.cxf.jaxws.service.SayHiInterface")
    @InInterceptors (interceptors = "org.apache.cxf.jaxws.service.Test2Interceptor")
    public class SayHiInterfaceImpl2 implements SayHiInterface {
        public String sayHi(String s) {
            return "HI";
        } 
    }
    
    
}
