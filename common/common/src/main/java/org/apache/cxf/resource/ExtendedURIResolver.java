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
import java.io.InputStream;
import java.util.Stack;

import org.xml.sax.InputSource;

public class ExtendedURIResolver {

    private URIResolver currentResolver;
    private Stack<InputStream> resourceOpened = new Stack<InputStream>();


    public InputSource resolve(String schemaLocation, String baseUri) {
        try {
            currentResolver = new URIResolver();
            currentResolver.resolveStateful(baseUri, schemaLocation, getClass());
            if (currentResolver.isResolved()) {
                if (currentResolver.getURI() != null && currentResolver.getURI().isAbsolute()) {
                    // When importing a relative file,
                    // setSystemId with an absolute path so the
                    // resolver finds any files which that file
                    // imports with locations relative to it.
                    schemaLocation = currentResolver.getURI().toString();
                }
                InputStream in = currentResolver.getInputStream();
                resourceOpened.addElement(in);
                InputSource source = new InputSource(in);
                source.setSystemId(schemaLocation);
                return source;
            }

        } catch (Exception e) {
            // move on...
        }
        return new InputSource(schemaLocation);
    }

    public void close() {
        try {
            while (!resourceOpened.isEmpty()) {
                InputStream in = resourceOpened.pop();
                in.close();
            }
        } catch (IOException ioe) {
            // move on...
        }
    }

    public String getLatestImportURI() {
        return currentResolver.getURI().toString();
    }

}
