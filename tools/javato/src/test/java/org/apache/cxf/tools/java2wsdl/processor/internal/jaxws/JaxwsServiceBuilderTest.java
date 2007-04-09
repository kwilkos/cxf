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

import java.io.File;

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
        // REVISIT: Wrong part name
        //    <wsdl:message name="getPrice">
        //        <wsdl:part name="arg00" type="xsd:string">
        //    </wsdl:part>

        builder.setServiceClass(Stock.class);
        ServiceInfo service =  builder.build();
        generator.setServiceModel(service);
        File output = getOutputFile("stock_bare.wsdl");
        assertNotNull(output);
        generator.generate(output);
        assertTrue(output.exists());
    }

    public void testWrapped() {
        // REVIST: If there is wsdlLocation annotation, but the serviceBuilder can not load the wsdl,
        // we should print WARNs not throw exceptions and fail the build.
        // builder.setServiceClass(org.apache.cxf.tools.fortest.withannotation.doc.Hello.class);

        // REVISIT: Wrong part name
        //    <wsdl:message name="getPrice">
        //        <wsdl:part name="arg00" type="xsd:string">
        //    </wsdl:part>

        builder.setServiceClass(org.apache.cxf.tools.fortest.withannotation.doc.HelloWrapped.class);
        ServiceInfo service =  builder.build();
        generator.setServiceModel(service);
        File output = getOutputFile("hello_wrapped.wsdl");
        assertNotNull(output);
        generator.generate(output);
        assertTrue(output.exists());
    }

    private File getOutputFile(String fileName) {
        return new File(output, fileName);
    }
}
