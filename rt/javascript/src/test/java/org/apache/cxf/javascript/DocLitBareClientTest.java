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

package org.apache.cxf.javascript;

import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.JavascriptTestUtilities.JSRunnable;
import org.apache.cxf.javascript.JavascriptTestUtilities.Notifier;
import org.apache.cxf.javascript.fortest.SimpleDocLitWrappedImpl;
import org.apache.cxf.javascript.fortest.TestBean1;
import org.apache.cxf.javascript.fortest.TestBean2;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.context.support.GenericApplicationContext;

/*
 * We end up here with a part with isElement == true, a non-array element, 
 * but a complex type for an array of the element.
 */

@org.junit.Ignore
public class DocLitBareClientTest extends AbstractCXFSpringTest {

    private static final Logger LOG = LogUtils.getL7dLogger(DocLitBareClientTest.class);

    // shadow declaration from base class.
    private JavascriptTestUtilities testUtilities;
    private JaxWsProxyFactoryBean clientProxyFactory;
    private EndpointImpl endpoint;

    public DocLitBareClientTest() throws Exception {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
    }

    @Before
    public void setupRhino() throws Exception {
        testUtilities.setBus(getBean(Bus.class, "cxf"));
        testUtilities.initializeRhino();
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/cxf-utils.js");
        clientProxyFactory = getBean(JaxWsProxyFactoryBean.class, "dlb-proxy-factory");
        Client client = clientProxyFactory.getClientFactoryBean().create();
        List<ServiceInfo> serviceInfos = client.getEndpoint().getService().getServiceInfos();
        // there can only be one.
        assertEquals(1, serviceInfos.size());
        ServiceInfo serviceInfo = serviceInfos.get(0);
        testUtilities.loadJavascriptForService(serviceInfo);
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/DocLitBareTests.js");
        endpoint = getBean(EndpointImpl.class, "dlb-service-endpoint");
    }
    
    @Override
    protected void additionalSpringConfiguration(GenericApplicationContext context) throws Exception {
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:DocLitBareClientTestBeans.xml"};
    }

    private Void beanFunctionCaller(Context context) {
        TestBean1 b1 = new TestBean1(); 
        b1.stringItem = "strung";
        TestBean1[] beans = new TestBean1[3];
        beans[0] = new TestBean1();
        beans[0].beanTwoNotRequiredItem = new TestBean2("bean2");
        beans[1] = null;
        beans[2] = new TestBean1();
        beans[2].optionalIntArrayItem = new int[2];
        beans[2].optionalIntArrayItem[0] = 4;
        beans[2].optionalIntArrayItem[1] = 6;
        
        Object[] jsBeans = new Object[3];
        jsBeans[0] = testBean1ToJS(testUtilities, context, beans[0]);
        jsBeans[1] = testBean1ToJS(testUtilities, context, beans[1]);
        jsBeans[2] = testBean1ToJS(testUtilities, context, beans[2]);
        
        Scriptable jsBean1 = testBean1ToJS(testUtilities, context, b1);
        Scriptable jsBeanArray = context.newArray(testUtilities.getRhinoScope(), jsBeans);
        
        LOG.info("About to call beanFunctionTest " + endpoint.getAddress());
        Notifier notifier = 
            testUtilities.rhinoCallConvert("beanFunctionTest", Notifier.class, 
                                           testUtilities.javaToJS(endpoint.getAddress()),
                                           jsBean1,
                                           jsBeanArray);
        boolean notified = notifier.waitForJavascript(1000 * 10);
        assertTrue(notified);
        Integer errorStatus = testUtilities.rhinoEvaluateConvert("globalErrorStatus", Integer.class);
        assertNull(errorStatus);
        String errorText = testUtilities.rhinoEvaluateConvert("globalErrorStatusText", String.class);
        assertNull(errorText);

        // this method returns void, which translated into a Javascript object with no properties. 
        Scriptable responseObject = (Scriptable)testUtilities.rhinoEvaluate("globalResponseObject");
        assertNotNull(responseObject);
        SimpleDocLitWrappedImpl impl = getBean(SimpleDocLitWrappedImpl.class, "dlw-service");
        TestBean1 b1returned = impl.getLastBean1();
        assertEquals(b1, b1returned);
        TestBean1[] beansReturned = impl.getLastBean1Array();
        assertArrayEquals(beans, beansReturned);
        return null;
    }
    
    @Test
    public void callFunctionWithBeans() {
        LOG.info("about to call beanFunctionTest");
        testUtilities.runInsideContext(Void.class, new JSRunnable<Void>() {
            public Void run(Context context) {
                return beanFunctionCaller(context);
            }
        });
    }

    public static Scriptable testBean1ToJS(JavascriptTestUtilities testUtilities,
                                           Context context, 
                                           TestBean1 b1) {
        if (b1 == null) {
            return null; // black is always in fashion. (Really, we can be called with a null).
        }
        Scriptable rv = context.newObject(testUtilities.getRhinoScope(), 
                                          "org_apache_cxf_javascript_testns_testBean1");
        testUtilities.rhinoCallMethod(rv, "setStringItem", testUtilities.javaToJS(b1.stringItem));
        testUtilities.rhinoCallMethod(rv, "setIntItem", testUtilities.javaToJS(b1.intItem));
        testUtilities.rhinoCallMethod(rv, "setLongItem", testUtilities.javaToJS(b1.longItem));
        testUtilities.rhinoCallMethod(rv, "setBase64Item", testUtilities.javaToJS(b1.base64Item));
        testUtilities.rhinoCallMethod(rv, "setOptionalIntItem", testUtilities.javaToJS(b1.optionalIntItem));
        testUtilities.rhinoCallMethod(rv, "setOptionalIntArrayItem",
                                      testUtilities.javaToJS(b1.optionalIntArrayItem));
        testUtilities.rhinoCallMethod(rv, "setDoubleItem", testUtilities.javaToJS(b1.doubleItem));
        testUtilities.rhinoCallMethod(rv, "setBeanTwoItem", testBean2ToJS(testUtilities,
                                                                          context, b1.beanTwoItem));
        testUtilities.rhinoCallMethod(rv, "setBeanTwoNotRequiredItem", 
                                      testBean2ToJS(testUtilities, context, b1.beanTwoNotRequiredItem));
        return rv; 
    }

    public static Object testBean2ToJS(JavascriptTestUtilities testUtilities,
                                       Context context, TestBean2 beanTwoItem) {
        if (beanTwoItem == null) {
            return null;
        }
        Scriptable rv = context.newObject(testUtilities.getRhinoScope(), 
                                          "org_apache_cxf_javascript_testns3_testBean2");
        testUtilities.rhinoCallMethod(rv, "setStringItem", beanTwoItem.stringItem);
        return rv;
    }
}
