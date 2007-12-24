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
package org.apache.cxf.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProviderFactoryImplTest extends Assert {

    @Before
    public void setUp() throws Exception {

    }
    
    @Test
    public void testSortEntityProviders() throws Exception {
        ProviderFactoryImpl pf = new ProviderFactoryImpl();
        pf.registerEntityProvider(new TestStringProvider());
        
        List<EntityProvider> providers = pf.getEntityProviders();

        assertTrue(indexOf(providers, TestStringProvider.class) < indexOf(providers, StringProvider.class));
        //REVISIT the compare algorithm
        //assertTrue(indexOf(providers, JSONProvider.class) < indexOf(providers, TestStringProvider.class));
    }
    
    @Test
    public void testGetStringProvider() throws Exception {
        String[] methodMimeTypes = {"text/html"};
        EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
        .createEntityProvider(String.class, methodMimeTypes, false);
        assertTrue(provider instanceof StringProvider);
    }
    
    @Test
    public void testGetStringProviderWildCard() throws Exception {
        String[] methodMimeTypes = {"text/*"};
        EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
        .createEntityProvider(String.class, methodMimeTypes, false);
        assertTrue(provider instanceof StringProvider);
    }
    
    @Test
    public void testGetStringProviderUsingProviderDeclaration() throws Exception {
        String[] methodMimeTypes = {"text/html"};
        ProviderFactoryImpl pf = (ProviderFactoryImpl)ProviderFactory.getInstance();
        pf.registerEntityProvider(new TestStringProvider());
        EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
        .createEntityProvider(String.class, methodMimeTypes, false);
        assertTrue(provider instanceof TestStringProvider);
    }    
    
    @Test
    public void testGetJSONProviderConsumeMime() throws Exception {
        String[] methodMimeTypes = {"application/json"};
        EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
        .createEntityProvider(org.apache.cxf.jaxrs.resources.Book.class, methodMimeTypes, true);
        assertTrue(provider instanceof JSONProvider);
    }
    
    private int indexOf(List<EntityProvider> providers, Class providerType) {
        int index = 0;
        for (EntityProvider p : providers) {
            if (p.getClass().isAssignableFrom(providerType)) {
                break;
            }
            index++;
        }
        return index;
    }
    
    @ConsumeMime("text/html")
    @ProduceMime("text/html")
    private final class TestStringProvider implements EntityProvider<String>  {

        public boolean supports(Class<?> type) {
            return type == String.class;
        }

        public String readFrom(Class<String> type, MediaType m, MultivaluedMap<String, String> headers,
                               InputStream is) {
            try {
                return IOUtils.toString(is);
            } catch (IOException e) {
                // TODO: better exception handling
            }
            return null;
        }

        public void writeTo(String obj, MediaType m, MultivaluedMap<String, Object> headers, 
                            OutputStream os) {
            try {
                os.write(obj.getBytes());
            } catch (IOException e) {
                // TODO: better exception handling
            }
        }

    }
}
