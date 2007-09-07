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


package org.apache.cxf.systest.jaxrs;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;


@UriTemplate("/bookstore/")
public class BookStore {

    @HttpContext UriInfo uriInfo;

    public BookStore() {
    }

    @HttpMethod("GET")
    public List<Book> getBooks() {
        System.out.println("----invoking getBooks");
        List<Book> books = new ArrayList<Book>(1);
        Book book = new Book();
        book.setId(123);
        book.setName("CXF in Action");
        books.add(book);
/*        
        Book book1 = new Book();
        book1.setId(124);
        book1.setName("CXF in Action - 2");
        books.add(book1);*/
        
        return books;
    }
    
    @HttpMethod("GET")
    @UriTemplate("/{bookId}/")
    public Book getBook(@UriParam("bookId") String bookId) {
        System.out.println("----invoking getBook with bookId: " + bookId);

        Book book = new Book();
        book.setId(123);
        book.setName("CXF in Action");
        
        return book;
    }
    
    @UriTemplate("/cd/{CDId}/")
    public CD getCD(@UriParam("CDId") String cdId) {
        System.out.println("----invoking getCD with cdId: " + cdId);
        CD cd = new CD();
        cd.setId(223);
        cd.setName("BOHEMIAN RHAYSODY");
        
        return cd;
    }

}
