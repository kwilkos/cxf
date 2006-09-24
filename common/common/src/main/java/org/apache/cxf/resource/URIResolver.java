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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.cxf.common.classloader.ClassLoaderUtils;

/**
 * Resolves a File, classpath resource, or URL according to the follow rules:
 * <ul>
 * <li>Check to see if a File exists with the specified URI.</li>
 * <li>Attempt to create URI from the uri string directly and create an
 * InputStream from it.</li>
 * <li>Check to see if a File exists relative to the specified base URI.</li>
 * <li>Attempt to create URI relative to the base URI and create an InputStream
 * from it.</li>
 * <li>If the classpath doesn't exist, try to create URL from the URI.</li>
 * </ul>
 * 
 * @author Dan Diephouse
 */
public class URIResolver {
    private File file;
    private URI uri;
    private InputStream is;

    public URIResolver(String path) throws IOException {
        this("", path);
    }

    public URIResolver(String baseUriStr, String uriStr) throws IOException {
        this(baseUriStr, uriStr, null);
    }

    public URIResolver(String baseUriStr, String uriStr, Class callingClass) throws IOException {
        try {
            URI relative;
            File uriFile = new File(uriStr);
            uriFile = new File(uriFile.getAbsolutePath());
            if (uriFile.exists()) {
                relative = uriFile.toURI();
            } else {
                relative = new URI(uriStr);
            }

            if (relative.isAbsolute()) {
                uri = relative;
                is = relative.toURL().openStream();
            } else if (baseUriStr != null) {
                URI base;
                File baseFile = new File(baseUriStr);
                if (baseFile.exists()) {
                    base = baseFile.toURI();
                } else {
                    base = new URI(baseUriStr);
                }

                base = base.resolve(relative);
                if (base.isAbsolute()) {
                    is = base.toURL().openStream();
                    uri = base;
                }
            }
        } catch (URISyntaxException e) {
            // Do nothing
        }

        if (uri != null && "file".equals(uri.getScheme())) {
            file = new File(uri);
        }

        if (is == null && file != null && file.exists()) {
            uri = file.toURI();
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File was deleted! " + uriStr, e);
            }
        } else if (is == null) {
            if (callingClass == null) {
                callingClass = getClass();
            }
            
            URL url = ClassLoaderUtils.getResource(uriStr, callingClass);
            
            if (url == null) {
                try {
                    url = new URL(uriStr);
                    uri = new URI(url.toString());
                    is = url.openStream();
                } catch (MalformedURLException e) {
                    // Do nothing
                } catch (URISyntaxException e) {
                    // Do nothing
                }
            } else {
                try {
                    uri = new URI(url.toString());
                } catch (URISyntaxException e) {
                    // do nothing?
                }
                is = url.openStream();
            }
        }
    }

    public URI getURI() {
        return uri;
    }

    public InputStream getInputStream() {
        return is;
    }

    public boolean isFile() {
        return file.exists();
    }

    public File getFile() {
        return file;
    }

    public boolean isResolved() {
        return uri != null;
    }
}
