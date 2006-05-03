package org.objectweb.celtix.bus.jaxws.servlet;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.bus.transports.http.HTTPTransportFactory;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class ServletTransportFactory extends HTTPTransportFactory {
    protected CeltixServlet servlet;
    public ServletTransportFactory(CeltixServlet serv) {
        servlet = serv;
    }
    public ServerTransport createServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException {

        return new ServletServerTransport(bus, address, servlet);
    }
}