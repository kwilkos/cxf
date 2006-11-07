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

package org.apache.cxf.resource;

import java.io.IOException;

import junit.framework.TestCase;

public class URIResolverTest extends TestCase {

    public void testNestedRelative() throws Exception {
        URIResolver uriResolver = new URIResolver();
        try {
            uriResolver.resolveStateful("file:/c:/tmp1/a.wsdl", "./tmp2/b.xsd", getClass());
        } catch (IOException ioe) {
            // ignore the io exception, due to file not exist
        }
        assertEquals("check level 1: ", "file:/c:/tmp1/tmp2/b.xsd", uriResolver.getURI().toString());

        try {
            uriResolver.resolveStateful("./tmp2/b.xsd", "./tmp3/c.xsd", getClass());
        } catch (IOException ioe) {
            // ignore the io exception, due to file not exist
        }
        assertEquals("check level 2: ", "file:/c:/tmp1/tmp2/tmp3/c.xsd", uriResolver.getURI().toString());

        try {
            uriResolver.resolveStateful("./tmp3/c.xsd", "./tmp4/d.xsd", getClass());
        } catch (IOException ioe) {
            // ignore the io exception, due to file not exist
        }
        assertEquals("check level 3: ", "file:/c:/tmp1/tmp2/tmp3/tmp4/d.xsd", uriResolver.getURI()
            .toString());

    }

}
