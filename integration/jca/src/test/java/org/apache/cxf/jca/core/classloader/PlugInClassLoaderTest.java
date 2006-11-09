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
package org.apache.cxf.jca.core.classloader;


import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class PlugInClassLoaderTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(PlugInClassLoaderTest.class.getName());
    private static boolean debug;
    PlugInClassLoader plugInClassLoader;
   
    public PlugInClassLoaderTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {PlugInClassLoaderTest.class.getName()});
    }

    public static Test suite() {
        return new TestSuite(PlugInClassLoaderTest.class);
    }

    public void setUp() throws Exception {
        plugInClassLoader = new PlugInClassLoader(getClass().getClassLoader());
        if (debug) {
            LOG.setLevel(Level.INFO);
        } else {
            LOG.setLevel(Level.WARNING);
        }
    }

    public void testLoadClassWithPlugInClassLoader() throws Exception {
        Class resultClass = plugInClassLoader.loadClass(
                "org.apache.cxf.jca.dummy.Dummy");
        assertEquals("wrong class", "org.apache.cxf.jca.dummy.Dummy",
            resultClass.getName());
        assertEquals("class loader must be the plugInClassLoader",
            plugInClassLoader, resultClass.getClassLoader());
    }

    public void testInheritsClassLoaderProtectionDomain()
        throws Exception {
        Class resultClass = plugInClassLoader.loadClass(
                "org.apache.cxf.jca.dummy.Dummy");
        ProtectionDomain pd1 = plugInClassLoader.getClass().getProtectionDomain();
        ProtectionDomain pd2 = resultClass.getProtectionDomain();
        LOG.info("PluginClassLoader protection domain: " + pd1);
        LOG.info("resultClass protection domain: " + pd2);
        assertEquals("protection domain has to be inherited from the PluginClassLoader. ",
            pd1, pd2);
    }

    public void testLoadClassWithParentClassLoader() throws Exception {
        Class resultClass = plugInClassLoader.loadClass("org.omg.CORBA.ORB");
        assertEquals("wrong class", "org.omg.CORBA.ORB", resultClass.getName());
        assertTrue("class loader must NOT be the plugInClassLoader",
            !(plugInClassLoader.equals(resultClass.getClassLoader())));
    }

    public void testLoadNonExistentClassWithPlugInClassLoader()
        throws Exception {
        try {
            plugInClassLoader.loadClass("org.objectweb.foo.bar");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException ex) {
            LOG.fine("Exception message: " + ex.getMessage());
            assertNotNull("Exception message must not be null.", ex.getMessage());
            assertTrue("not found class must be part of the message. ",
                ex.getMessage().indexOf("org.objectweb.foo.bar") > -1);
        }
    }

    public void testLoadNonFilteredButAvailableClassWithPlugInClassLoader()
        throws Exception {
        String className = "javax.resource.ResourceException";
        // ensure it is available
        getClass().getClassLoader().loadClass(className); 
        try {
            Class claz = plugInClassLoader.loadClass(className);
            assertEquals("That should be same classloader ", claz.getClassLoader(),
                        getClass().getClassLoader());
            
        } catch (ClassNotFoundException ex) {
            fail("Do not Expect ClassNotFoundException");            
        }
    }

    public void testLoadResourceWithPluginClassLoader()
        throws Exception {
        Class resultClass = plugInClassLoader.loadClass(
                "org.apache.cxf.jca.dummy.Dummy");
        URL url = resultClass.getResource("dummy.txt");
        LOG.info("URL: " + url);
        assertTrue("bad url: " + url, url.toString().startsWith("classloader:"));
        

        InputStream configStream = url.openStream();
        assertNotNull("stream must not be null. ", configStream);
        assertTrue("unexpected stream class: " + configStream.getClass(),
            configStream instanceof java.io.ByteArrayInputStream);

        byte[] bytes = new byte[10];
        configStream.read(bytes, 0, bytes.length);

        String result = new String(bytes);
        LOG.fine("dummy.txt contents: " + result);
        assertEquals("unexpected dummy.txt contents.", "blah,blah.", result);
    }

    public void testLoadSlashResourceWithPluginClassLoader()
        throws Exception {
        Class resultClass = plugInClassLoader.loadClass(
                "org.apache.cxf.jca.dummy.Dummy");
        URL url = resultClass.getResource("/META-INF/MANIFEST.MF");
        LOG.info("URL: " + url);
        assertTrue("bad url: " + url, url.toString().startsWith("classloader:"));
    
        InputStream configStream = url.openStream();
        assertNotNull("stream must not be null. ", configStream);
        assertTrue("unexpected stream class: " + configStream.getClass(),
            configStream instanceof java.io.ByteArrayInputStream);
    
        byte[] bytes = new byte[21];
        configStream.read(bytes, 0, bytes.length);
    
        String result = new String(bytes);
        LOG.fine("dummy.txt contents: " + result);
        assertTrue("unexpected dummy.txt contents:"  + result, result.indexOf("Manifest-Version: 1.0") != -1);
    }

    public void testLoadNonExistentResourceWithPluginClassLoader()
        throws Exception {
        Class resultClass = plugInClassLoader.loadClass(
                "org.apache.cxf.jca.dummy.Dummy");
        URL url = resultClass.getResource("foo.txt");
        assertNull("url must be null. ", url);
    }

    public void testLoadNonExistentDirectory() throws Exception {
        URL url = plugInClassLoader.findResource("foo/bar/");
        assertNull("url must be null. ", url);
    }

    public void testLoadNonExistentNestedDirectory() throws Exception {
        URL url = plugInClassLoader.findResource("foo!/bar/");
        assertNull("url must be null. ", url);
    }
   
}
