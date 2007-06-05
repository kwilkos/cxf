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
package org.apache.cxf.wsdl11;

import java.io.InputStream;
import java.net.URL;

import javax.wsdl.xml.WSDLLocator;
import org.xml.sax.InputSource;

import org.apache.cxf.Bus;
import org.apache.cxf.catalog.CatalogWSDLLocator;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.resource.ResourceManager;


public class ResourceManagerWSDLLocator implements WSDLLocator {
    WSDLLocator parent;
    Bus bus;
    String wsdlUrl;
    InputSource last;
    
    public ResourceManagerWSDLLocator(String wsdlUrl,
                                      WSDLLocator parent,
                                      Bus bus) {
        this.wsdlUrl = wsdlUrl;
        this.bus = bus;
        this.parent = parent;
    }

    public ResourceManagerWSDLLocator(String wsdlUrl,
                                      Bus bus) {
        this.wsdlUrl = wsdlUrl;
        this.bus = bus;
        this.parent = new CatalogWSDLLocator(wsdlUrl, OASISCatalogManager.getCatalogManager(bus));
    }


    public void close() {
        if (last != null) {
            parent.close();
        }
    }

    public InputSource getBaseInputSource() {
        InputSource is = parent.getBaseInputSource();
        if (is == null) {
            InputStream ins = bus.getExtension(ResourceManager.class).getResourceAsStream(wsdlUrl);
            is = new InputSource(ins);
            is.setSystemId(wsdlUrl);
            is.setPublicId(wsdlUrl);

            URL url = bus.getExtension(ResourceManager.class).resolveResource(wsdlUrl, URL.class);
            if (url != null) {
                is.setSystemId(url.toString());
                is.setPublicId(url.toString());
            }
            last = is;
        } else {
            last = null;
        }
        
        return is;
    }

    public String getBaseURI() {
        getBaseInputSource();
        if (last == null) {
            return parent.getBaseURI();
        }
        return last.getPublicId();
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        // TODO Auto-generated method stub
        return parent.getImportInputSource(parentLocation, importLocation);
    }

    public String getLatestImportURI() {
        // TODO Auto-generated method stub
        return parent.getLatestImportURI();
    }

}
