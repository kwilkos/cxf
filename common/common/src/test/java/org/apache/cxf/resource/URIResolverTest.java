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

import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

public class URIResolverTest extends TestCase {
    
    private URIResolver uriResolver;
    
    private URL resourceURL = getClass().getResource("resources/helloworld.bpr");
    
    public void setUp() throws Exception {
        uriResolver = new URIResolver();
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testJARProtocol() throws Exception {
        
        byte[] barray = new byte[]{0};
        byte[] barray2 = new byte[]{1}; 
        String uriStr = "jar:" + resourceURL.toString() + "!/wsdl/hello_world.wsdl";
                
        // Check standard Java API's work with "jar:"
        URL jarURL = new URL(uriStr);
        InputStream is = jarURL.openStream();
        assertNotNull(is);
        if (is != null) {
            barray = new byte[is.available()];
            is.read(barray);
            is.close();
        }
        
        uriResolver.resolve("baseUriStr", uriStr, null);
            
        InputStream is2 = uriResolver.getInputStream();
        assertNotNull(is2); 
        if (is2 != null) {
            barray2 = new byte[is2.available()];
            is2.read(barray2);
            is2.close();
            
        }       
        assertEquals(new String(barray), new String(barray2));
    }
}
