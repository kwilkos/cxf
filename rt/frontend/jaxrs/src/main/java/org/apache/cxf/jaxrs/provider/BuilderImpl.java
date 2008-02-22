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

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Builder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.jaxrs.MetadataMap;

public final class BuilderImpl extends Builder {
    private int status = 200;
    private Object entity;
    private MultivaluedMap<String, Object> metadata = new MetadataMap<String, Object>();

    public BuilderImpl() {
    }

       
    public Response build() {
        ResponseImpl r = new ResponseImpl(status, entity);
        MetadataMap<String, Object> m = new MetadataMap<String, Object>();
        m.putAll(metadata);
        r.addMetadata(m);
        reset();
        return r;
    }

    public Response.Builder status(int s) {
        status = s;
        return this;
    }

    public Response.Builder entity(Object e) {
        entity = e;
        return this;
    }

    public Response.Builder type(MediaType type) {
        return type(type.toString());
    }

    public Response.Builder type(String type) {
        metadata.putSingle("Content-Type", type);
        return this;
    }

    public Response.Builder language(String language) {
        return null;
    }

    public Response.Builder location(URI location) {
        metadata.putSingle("Location", location.toString());
        return this;
    }

    public Response.Builder contentLocation(URI location) {
        metadata.putSingle("Content-Location", location.toString());
        return this;
    }

    public Response.Builder tag(EntityTag tag) {
        return tag(tag.toString());
    }

    public Response.Builder tag(String tag) {
        metadata.putSingle("ETag", tag.toString());
        return this;
    }

    public Response.Builder lastModified(Date lastModified) {
        metadata.putSingle("Last-Modified", lastModified.toString());
        return this;
    }

    public Response.Builder cacheControl(CacheControl cacheControl) {
        metadata.putSingle("Cache-Control", cacheControl.toString());
        return this;
    }

    public Response.Builder cookie(NewCookie cookie) {
        metadata.putSingle("Cookie", cookie.toString());
        return this;
    }

    @SuppressWarnings("unchecked")
    public Response.Builder header(String name, Object value) {
        HeaderProvider hp = 
            ProviderFactory.getInstance().createHeaderProvider(value.getClass());
        metadata.putSingle(name, hp.toString(value));
        return this;
    }

    
    @Override
    public Response.Builder variant(Variant variant) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Builder variants(List<Variant> variants) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void reset() {
        metadata.clear();
        entity = null;
        status = 200;
    }
}
