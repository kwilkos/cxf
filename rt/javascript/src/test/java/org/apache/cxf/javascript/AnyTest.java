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
import org.apache.cxf.javascript.fortest.AnyImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

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
    
    @Test
    public void testServerStartup() throws Exception {
        LOG.fine("log something");
        // no need to do anything, just see what happens on init!
    }

}
