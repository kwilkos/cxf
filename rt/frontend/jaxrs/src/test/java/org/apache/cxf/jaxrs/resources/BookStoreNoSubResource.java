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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

@UriTemplate("/bookstore/")
public class BookStoreNoSubResource {

    public BookStoreNoSubResource() {
    }

    @HttpMethod("GET")
    @UriTemplate("/books/{bookId}/")
    public Book getBook(@UriParam("bookId") String id) {
        return null;
    }    

    @HttpMethod("POST")
    @UriTemplate("/books")
    public Response addBook(Book book) {
        return null;
    }

    @HttpMethod("PUT")
    @UriTemplate("/books/")
    public Response updateBook(Book book) {
        return null;
    }

    @HttpMethod("DELETE")
    @UriTemplate("/books/{bookId}/")
    public Response deleteBook(@UriParam("bookId") String id) {
        return null;
    }
}


