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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.javascript.JavascriptTestUtilities.JSRunnable;
import org.apache.cxf.javascript.JavascriptTestUtilities.Notifier;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.context.support.GenericApplicationContext;

public class DocLitWrappedClientTest extends AbstractCXFSpringTest {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DocLitWrappedClientTest.class);

    // shadow declaration from base class.
    private JavascriptTestUtilities testUtilities;
    private JaxWsProxyFactoryBean clientProxyFactory;

    public DocLitWrappedClientTest() throws Exception {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:DocLitWrappedClientTestBeans.xml"};
    }

    @Before
    public void setupRhino() throws Exception {
        testUtilities.setBus(getBean(Bus.class, "cxf"));
        testUtilities.initializeRhino();
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/cxf-utils.js");
        clientProxyFactory = getBean(JaxWsProxyFactoryBean.class, "dlw-proxy-factory");
        Client client = clientProxyFactory.getClientFactoryBean().create();
        List<ServiceInfo> serviceInfos = client.getEndpoint().getService().getServiceInfos();
        // there can only be one.
        assertEquals(1, serviceInfos.size());
        ServiceInfo serviceInfo = serviceInfos.get(0);
        testUtilities.loadJavascriptForService(serviceInfo);
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/DocLitWrappedTests.js");
    }

    // just one test function to avoid muddles with engine startup/shutdown
    @Test
    public void runTests() throws Exception {
        testUtilities.runInsideContext(Void.class, new JSRunnable<Void>() {
            public Void run(Context context) {
                EndpointImpl endpoint = getBean(EndpointImpl.class, "dlw-service-endpoint");
                LOG.info("About to call test1 " + endpoint.getAddress());

                Notifier notifier = 
                    testUtilities.rhinoCallConvert("test1", Notifier.class, 
                                                   testUtilities.javaToJS(endpoint.getAddress()), 
                                                   testUtilities.javaToJS(Double.valueOf(7.0)),
                                                   testUtilities.javaToJS(Float.valueOf((float)11.0)), 
                                                   testUtilities.javaToJS(Integer.valueOf(42)),
                                                   testUtilities.javaToJS(Long.valueOf(240000)),
                                                   "This is the cereal shot from guns");
                boolean notified = notifier.waitForJavascript(1000 * 10);
                assertTrue(notified);
                Integer errorStatus = testUtilities.rhinoEvaluateConvert("globalErrorStatus", Integer.class);
                assertNull(errorStatus);
                String errorText = testUtilities.rhinoEvaluateConvert("globalErrorStatusText", String.class);
                assertNull(errorText);

                Scriptable responseObject = (Scriptable)testUtilities.rhinoEvaluate("globalResponseObject");
                assertNotNull(responseObject);
                String returnString = 
                    testUtilities.rhinoCallMethodInContext(String.class, responseObject, "getReturnValue");
                assertEquals("eels", returnString);
                return null; // well, null AND void.
            }
        });
    }

    public String getStaticResourceURL() throws Exception {
        File staticFile = new File(this.getClass().getResource("test.html").toURI());
        staticFile = staticFile.getParentFile();
        staticFile = staticFile.getAbsoluteFile();
        URL furl = staticFile.toURL();
        return furl.toString();
    }

    @Override
    protected void additionalSpringConfiguration(GenericApplicationContext context) throws Exception {
    }
}
