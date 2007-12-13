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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.javascript.hwdemo.GreeterImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


/**
 * This test is here because the Javascript came up missing important pieces
 * when first tried with the wsdl_first sample program.
 * This test tanks when maven runs it with in the regular mix. Un-ignore when
 * needed.
 */
@org.junit.Ignore
public class DemoServerTest {
    private static final Logger LOG = LogUtils.getL7dLogger(DemoServerTest.class);
    private static final Charset UTF8 = Charset.forName("utf-8");
    private static String oldConfigProperty;

    private Endpoint endpoint;
    
    @BeforeClass
    public static void beforeClass() {
        oldConfigProperty = System.setProperty("cxf.config.file", 
                                               "org/apache/cxf/javascript/SampleServer_cxf.xml");
        
    }
    
    @AfterClass
    public static void afterClass() {
        if (oldConfigProperty == null) {
            System.setProperty("cxf.config.file", "");
            
        } else {
            System.setProperty("cxf.config.file", oldConfigProperty);
        }
    }

    @Before
    public void before() throws Exception {
        Object implementor = new GreeterImpl();
        String address = "http://localhost:9000/SoapContext/SoapPort";
        endpoint = Endpoint.publish(address, implementor);
    }

    @After
    public void after() throws Exception {
        endpoint.stop();
    }
    
    private String getStringFromURL(URL url) throws IOException {
        InputStream jsStream = url.openStream();
        return readStringFromStream(jsStream);
    }

    private String readStringFromStream(InputStream jsStream) throws IOException {
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
            js.append("\n");
            line = in.readLine();
        }
        return js.toString();
    }
    
    @Test 
    public void testJavascriptCompleteness() throws Exception {
        LOG.fine("avoid warnings");
        URL endpointURL = new URL("http://localhost:9000/SoapContext/SoapPort?js");
        String js = getStringFromURL(endpointURL);
        // faultDetail will only be here if we got a nontrivial schema.
        assertTrue(js.contains("function apache_org_hello_world_soap_http_in"));
    }
}
