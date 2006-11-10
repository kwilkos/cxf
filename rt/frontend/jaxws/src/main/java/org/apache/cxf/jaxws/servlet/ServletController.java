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
package org.apache.cxf.jaxws.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class ServletController {

    static final String HTTP_REQUEST = "HTTP_SERVLET_REQUEST";
    static final String HTTP_RESPONSE = "HTTP_SERVLET_RESPONSE";

    private static final Logger LOG = Logger.getLogger(ServletController.class.getName());

    private ServletTransportFactory transport;

    public ServletController(ServletTransportFactory df) {
        this.transport = df;
    }

    public void invoke(HttpServletRequest request, HttpServletResponse res) throws ServletException {
        boolean wsdl = false;
        if (request.getQueryString() != null && request.getQueryString().trim().equalsIgnoreCase("wsdl")) {
            wsdl = true;
        }

        try {
            EndpointInfo ei = new EndpointInfo();
            ei.setAddress("http://localhost" + request.getServletPath() + request.getPathInfo());
            ServletDestination d = (ServletDestination)transport.getDestination(ei);

            if (d.getMessageObserver() == null) {
                generateNotFound(request, res);
            } else if (wsdl) {
                generateWSDL(request, res, d);
            } else {
                invokeDestination(request, res, d);
            }
        } catch (IOException e) {
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
            inMessage.put(HTTP_REQUEST, request);
            inMessage.put(HTTP_RESPONSE, response);
            inMessage.put(Message.HTTP_REQUEST_METHOD, request.getMethod());
            inMessage.put(Message.PATH_INFO, request.getPathInfo());
            inMessage.put(Message.QUERY_STRING, request.getQueryString());
            inMessage.put(Message.CONTENT_TYPE, request.getContentType());
            inMessage.put(Message.ENCODING, request.getCharacterEncoding());

            d.doMessage(inMessage);
        } catch (IOException e) {
            throw new ServletException(e);
        }

    }

    protected void generateWSDL(HttpServletRequest request, 
                                HttpServletResponse response, 
                                ServletDestination d)
        throws ServletException {
        response.setHeader("Content-Type", "text/xml");

        try {
            OutputStream os = response.getOutputStream();

            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
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

            wsdlWriter.writeWSDL(def, os);
            response.getOutputStream().flush();
        } catch (WSDLException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
}
