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

package org.apache.cxf.tools.util;

import junit.framework.TestCase;

public class URIParserUtilTest extends TestCase {

    public void testGetPackageName() {
        String packageName = URIParserUtil.getPackageName("http://www.cxf.iona.com");
        assertEquals(packageName, "com.iona.cxf");
        packageName = URIParserUtil.getPackageName("urn://www.class.iona.com");
        assertEquals(packageName, "com.iona._class");
    }

    public void testNormalize() throws Exception {
        String uri = "wsdl/hello_world.wsdl";
        assertEquals(uri, URIParserUtil.normalize(uri));

        uri = "C:\\src\\wsdl/hello_world.wsdl";
        assertEquals("file:/C:/src/wsdl/hello_world.wsdl", URIParserUtil.normalize(uri));

        uri = "wsdl\\hello_world.wsdl";
        assertEquals("wsdl/hello_world.wsdl", URIParserUtil.normalize(uri));        
    }

    public void testGetAbsoluteURI() throws Exception {
        String uri = "wsdl/hello_world.wsdl";
        String uri2 = URIParserUtil.getAbsoluteURI(uri);
        assertNotNull(uri2);
        assertTrue(uri2.startsWith("file"));
        assertTrue(uri2.contains(uri));

        uri = getClass().getResource("/schemas/wsdl/xml-binding.xsd").toString();
        uri2 = URIParserUtil.getAbsoluteURI(uri);
        assertNotNull(uri2);
        assertTrue(uri2.startsWith("file"));
        assertTrue(uri2.contains(uri));        
    }
}
