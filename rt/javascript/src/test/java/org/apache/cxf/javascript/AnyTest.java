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

import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.javascript.JavascriptTestUtilities.JSRunnable;
import org.apache.cxf.javascript.JavascriptTestUtilities.Notifier;
import org.apache.cxf.javascript.fortest.AnyImpl;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.springframework.context.support.GenericApplicationContext;
import uri.cxf_apache_org.jstest.types.any.alts.Alternative1;

/*
 * We end up here with a part with isElement == true, a non-array element, 
 * but a complex type for an array of the element.
 */

public class AnyTest extends JavascriptRhinoTest {

    private static final Logger LOG = LogUtils.getL7dLogger(AnyTest.class);

    AnyImpl implementor;

    public AnyTest() throws Exception {
        super();
    }

    @Override
    protected void additionalSpringConfiguration(GenericApplicationContext context) throws Exception {
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:AnyBeans.xml"};
    }
    
    @Before
    public void before() throws Exception {
        setupRhino("any-proxy-factory", 
                   "any-service-endpoint", 
                   "/org/apache/cxf/javascript/AnyTests.js",
                   true);
        implementor = (AnyImpl)endpoint.getImplementor();
        implementor.reset();
    }
    
    private Void acceptOneChalk(Context context) {
        LOG.info("About to call accept1 with Chalk" + endpoint.getAddress());
        testUtilities.rhinoCall("testAny1ToServerChalk",  
                                testUtilities.javaToJS(endpoint.getAddress()));
        assertEquals("before chalk", implementor.getBefore());
        Object someAlternative = implementor.getAny1value();
        assertTrue(someAlternative instanceof Alternative1);
        Alternative1 a1 = (Alternative1) someAlternative;
        assertEquals("bismuth", a1.getChalk());
        assertEquals("after chalk", implementor.getAfter());
        return null;
    }
    
    @Test
    public void callAcceptOneChalk() {
        testUtilities.runInsideContext(Void.class, new JSRunnable<Void>() {
            public Void run(Context context) {
                return acceptOneChalk(context);
            }
        });
    }
    
    private Void returnAny1(Context context) {
        Notifier notifier = 
            testUtilities.rhinoCallConvert("testAny1ToClientChalk", Notifier.class, 
                                           testUtilities.javaToJS(endpoint.getAddress()));
        
        boolean notified = notifier.waitForJavascript(1000 * 10);
        assertTrue(notified);
        Integer errorStatus = testUtilities.rhinoEvaluateConvert("globalErrorStatus", Integer.class);
        assertNull(errorStatus);
        String errorText = testUtilities.rhinoEvaluateConvert("globalErrorStatusText", String.class);
        assertNull(errorText);

        //This method returns a String
        String chalk = (String)testUtilities.rhinoEvaluate("globalResponseObject._any.object._chalk");
        assertEquals("dover", chalk);
        return null;
    }
    
    @Test
    public void callReturnAny1() throws Exception {
        testUtilities.runInsideContext(Void.class, new JSRunnable<Void>() {
            public Void run(Context context) {
                return returnAny1(context);
            }
        });
    }



}
