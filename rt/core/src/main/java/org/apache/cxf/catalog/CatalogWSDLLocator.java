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
package org.apache.cxf.catalog;

import java.io.IOException;
import javax.wsdl.xml.WSDLLocator;

import org.xml.sax.InputSource;

import org.apache.cxf.resource.ExtendedURIResolver;
import org.apache.xml.resolver.Catalog;

/**
 * Resolves WSDL URIs using Apache Commons Resolver API.
 */
public class CatalogWSDLLocator implements WSDLLocator {

    private String wsdlUrl;
    private ExtendedURIResolver resolver;
    private Catalog catalogResolver;
    private String baseUri;
    
    public CatalogWSDLLocator(String wsdlUrl, Catalog catalogResolver) {
        this.wsdlUrl = wsdlUrl;
        this.baseUri = this.wsdlUrl;
        this.catalogResolver = catalogResolver;
        this.resolver = new ExtendedURIResolver();
    }

    public InputSource getBaseInputSource() {
        InputSource result =  resolver.resolve(baseUri, null);
        baseUri = resolver.getURI();
        return result;
    }

    public String getBaseURI() {
        return baseUri;
    }

    public String getLatestImportURI() {
        return resolver.getLatestImportURI();
    }

    public InputSource getImportInputSource(String parent, String importLocation) {
        this.baseUri = parent;

        String resolvedImportLocation = null;
        try {
            resolvedImportLocation = this.catalogResolver.resolveSystem(importLocation);
        } catch (IOException e) {
            throw new RuntimeException("Catalog resolution failed", e);
        }

        InputSource in = null;
        if (resolvedImportLocation == null) {
            in = this.resolver.resolve(importLocation, this.baseUri);
        } else {
            in =  this.resolver.resolve(resolvedImportLocation, null);
        }

        // XXX: If we return null (as per javadoc), a NPE is raised in WSDL4J code.
        // So let's return new InputSource() and let WSDL4J fail. Optionally, 
        // we can throw a similar exception as in CatalogXmlSchemaURIResolver.
        if (in == null) {
            in = new InputSource();
        }

        return in;
    }

    public void close() {
        resolver.close();
    }
}
