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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;


public final class BuilderImpl extends Response.Builder {
    private int status = 200;
    private Object entity;

    public BuilderImpl() {
    }

    public BuilderImpl(Response r) {
        this.status = r.getStatus();
        this.entity = r.getEntity();
    }
    
    static Response.Builder create(Response response) {
        return new BuilderImpl(response);
    }

    public Response build() {
        Response r = new ResponseImpl(status, entity);
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
        return null;
    }

    public Response.Builder type(String type) {
        return null;
    }

    public Response.Builder language(String language) {
        return null;
    }

    public Response.Builder location(URI location) {
        return null;
    }

    public Response.Builder contentLocation(URI location) {
        return null;
    }

    public Response.Builder tag(EntityTag tag) {
        return null;
    }

    public Response.Builder tag(String tag) {
        return null;
    }

    public Response.Builder lastModified(Date lastModified) {
        return null;
    }

    public Response.Builder cacheControl(CacheControl cacheControl) {
        return null;
    }

    public Response.Builder cookie(NewCookie cookie) {
        return null;
    }

    public Response.Builder header(String name, Object value) {
        return null;
    }

}
