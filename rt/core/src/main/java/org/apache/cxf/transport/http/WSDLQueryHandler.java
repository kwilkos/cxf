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

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.StemMatchingQueryHandler;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.ResourceManagerWSDLLocator;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;


public class WSDLQueryHandler implements StemMatchingQueryHandler {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WSDLQueryHandler.class);
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLQueryHandler.class);
    private Bus bus;

    public WSDLQueryHandler(Bus b) {
        bus = b;
    }

    public String getResponseContentType(String baseUri, String ctx) {
        if (baseUri.toLowerCase().contains("?wsdl")
            || baseUri.toLowerCase().contains("?xsd=")) {
            return "text/xml";
        }
        return null;
    }

    public boolean isRecognizedQuery(String baseUri, String ctx, 
                                     EndpointInfo endpointInfo, boolean contextMatchExact) {
        if (baseUri != null 
            && (baseUri.toLowerCase().contains("?wsdl")
                || baseUri.toLowerCase().contains("?xsd="))) {
            if (contextMatchExact) {
                return endpointInfo.getAddress().contains(ctx);
            } else {
                // contextMatchStrategy will be "stem"
                return endpointInfo.getAddress().contains(getStem(baseUri));
            }
        }
        return false;
    }

    public void writeResponse(String baseUri, String ctxUri,
                              EndpointInfo endpointInfo, OutputStream os) {
        try {
            int idx = baseUri.toLowerCase().indexOf("?wsdl");
            String base = null;
            String wsdl = "";
            String xsd =  null;
            if (idx != -1) {
                base = baseUri.substring(0, baseUri.toLowerCase().indexOf("?wsdl"));
                wsdl = baseUri.substring(baseUri.toLowerCase().indexOf("?wsdl") + 5);
                if (wsdl.length() > 0) {
                    wsdl = wsdl.substring(1);
                }
            } else {
                base = baseUri.substring(0, baseUri.toLowerCase().indexOf("?xsd="));
                xsd = baseUri.substring(baseUri.toLowerCase().indexOf("?xsd=") + 5);
            }
            
            Map<String, Definition> mp = CastUtils.cast((Map)endpointInfo.getService()
                                                        .getProperty(WSDLQueryHandler.class.getName()));
            Map<String, SchemaReference> smp = CastUtils.cast((Map)endpointInfo.getService()
                                                        .getProperty(WSDLQueryHandler.class.getName() 
                                                                     + ".Schemas"));

            if (mp == null) {
                endpointInfo.getService().setProperty(WSDLQueryHandler.class.getName(),
                                                      new ConcurrentHashMap());
                mp = CastUtils.cast((Map)endpointInfo.getService()
                                    .getProperty(WSDLQueryHandler.class.getName()));
            }
            if (smp == null) {
                endpointInfo.getService().setProperty(WSDLQueryHandler.class.getName()
                                                      + ".Schemas",
                                                      new ConcurrentHashMap());
                smp = CastUtils.cast((Map)endpointInfo.getService()
                                    .getProperty(WSDLQueryHandler.class.getName()
                                                 + ".Schemas"));
            }
            
            if (!mp.containsKey(wsdl)) {
                Definition def = new ServiceWSDLBuilder(bus, endpointInfo.getService()).build();
                mp.put("", def);
                updateDefinition(def, mp, smp, base, endpointInfo);
            }
            
            
            Document doc;
            if (xsd == null) {
                Definition def = mp.get(wsdl);
    
                WSDLWriter wsdlWriter = bus.getExtension(WSDLManager.class)
                    .getWSDLFactory().newWSDLWriter();
                def.setExtensionRegistry(bus.getExtension(WSDLManager.class).getExtenstionRegistry());
                doc = wsdlWriter.getDocument(def);
            } else {
                SchemaReference si = smp.get(xsd);
                ResourceManagerWSDLLocator rml = new ResourceManagerWSDLLocator(si.getReferencedSchema()
                                                                                .getDocumentBaseURI(),
                                                                                bus);
                
                InputSource src = rml.getBaseInputSource();
                doc = XMLUtils.getParser().parse(src);
            }
            
            NodeList nl = doc.getDocumentElement()
                .getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                                        "import");
            for (int x = 0; x < nl.getLength(); x++) {
                Element el = (Element)nl.item(x);
                String sl = el.getAttribute("schemaLocation");
                if (smp.containsKey(sl)) {
                    el.setAttribute("schemaLocation", base + "?xsd=" + sl);
                }
            }
            nl = doc.getDocumentElement()
                .getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                                        "include");
            for (int x = 0; x < nl.getLength(); x++) {
                Element el = (Element)nl.item(x);
                String sl = el.getAttribute("schemaLocation");
                if (smp.containsKey(sl)) {
                    el.setAttribute("schemaLocation", base + "?xsd=" + sl);
                }
            }
            nl = doc.getDocumentElement()
                .getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/",
                                    "import");
            for (int x = 0; x < nl.getLength(); x++) {
                Element el = (Element)nl.item(x);
                String sl = el.getAttribute("location");
                if (mp.containsKey(sl)) {
                    el.setAttribute("location", base + "?wsdl=" + sl);
                }
            }
            
            XMLUtils.writeTo(doc, os);
        } catch (WSDLException wex) {
            throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL",
                                                     BUNDLE,
                                                     baseUri), wex);
        } catch (SAXException e) {
            throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL",
                                                     BUNDLE,
                                                     baseUri), e);
        } catch (IOException e) {
            throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL",
                                                     BUNDLE,
                                                     baseUri), e);
        } catch (ParserConfigurationException e) {
            throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL",
                                                     BUNDLE,
                                                     baseUri), e);
        }
    }
    
    
    protected void updateDefinition(Definition def, Map<String, Definition> done,
                                  Map<String, SchemaReference> doneSchemas,
                                  String base, EndpointInfo ei) {
        Collection<List> imports = CastUtils.cast((Collection<?>)def.getImports().values());
        for (List lst : imports) {
            List<Import> impLst = CastUtils.cast(lst);
            for (Import imp : impLst) {
                String start = imp.getLocationURI();
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    done.put(start, imp.getDefinition());
                    updateDefinition(imp.getDefinition(), done, doneSchemas, base, ei);
                }
            }
        }      
        
        
        /* This doesn't actually work.   Setting setSchemaLocationURI on the import
        * for some reason doesn't actually result in the new URI being written
        * */
        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el 
                : CastUtils.cast((List)types.getExtensibilityElements(), ExtensibilityElement.class)) {
                if (el instanceof Schema) {
                    Schema see = (Schema)el;
                    updateSchemaImports(see, doneSchemas, base);
                }
            }
        }
    }
    
    protected void updateSchemaImports(Schema schema,
                                           Map<String, SchemaReference> doneSchemas,
                                           String base) {
        Collection<List>  imports = CastUtils.cast((Collection<?>)schema.getImports().values());
        for (List lst : imports) {
            List<SchemaImport> impLst = CastUtils.cast(lst);
            for (SchemaImport imp : impLst) {
                String start = imp.getSchemaLocationURI();
                if (start != null && !doneSchemas.containsKey(start)) {
                    try {
                        //check to see if it's aleady in a URL format.  If so, leave it.
                        new URL(start);
                    } catch (MalformedURLException e) {
                        doneSchemas.put(start, imp);
                        updateSchemaImports(imp.getReferencedSchema(), doneSchemas, base);
                    }
                }
            }
        }
        List<SchemaReference> includes = CastUtils.cast(schema.getIncludes());
        for (SchemaReference included : includes) {
            String start = included.getSchemaLocationURI();
            if (start != null && !doneSchemas.containsKey(start)) {
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    doneSchemas.put(start, included);
                    updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
                }
            }
        }
    }
    
    public boolean isRecognizedQuery(String baseUri, String ctx, EndpointInfo endpointInfo) {
        return isRecognizedQuery(baseUri, ctx, endpointInfo, false);
    }
    
      
    private String getStem(String baseURI) {
        
        URL url = null;
        try {
            url = new URL(baseURI);
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "URL creation failed: ", e);
        }
        String port = String.valueOf(url.getPort());
        baseURI = baseURI.substring(baseURI.indexOf(port) + port.length(), baseURI.lastIndexOf("/"));
        
        return baseURI;
    }
     
}
