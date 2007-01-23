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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class ClassLoaderResolverTest extends TestCase {
    private static final String RESOURCE_DATA = "this is the resource data"; 

    private String resourceName;
    private ClassLoaderResolver clr; 
    
    public void setUp() throws IOException { 
        File resource = File.createTempFile("test", "resource");
        resource.deleteOnExit(); 
        resourceName = resource.getName();

        FileWriter writer = new FileWriter(resource);
        writer.write(RESOURCE_DATA);
        writer.write("\n");
        writer.close();

        URL[] urls = {resource.getParentFile().toURI().toURL()};
        ClassLoader loader = new URLClassLoader(urls); 
        assertNotNull(loader.getResourceAsStream(resourceName));
        assertNull(ClassLoader.getSystemResourceAsStream(resourceName));
        clr = new ClassLoaderResolver(loader);
    } 
    
    public void tearDown() {
        clr = null;
        resourceName = null;
    }
 
    public void testResolve() { 
        assertNull(clr.resolve(resourceName, null));
        assertNotNull(clr.resolve(resourceName, URL.class));
    } 

    public void testGetAsStream() throws IOException { 
        InputStream in = clr.getAsStream(resourceName);
        assertNotNull(in); 

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String content = reader.readLine(); 

        assertEquals("resource content incorrect", RESOURCE_DATA, content);
    } 

    public static Test suite() {
        return new TestSuite(ClassLoaderResolverTest.class);
    }
    
    public static void main(String[] args) {
        TestRunner.main(new String[] {ClassLoaderResolverTest.class.getName()});
    }
}
