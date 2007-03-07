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
package org.codehaus.xfire.aegis.inheritance;

import org.apache.cxf.aegis.AbstractAegisTest;

public class ExceptionInheritanceTest extends AbstractAegisTest
{
//    private Service service;
//
//    public void setUp() throws Exception 
//    {
//        super.setUp();
//
//        HashMap props = new HashMap();
//        props.put(AegisBindingProvider.WRITE_XSI_TYPE_KEY, Boolean.TRUE);
//        ArrayList l = new ArrayList();
//        l.add(SimpleBean.class.getName());
//        l.add(WS1ExtendedException.class.getName());
//
//        props.put(AegisBindingProvider.OVERRIDE_TYPES_KEY, l);
//        
//        createService(WS1.class, null);
//        
//        service = getServiceFactory().create(WS1.class, props);
//        service.setInvoker(new BeanInvoker(new WS1Impl()));
//        getServiceRegistry().register(service);
//    }
//    
//    public void testClient() throws Exception 
//    {
//        WS1 client = (WS1) new XFireProxyFactory(getXFire()).create(service, "xfire.local://WS1");
//        
//        try 
//        {
//            client.throwException(true);
//        }
//        catch (WS1ExtendedException ex)
//        {
//            Object sb = ex.getSimpleBean();
//            assertTrue(sb instanceof SimpleBean);
//        }
//    }
    
    public void testNothing() {
        
    }
}
