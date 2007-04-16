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
package org.apache.cxf.transport.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.resource.ExtendedURIResolver;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class ServletController {
    
    private static final Logger LOG = Logger.getLogger(ServletController.class.getName());

    private ServletTransportFactory transport;
    private ServletContext servletContext;
    private CXFServlet cxfServlet;
 
    public ServletController(ServletTransportFactory df, ServletContext servCont, CXFServlet servlet) {
        this.transport = df;
        this.servletContext = servCont;
        this.cxfServlet = servlet;
    }

    public void invoke(HttpServletRequest request, HttpServletResponse res) throws ServletException {
        boolean wsdl = false;
        boolean xsd = false;
        if (request.getQueryString() != null && request.getQueryString().trim().equalsIgnoreCase("wsdl")) {
            wsdl = true;
        }       
        String xsdName = request.getRequestURI().substring(
            request.getRequestURI().lastIndexOf("/") + 1); 
        if (xsdName != null 
                && xsdName.substring(xsdName.lastIndexOf(".") + 1).equalsIgnoreCase("xsd")) {
            xsd = true;
        }
        try {
            EndpointInfo ei = new EndpointInfo();
            String address = "";
            
            if (xsd) {
                address = "http://localhost"
                          + request.getServletPath()
                          + (request.getPathInfo() == null ? "" 
                              : request.getPathInfo()
                                  .substring(0, request.getPathInfo().lastIndexOf(xsdName) - 1));
            } else {
                address = "http://localhost" + request.getServletPath() 
                    + (request.getPathInfo() == null ? "" : request.getPathInfo());
            }
            ei.setAddress(address);
           
            ServletDestination d = (ServletDestination)transport.getDestination(ei);

            if (d.getMessageObserver() == null) {
                if (request.getRequestURI().endsWith("services")
                    || request.getRequestURI().endsWith("services/")) {
                    generateServiceList(request, res);
                } else {
                    LOG.warning("Can't find the the request for " + address + "'s Observer ");
                    generateNotFound(request, res);
                }

            } else if (xsd) {
                generateXSD(request, res, d, xsdName);
            } else if (wsdl) {
                generateWSDL(request, res, d);
            } else {
                invokeDestination(request, res, d);
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
    
    private void generateServiceList(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        Collection<ServletDestination> destinations = transport.getDestinations();
        response.setContentType("text/html");        
        response.getWriter().write("<html><body>");
        if (destinations.size() > 0) {  
            for (ServletDestination sd : destinations) {
                if (null != sd.getEndpointInfo().getName()) {
                    String address = sd.getAddress().getAddress().getValue();
                    int bi = address.indexOf(CXFServlet.ADDRESS_PERFIX);
                    String reqPerfix = request.getRequestURL().toString();
                    if (reqPerfix.endsWith("/")) {
                        reqPerfix = reqPerfix.substring(0, reqPerfix.length() - 1);
                    }
                    address = reqPerfix 
                        + address.substring(bi + CXFServlet.ADDRESS_PERFIX.length());
                    response.getWriter().write("<p> <a href=\"" + address + "?wsdl\">");
                    response.getWriter().write(sd.getEndpointInfo().getName() + "</a> </p>");
                }    
            }
        } else {
            response.getWriter().write("No service was found.");
        }
        response.getWriter().write("</body></html>");
    }

    private void generateXSD(HttpServletRequest request, HttpServletResponse response, ServletDestination d,
                             String xsdName) throws ServletException {
        response.setHeader(HttpHeaderHelper.CONTENT_TYPE, "text/xml");
        try {
            OutputStream os = response.getOutputStream();
            ExtendedURIResolver resolver = new ExtendedURIResolver();
            Source source = null;

            EndpointInfo ei = d.getEndpointInfo();
            String wsdlBaseURI = ei.getService().getDescription().getBaseURI();
            if (wsdlBaseURI != null) {
                InputSource inputSource = resolver.resolve(xsdName, wsdlBaseURI);
                source = new SAXSource(inputSource);

            } else {
                source = new StreamSource(servletContext.getResourceAsStream("/WEB-INF/wsdl/" + xsdName));
            }
            Result result = new StreamResult(os);
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void generateNotFound(HttpServletRequest request, HttpServletResponse res) throws IOException {
        res.setStatus(404);
        res.setContentType("text/html");
        res.getWriter().write("<html><body>No service was found.</body></html>");
    }

    public void invokeDestination(HttpServletRequest request, HttpServletResponse response,
                                  ServletDestination d) throws ServletException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Service http request on thread: " + Thread.currentThread());
        }

        try {
            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, request.getInputStream());
            inMessage.put(AbstractHTTPDestination.HTTP_REQUEST, request);
            inMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, response);
            inMessage.put(Message.HTTP_REQUEST_METHOD, request.getMethod());
            inMessage.put(Message.PATH_INFO, request.getPathInfo());
            inMessage.put(Message.QUERY_STRING, request.getQueryString());
            inMessage.put(Message.CONTENT_TYPE, request.getContentType());
            inMessage.put(Message.ENCODING, request.getCharacterEncoding());
            SSLUtils.propogateSecureSession(request, inMessage);
            d.doMessage(inMessage);
        } catch (IOException e) {
            throw new ServletException(e);
        }

    }

    protected void generateWSDL(HttpServletRequest request, 
                                HttpServletResponse response, 
                                ServletDestination d)
        throws ServletException {
        
        try {
            OutputStream os = response.getOutputStream();

            EndpointInfo ei = d.getEndpointInfo();
            Definition def = new ServiceWSDLBuilder(ei.getService()).build();
            Port port = def.getService(ei.getService().getName()).getPort(ei.getName().getLocalPart());
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                if (SOAPBindingUtil.isSOAPAddress(el)) {
                    SoapAddress add = SOAPBindingUtil.getSoapAddress(el);
                    add.setLocationURI(request.getRequestURL().toString());
                }
                if (el instanceof AddressType) {
                    AddressType add = (AddressType)el;
                    add.setLocation(request.getRequestURL().toString());
                }
            }
            
            Bus bus = cxfServlet.getBus();
            if (bus.getExtension(QueryHandlerRegistry.class) != null) { 
                for (QueryHandler qh : bus.getExtension(QueryHandlerRegistry.class).getHandlers()) {
                    if (null != request.getQueryString() && request.getQueryString().length() > 0) {
                        String requestURL = request.getPathInfo() + "?" + request.getQueryString();
                        if (qh.isRecognizedQuery(requestURL, ei)) {
                            response.setContentType(qh.getResponseContentType(requestURL));
                            try {
                                qh.writeResponse(requestURL, ei, os);
                            } catch (Exception e) {
                                throw new ServletException(e);
                            }
                        }
                    }   
                }
            }

            response.getOutputStream().flush();
            
        } catch (WSDLException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
}
