package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPServerTransport implements ServerTransport {
    EndpointReferenceType reference;
    String url;
    
    public HTTPServerTransport(Bus bus, EndpointReferenceType ref) throws WSDLException, IOException {
        reference = ref;
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        List<?> list = port.getExtensibilityElements();
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof SOAPAddress) {
                SOAPAddress ad = (SOAPAddress)ext;
                url = ad.getLocationURI();
            }
        }
        URL nurl = new URL(url);
        
        Embedded embedded = new Embedded();
        Engine engine = embedded.createEngine();
        engine.setDefaultHost("localhost");
        
        Host host = embedded.createHost("localhost", "/webapps");
        engine.addChild(host);

        Context context = embedded.createContext("", "/webapps/ROOT");
        host.addChild(context);

        embedded.addEngine(engine);
        Connector connect = embedded.createConnector((String)null, nurl.getPort(), false);
        embedded.addConnector(connect);

        try {
            embedded.start();
        } catch (LifecycleException e) {
            throw (IOException)new IOException("Could not start embedded tomcat server").initCause(e);
        }
    }

    public void activate(ServerTransportCallback callback) {
        // TODO Auto-generated method stub

    }

    public void deactivate() {
        // TODO Auto-generated method stub

    }

    public void shutdown() {
        // TODO Auto-generated method stub

    }

}
