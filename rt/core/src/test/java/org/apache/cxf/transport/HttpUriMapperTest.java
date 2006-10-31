package org.apache.cxf.transport;

import java.net.URL;

import junit.framework.TestCase;

public class HttpUriMapperTest extends TestCase {

    public void testGetContext() throws Exception {
        URL url = new URL("http://localhost:8080/SoapContext/SoapPort");
        String path = url.getPath();
        System.err.println(path);
        assertEquals("/SoapContext", HttpUriMapper.getContextName(path));
        
        url = new URL("http://localhost:8080/SoapContext/SoapPort/");
        path = url.getPath();
        System.err.println(path);
        assertEquals("/SoapContext/SoapPort", HttpUriMapper.getContextName(path));
    }
    
    public void testGetResourceBase() throws Exception {
        URL url = new URL("http://localhost:8080/SoapContext/SoapPort");
        String path = url.getPath();
        assertEquals("/SoapPort", HttpUriMapper.getResourceBase(path));
        url = new URL("http://localhost:8080/SoapContext/SoapPort/");
        path = url.getPath();
        assertEquals("/", HttpUriMapper.getResourceBase(path));
    }
}
