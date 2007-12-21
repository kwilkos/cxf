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

package org.apache.cxf.cxf1220;

import java.net.URL;

import org.w3c.dom.Document;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.test.TestUtilities;
import org.junit.Test;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;



/**
 * CXF-1220 reports an apparent failure to correctly cross-reference schema in a WSDL.
 */
@org.junit.Ignore
public class Cxf1220Test extends AbstractDependencyInjectionSpringContextTests {
    private TestUtilities testUtilities;

    public Cxf1220Test() {
        setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
        testUtilities = new TestUtilities(getClass());
    }

    @Test
    public void testJaxbCrossSchemaImport() throws Exception {
        testUtilities.setBus((Bus)applicationContext.getBean("cxf"));
        testUtilities.addDefaultNamespaces();
        JaxWsServerFactoryBean serverFactory = (JaxWsServerFactoryBean)applicationContext.getBean("server");
        Server s = serverFactory.getServer();
        URL url = new URL("http://localhost:7081/Cxf1220?wsdl");
        String wsdlString  = IOUtils.toString(url.openStream());
        assertNotNull(wsdlString);
        Document wsdl = testUtilities.getWSDLDocument(s);
        testUtilities.
             assertValid("//xsd:schema[@targetNamespace='http://apache.org/type_test/doc']/"
                         + "xsd:import[@namespace='http://apache.org/type_test/types1']", wsdl);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:cxf1220Beans.xml"};
    }

}
