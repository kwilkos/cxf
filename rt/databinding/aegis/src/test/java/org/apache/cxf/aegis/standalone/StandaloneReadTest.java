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

package org.apache.cxf.aegis.standalone;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.aegis.AegisContext;
import org.apache.cxf.aegis.AegisXMLStreamDataReader;
import org.apache.cxf.test.TestUtilities;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * 
 */
public class StandaloneReadTest {
    private AegisContext context;
    private TestUtilities testUtilities;
    
    @Before
    public void before() {
        testUtilities = new TestUtilities(getClass());
        context = new AegisContext();
        context.initialize();
    }
    
    @Test
    public void testBasicTypeRead() throws Exception {
        XMLStreamReader streamReader = testUtilities.getResourceAsXMLStreamReader("stringElement.xml");
        AegisXMLStreamDataReader reader = 
            context.createReader(AegisXMLStreamDataReader.class, XMLStreamReader.class);
        Object something = reader.read(streamReader);
        assertTrue("ball-of-yarn".equals(something));
    }
}
