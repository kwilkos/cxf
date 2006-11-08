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
package org.apache.cxf.resource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import org.xml.sax.InputSource;

import org.apache.ws.commons.schema.resolver.URIResolver;

/**
 * Resolves URIs in a more sophisticated fashion than XmlSchema's default URI
 * Resolver does by using our own {@link org.apache.cxf.resource.URIResolver}
 * class.
 */
public class XmlSchemaURIResolver implements URIResolver {

    private Stack<ResolverInfo> stack = new Stack<ResolverInfo>();
    private org.apache.cxf.resource.URIResolver currentResolver;
    
    private class ResolverInfo {
        String uri;
        org.apache.cxf.resource.URIResolver resolver;
        public ResolverInfo(String uri, org.apache.cxf.resource.URIResolver resolver) {
            this.uri = uri;
            this.resolver = resolver;
        }
        public String getUri() {
            return uri;
        }
        public org.apache.cxf.resource.URIResolver getResolver() {
            return resolver;
        }
    }
    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        try {
            if (baseUri != null) {
                URI check = null;
                if (baseUri.startsWith("classpath:")) {
                    check = new URI(baseUri.substring(10));
                } else if (baseUri.startsWith("jar:")) {
                    int i = baseUri.indexOf("!");
                    if (i != -1) {
                        String bu = baseUri.substring(i + 1);
                        check = new URI(bu.startsWith("file:") ? bu : "file:" + bu);
                    } else {
                        check = new URI(baseUri);
                    }
                } else {
                    check = new URI(baseUri);
                }
                if (check.isAbsolute()) {
                    currentResolver = new org.apache.cxf.resource.URIResolver();
                    stack.addElement(new ResolverInfo(schemaLocation, currentResolver));            
                } else {
                    while (!stack.isEmpty()) {
                        ResolverInfo ri = stack.pop();
                        if (ri.getUri().equals(baseUri)) {
                            currentResolver = ri.getResolver();
                            stack.addElement(ri);
                            break;
                        }
                    }
                    stack.addElement(new ResolverInfo(schemaLocation, currentResolver));            
                }
                if (currentResolver == null) {
                    throw new RuntimeException("invalidate schema import");
                }
            } else {
                if (currentResolver == null) {
                    currentResolver = new org.apache.cxf.resource.URIResolver();
                }
            }
            currentResolver.resolveStateful(baseUri, schemaLocation, getClass());
            if (currentResolver.isResolved()) {
                if (currentResolver.getURI() != null && currentResolver.getURI().isAbsolute()) {
                    // When importing a relative file,
                    // setSystemId with an absolute path so the
                    // resolver finds any files which that file
                    // imports with locations relative to it.
                    schemaLocation = currentResolver.getURI().toString();
                }
                InputSource source = new InputSource(currentResolver.getInputStream());
                source.setSystemId(schemaLocation);
                return source;
            }

        } catch (IOException e) {
            // move on...
        } catch (URISyntaxException use) {            
            // move on...
        }

        return new InputSource(schemaLocation);

    }

    
}
