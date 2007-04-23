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
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

public class OASISCatalogManager {

    private static final Logger LOG =
        Logger.getLogger(OASISCatalogManager.class.getName());

    private Catalog resolver;
    
    public OASISCatalogManager() {
        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setUseStaticCatalog(false);
        catalogManager.setIgnoreMissingProperties(true);
        CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
        this.resolver = catalogResolver.getCatalog();
    }
    
    public Catalog getCatalog() {
        return this.resolver;
    }

    public void loadContextCatalogs() {
        try {
            loadCatalogs(Thread.currentThread().getContextClassLoader());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error loading META-INF/jax-ws-catalog.xml catalog files", e);
        }
    }
    
    public void loadCatalogs(ClassLoader classLoader) throws IOException {
        if (classLoader == null) {
            return;
        }
        
        Enumeration<URL> catalogs = classLoader.getResources("META-INF/jax-ws-catalog.xml");
        while (catalogs.hasMoreElements()) {
            URL catalogURL = catalogs.nextElement();
            this.resolver.parseCatalog(catalogURL);
        }
    }
    
    public void loadCatalog(URL catalogURL) throws IOException {
        this.resolver.parseCatalog(catalogURL);
    }

    private static Catalog getContextCatalog() {
        OASISCatalogManager oasisCatalog = new OASISCatalogManager();
        oasisCatalog.loadContextCatalogs();
        return oasisCatalog.getCatalog();
    }

    public static Catalog getCatalog(Bus bus) {
        if (bus == null) {
            return getContextCatalog();
        }
        Catalog catalog = bus.getExtension(Catalog.class);
        if (catalog == null) {
            return getContextCatalog();
        } else {
            return catalog;
        }
    }
    
}
