package org.objectweb.celtix.bus.transports.http;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLPolicy;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

public class HTTPTransportConfigurationTest extends TestCase {

    public void testCreateClientConfiguration() {
        Configuration c = new HTTPClientTransportConfiguration(null, null);
        assertNotNull(c);
        HTTPClientPolicy cp = (HTTPClientPolicy)c.getObject("httpClient");
        assertNotNull(cp);
        assertEquals(30000, cp.getReceiveTimeout());
        AuthorizationPolicy ap = (AuthorizationPolicy)c.getObject("authorization");
        assertNull(ap);
        SSLPolicy sslp = (SSLPolicy)c.getObject("ssl");
        assertNotNull(sslp);
        assertTrue(!sslp.isSetUseSecureSockets());
    }

    public void testCreateServerConfiguration() {
        Configuration c = new HTTPServerTransportConfiguration(null, null);
        assertNotNull(c);
        HTTPServerPolicy sp = (HTTPServerPolicy)c.getObject("httpServer");
        assertNotNull(sp);
        assertEquals("text/xml", sp.getContentType());
        AuthorizationPolicy ap = (AuthorizationPolicy)c.getObject("authorization");
        assertNull(ap);
        SSLPolicy sslp = (SSLPolicy)c.getObject("ssl");
        assertNotNull(sslp);
        assertTrue(!sslp.isSetUseSecureSockets());
    }

}
