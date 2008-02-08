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

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.EntityProvider;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;

@ProduceMime("application/atom+xml")
@ConsumeMime("application/atom+xml")
public class AtomFeedProvider implements EntityProvider<Feed> {

    private static final Abdera ATOM_ENGINE = new Abdera();
    
    public Feed readFrom(Class<Feed> clazz, MediaType mt, 
                         MultivaluedMap<String, String> headers, InputStream is)
        throws IOException {
        Document<Feed> doc = ATOM_ENGINE.getParser().parse(is);
        return doc.getRoot();
    }

    public boolean supports(Class<?> type) {
        
        return Feed.class.isAssignableFrom(type);
    }

    public void writeTo(Feed feed, MediaType mt, 
                        MultivaluedMap<String, Object> headers, OutputStream os)
        throws IOException {
        feed.writeTo(os);
    }

}
