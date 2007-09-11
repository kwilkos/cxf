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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@UriTemplate("/bookstore/")
public class BookStore {
    
    private static List<Book> books = new ArrayList<Book>();
    private static long bookId = 123;
    
    @HttpContext UriInfo uriInfo;

    static {
        Book book = new Book();
        book.setId(bookId);
        book.setName("CXF in Action");
        books.add(book);
    }
    
    public BookStore() {
    }

    @HttpMethod("GET")
    public List<Book> getAllItems() {
        System.out.println("----invoking getBooks");
       
        return books;
    }    
   
    @HttpMethod("GET")
    @UriTemplate("/books/{bookId}/")
    public Book getBook(@UriParam("bookId") String id) {
        System.out.println("----invoking getBook with cdId: " + id);
        long idNumber = Long.parseLong(id);
        for (Book b : books) {
            if (idNumber == b.getId()) {
                return b;
            }
        }
        
        return null;
    }
    
    @HttpMethod("POST")
    @UriTemplate("/books")
    public Book addBook(Book book) {
        System.out.println("----invoking addBook, book name is: " + book.getName());
        book.setId(++bookId);
        
        books.add(book);

        return book;
    }
    
    @HttpMethod("PUT")
    @UriTemplate("/books/")
    public Response updateBook(Book book) {
        System.out.println("----invoking updateBook, book name is: " + book.getName());
        boolean found = false;
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (b.getId() == book.getId()) {
                books.set(i, book);
                found = true;
                break;
            }
        }
        
        Response r;
        if (found) {
            r = Response.Builder.ok().build();
        } else {
            r = Response.Builder.notModified().build();
        }
        
        return r;       
    }
    
    
    @HttpMethod("DELETE")
    @UriTemplate("/books/{bookId}/")
    public Response deleteBook(@UriParam("bookId") String id) {
        System.out.println("----invoking deleteBook with bookId: " + id);
        long idNumber = Long.parseLong(id);
        boolean found = false;
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (idNumber == b.getId()) {
                books.remove(i);
                found = true;
                break;
            }
        }
        
        Response r;
        if (found) {
            r = Response.Builder.ok().build();
        } else {
            r = Response.Builder.notModified().build();
        }
        
        return r;        
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


