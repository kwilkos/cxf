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

import org.xml.sax.InputSource;

import org.apache.cxf.resource.ExtendedURIResolver;
import org.apache.ws.commons.schema.XmlSchemaException;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.apache.xml.resolver.Catalog;

/**
 * Resolves URIs using Apache Commons Resolver API.
 */
public class CatalogXmlSchemaURIResolver implements URIResolver {

    private ExtendedURIResolver resolver;
    private Catalog catalogResolver;

    public CatalogXmlSchemaURIResolver(OASISCatalogManager catalogManager) {
        this.resolver = new ExtendedURIResolver();
        this.catalogResolver = catalogManager.getCatalog();
    }

    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        String resolvedSchemaLocation = null;
        try {
            resolvedSchemaLocation = this.catalogResolver.resolveSystem(schemaLocation);
        } catch (IOException e) {
            throw new RuntimeException("Catalog resolution failed", e);
        }

        InputSource in = null;
        if (resolvedSchemaLocation == null) {
            in = this.resolver.resolve(schemaLocation, baseUri);
        } else {
            in = this.resolver.resolve(resolvedSchemaLocation, baseUri);
        }

        // XXX: If we return null, a NPE is raised in SchemaBuilder.
        // If we return new InputSource(), a XmlSchemaException is raised
        // but without any nice error message. So let's just throw a nice error here.
        if (in == null) {
            throw new XmlSchemaException("Unable to locate imported document "
                                         + "at '" + schemaLocation + "'"
                                         + (baseUri == null
                                            ? "."
                                            : ", relative to '" + baseUri + "'."));
        }

        return in;
    }
}
