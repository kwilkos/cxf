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
import java.util.Properties;

import org.apache.cxf.Bus;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.GenericApplicationContext;


/**
 * 
 */
public class JsHttpRequestTest extends AbstractCXFSpringTest {

    // shadow declaration from base class.
    private JavascriptTestUtilities testUtilities;
    
    public JsHttpRequestTest() throws Exception {
        testUtilities = new JavascriptTestUtilities(getClass());
        testUtilities.addDefaultNamespaces();
    }
    
    public void additionalSpringConfiguration(GenericApplicationContext applicationContext) throws Exception {
        // bring in some property values from a Properties file
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("staticResourceURL", getStaticResourceURL());
        cfg.setProperties(properties);
        // now actually do the replacement
        cfg.postProcessBeanFactory(applicationContext.getBeanFactory());        
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:XMLHttpRequestTestBeans.xml"};
    }
    
    
    @Before
    public 
    void setupRhino() throws Exception {
        testUtilities.setBus(getBean(Bus.class, "cxf"));
        testUtilities.initializeRhino();
        JsXMLHttpRequest.register(testUtilities.getRhinoScope());
        testUtilities.readResourceIntoRhino("/org/apache/cxf/javascript/XMLHttpRequestTests.js");
    }
    
    @Test
    public void testInvalidURI() throws Exception {
        testUtilities.rhinoCallExpectingException("SYNTAX_ERR", "testOpaqueURI");
        testUtilities.rhinoCallExpectingException("SYNTAX_ERR", "testNonAbsolute");
        testUtilities.rhinoCallExpectingException("SYNTAX_ERR", "testNonHttp");
    }
    
    @Test
    public void testSequencing() throws Exception {
        testUtilities.rhinoCallExpectingException("INVALID_STATE_ERR", "testSendNotOpenError");
    }
    
    @Test
    public void testSyncHttpFetch() throws Exception {
        setupRhino();
        Object httpObj = testUtilities.rhinoCall("testSyncHttpFetch");
        assertNotNull(httpObj);
        assertTrue(httpObj instanceof String);
        String httpResponse = (String) httpObj;
        assertTrue(httpResponse.contains("\u05e9\u05dc\u05d5\u05dd"));
    }
    
    public String getStaticResourceURL() throws Exception {
        File staticFile = new File(this.getClass().getResource("test.html").toURI());
        staticFile = staticFile.getParentFile();
        staticFile = staticFile.getAbsoluteFile();
        URL furl = staticFile.toURL();
        return furl.toString();
    }
}
