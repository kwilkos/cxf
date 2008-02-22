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


package org.apache.cxf.jaxrs.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.Response;

@Path("/bookstore/")
public class BookStoreNoSubResource {

    public BookStoreNoSubResource() {
    }

    @GET
    @Path("/books/{bookId}/")
    @ProduceMime("application/xml")
    public Book getBook(@UriParam("bookId") String id) {
        return null;
    }    
    
    @GET
    @Path("/books/{bookId}/")
    @ProduceMime("application/json")
    public Book getBookJSON(@UriParam("bookId") String id) {
        return null;
    } 

    @POST
    @Path("/books")
    public Response addBook(Book book) {
        return null;
    }

    @PUT
    @Path("/books/")
    public Response updateBook(Book book) {
        return null;
    }

    @Path("/books/{bookId}/")
    @DELETE
    public Response deleteBook(@UriParam("bookId") String id) {
        return null;
    }
}


