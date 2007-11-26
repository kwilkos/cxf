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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.test.AbstractCXFSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 
 */
public class QueryHandlerTest extends AbstractCXFSpringTest {
    private static final Charset UTF8 = Charset.forName("utf-8");
    private static final Logger LOG = LogUtils.getL7dLogger(QueryHandlerTest.class);
    private EndpointImpl hwEndpoint;
    private EndpointImpl dlbEndpoint;

    public QueryHandlerTest() throws Exception {
        super();
    }

    /** {@inheritDoc}*/
    @Override
    protected void additionalSpringConfiguration(GenericApplicationContext context) throws Exception {
        // we don't need any.
    }

    /** {@inheritDoc}*/
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:HelloWorldDocLitBeans.xml",
                             "classpath:DocLitBareClientTestBeans.xml"};

    }
    
    @Before
    public void before() {
        hwEndpoint = getBean(EndpointImpl.class, "hw-service-endpoint");
        dlbEndpoint = getBean(EndpointImpl.class, "dlb-service-endpoint");

    }
    
    // This service runs into yet another RFSB/JAXB bug.
    @org.junit.Ignore
    @Test
    public void hwQueryTest() throws Exception {
        URL endpointURL = new URL(hwEndpoint.getAddress()  + "?js");
        InputStream jsStream = endpointURL.openStream();
        InputStreamReader isr = new InputStreamReader(jsStream, UTF8);
        BufferedReader in = new BufferedReader(isr);
        String line = in.readLine();
        StringBuilder js = new StringBuilder();
        while (line != null) {
            String[] tok = line.split("\\s");

            for (int x = 0; x < tok.length; x++) {
                String token = tok[x];
                js.append("  " + token);
            }
            line = in.readLine();
        }
        assertNotSame(0, js.length());
    }
    
    @Test
    public void dlbQueryTest() throws Exception {
        LOG.finest("logged to avoid warning on LOG");
        URL endpointURL = new URL(dlbEndpoint.getAddress()  + "?js");
        InputStream jsStream = endpointURL.openStream();
        InputStreamReader isr = new InputStreamReader(jsStream, UTF8);
        BufferedReader in = new BufferedReader(isr);
        String line = in.readLine();
        StringBuilder js = new StringBuilder();
        while (line != null) {
            String[] tok = line.split("\\s");

            for (int x = 0; x < tok.length; x++) {
                String token = tok[x];
                js.append("  " + token);
            }
            line = in.readLine();
        }
        assertNotSame(0, js.length());
    }
}
