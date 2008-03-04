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

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.apache.cxf.message.Message;

/**
 * This is actually a complete no-op implementation, null is a valid response,
 * by default all the precondions are met.
 *
 */

public class RequestImpl implements Request {
    
    public RequestImpl(Message m) {
        // complete
    }

    public Response evaluatePreconditions(EntityTag eTag) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response evaluatePreconditions(Date lastModified) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response evaluatePreconditions(EntityTag eTag, Variant variant) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response evaluatePreconditions(Date lastModified, Variant variant) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response evaluatePreconditions(Date lastModified, EntityTag eTag) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response evaluatePreconditions(Date lastModified, EntityTag eTag, Variant variant) {
        // TODO Auto-generated method stub
        return null;
    }

    public Variant selectVariant(List<Variant> arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

}
