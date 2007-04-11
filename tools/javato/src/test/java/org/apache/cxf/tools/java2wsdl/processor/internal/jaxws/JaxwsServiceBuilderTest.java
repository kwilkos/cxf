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

package org.apache.cxf.tools.java2wsdl.processor.internal.jaxws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.fortest.classnoanno.docbare.Stock;
import org.apache.cxf.tools.fortest.withannotation.doc.Hello;
import org.apache.cxf.tools.java2wsdl.generator.wsdl11.WSDL11Generator;

public class JaxwsServiceBuilderTest extends ProcessorTestBase {
    JaxwsServiceBuilder builder = new JaxwsServiceBuilder();
    WSDL11Generator generator = new WSDL11Generator();
    
    public void setUp() throws Exception {
        super.setUp();
        builder.setBus(BusFactory.getDefaultBus());
    }

    public void tearDown() {
    }
    
    public void testGetOutputFile() {
        builder.setServiceClass(Stock.class);
        assertNull(builder.getOutputFile());

        builder.setServiceClass(Hello.class);
        assertNotNull(builder.getOutputFile());
        File expected = new File("file:///c:/tmp.wsdl");
        assertTrue(expected.equals(builder.getOutputFile()));
    }

    public void testBare() {
        builder.setServiceClass(Stock.class);
        ServiceInfo service =  builder.build();
        generator.setServiceModel(service);
        File output = getOutputFile("stock_bare.wsdl");
        assertNotNull(output);
        generator.generate(output);
        assertTrue(output.exists());
        
        String expectedFile = this.getClass().getResource("resources/expected_stock_bare.wsdl").getFile();
        compareTextFile(expectedFile, output.getAbsolutePath());
    }

    public void xtestWrapped() {
        builder.setServiceClass(org.apache.cxf.tools.fortest.withannotation.doc.Hello.class);
        ServiceInfo service =  builder.build();
        generator.setServiceModel(service);
        File output = getOutputFile("hello_wrapped.wsdl");
        assertNotNull(output);
        generator.generate(output);
        assertTrue(output.exists());        
    }
    
    //TODO: CXF-519
    public void xtestAsyn() throws Exception {
        builder.setServiceClass(org.apache.hello_world_async_soap_http.GreeterAsync.class);
        ServiceInfo service =  builder.build();
        generator.setServiceModel(service);
        File output = getOutputFile("hello_async.wsdl");
        assertNotNull(output);
        generator.generate(output);
        assertTrue(output.exists());   

        String expectedFile = this.getClass().getResource("resources/expected_hello_world_async.wsdl")
            .getFile();
        compareTextFile(expectedFile, output.getAbsolutePath());
    }
    
    private File getOutputFile(String fileName) {
        return new File(output, fileName);
    }
    
    public static void compareTextFile(String location1, String location2) {
        String str1 = getStringFromFile(location1);
        String str2 = getStringFromFile(location2);

        StringTokenizer st1 = new StringTokenizer(str1);
        StringTokenizer st2 = new StringTokenizer(str2);

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            String tok1 = st1.nextToken();
            String tok2 = st2.nextToken();

            assertEquals("Compare failed", tok1, tok2);
        }

        assertTrue(!st1.hasMoreTokens());
    }

    private static String getStringFromFile(String location) {
        InputStream is = null;
        String result = null;

        try {
            is = new FileInputStream(new File(location));
            result = normalizeCRLF(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    //do nothing
                }
            }
        }

        return result;
    }
    
    private static String normalizeCRLF(InputStream instream) {
        BufferedReader in = new BufferedReader(new InputStreamReader(instream));
        StringBuffer result = new StringBuffer();
        String line = null;
        boolean commentChecked = false;
        String comment = "<!-- Generated by <javatowsdl>";

        try {
            while ((line = in.readLine()) != null) {
                if (!commentChecked) {
                    if (line.startsWith(comment)) {
                        commentChecked = true;
                        continue;
                    }
                }

                StringTokenizer tok = new StringTokenizer(line);

                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    result.append("  " + token);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result.toString();
    }
}
