package org.objectweb.celtix.tools.jaxws;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.objectweb.celtix.tools.common.ToolException;

public class JAXWSToolHelperTest extends TestCase {
    
    public void testServicePropertiesSet() { 
        
        // force class to set the necessary properties
        JAXWSToolHelper.setSystemProperties();
        
        assertEquals("com.sun.xml.stream.ZephyrWriterFactory", 
                     System.getProperty("javax.xml.stream.XMLOutputFactory"));
        assertEquals("com.sun.xml.stream.ZephyrParserFactory", 
                     System.getProperty("javax.xml.stream.XMLInputFactory"));
        assertEquals("com.sun.xml.stream.events.ZephyrEventFactory", 
                     System.getProperty("javax.xml.stream.XMLEventFactory"));
    }
    
    
    public void testGetURLsAsPath() throws MalformedURLException {
        
        URL[] urls = {new URL("file:/foo/bar/baz.jar"), new URL("file:/tmp")};
        String path = JAXWSToolHelper.getURLsAsPath(urls);
        assertNotNull(path);
        StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
        assertEquals(urls.length, tok.countTokens());
        assertEquals(urls[0].getPath(), tok.nextToken());
        assertEquals(urls[1].getPath(), tok.nextToken());
        
    }
    
    public void testGetURLsAsPathWithEmtpyURLs() throws MalformedURLException {
        String path = JAXWSToolHelper.getURLsAsPath(new URL[0]);
        assertNotNull(path);
        assertEquals("", path);
        
        path = JAXWSToolHelper.getURLsAsPath(null);
        assertNotNull(path);
        assertEquals("", path);
        
    }
    
    public void testGetJAXWSClassPath() {
        
        // picking up the jaxws.home value from eclipse is a royal pia.  
        // So, if the property is not set, assume that we are running in
        // the context of an eclipse project, so skip this test.  If the 
        // property is set, we assume we are running from ant, so run the 
        // test 
        String jaxwsHome = System.getProperty("jaxws.home", "");
        if ("".equals(jaxwsHome)) {
            System.err.println("skipping test <" + getName() + "> in eclipse environment");           
            return;
        }
        URL[] urls = JAXWSToolHelper.getJAXWSClassPath();
        assertNotNull(urls);
        assertTrue(urls.length > 0);
        
        // make sure case and path separators are consistent for
        // windows before doing any comparisons 
        jaxwsHome = normalisePath(jaxwsHome); 
        for (int i = 0; i < urls.length; i++) {
            String path = normalisePath(urls[i].getPath());
            assertTrue("<path:" + path + "> jaxwsHome:<" + jaxwsHome + ">", path.startsWith(jaxwsHome));
            assertTrue(new File(urls[i].getPath()).exists());
        }
    }
    
    
    private String normalisePath(String path) {
        
        String ret = path.replace('\\', '/');    
        if (ret.startsWith("/")) {
            ret = ret.substring(1);
        }
        return ret;
    }
    
    public void testGetJAXWSClassPathWithNoProperty() {
        String origValue = System.getProperty("jaxws.home", "");
        System.setProperty("jaxws.home", "");
        
        try {
            JAXWSToolHelper.getJAXWSClassPath();
            fail("did not receive expected exception");
        } catch (ToolException ex) {
            assertEquals(JAXWSToolHelper.NO_JAXWS_HOME_MSG, ex.getMessage());
        } finally { 
            System.setProperty("jaxws.home", origValue);
        }
    }
    
    public void testSetSystemClassPath() throws MalformedURLException {
        
        final String origClasspath = System.getProperty("java.class.path");
        final URL[] path = {new URL("file:/foo/bar/baz"), new URL("file:/wibbly/wobbly/wonder.jar")};
        
        JAXWSToolHelper.setSystemClassPath(path);
        
        final String newClasspath = System.getProperty("java.class.path");
        
        assertTrue(newClasspath.contains(path[0].getPath()));
        assertTrue(newClasspath.contains(path[1].getPath()));
        assertTrue(newClasspath.contains(origClasspath));
        
    }
}
