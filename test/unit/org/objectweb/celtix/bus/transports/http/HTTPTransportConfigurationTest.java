package org.objectweb.celtix.bus.transports.http;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.Configuration;

public class HTTPTransportConfigurationTest extends TestCase {

    public void testCreateClientConfiguration() {
        Configuration c = new HTTPClientTransportConfiguration(null, null);
        assertNotNull(c);
        assertEquals(30000, c.getInt("SendTimeout"));
    }

    public void testCreateServerConfiguration() {
        Configuration c = new HTTPServerTransportConfiguration(null, null);
        assertNotNull(c);
        assertEquals("text/xml", c.getString("ContentType"));
    }

}
