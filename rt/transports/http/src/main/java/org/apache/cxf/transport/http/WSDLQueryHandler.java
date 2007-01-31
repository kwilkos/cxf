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

package org.apache.cxf.transport.http;

import java.io.OutputStream;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;

public class WSDLQueryHandler implements QueryHandler {

    public String getResponseContentType(String uri) {
        if (uri.toString().toLowerCase().endsWith("?wsdl")) {
            return "text/xml";
        }
        return null;
    }

    public boolean isRecognizedQuery(String uri, EndpointInfo endpointInfo) {       
        if (uri != null) {
            return endpointInfo.getAddress().contains(uri) 
                && uri.toString().toLowerCase().endsWith("?wsdl");   
        }
        return false;
    }

    public void writeResponse(String queryURI, EndpointInfo endpointInfo, OutputStream os) {
        try {
            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            Definition def = new ServiceWSDLBuilder(endpointInfo.getService()).build();
            wsdlWriter.writeWSDL(def, os);
        } catch (WSDLException wex) {
            wex.printStackTrace();
        }
    }

}
