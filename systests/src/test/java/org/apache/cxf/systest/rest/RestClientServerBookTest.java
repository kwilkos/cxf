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

package org.apache.cxf.systest.rest;



import java.util.logging.Logger;


import org.apache.cxf.binding.http.HttpBindingFactory;
import org.apache.cxf.customer.book.Book;
import org.apache.cxf.customer.book.BookService;
import org.apache.cxf.customer.book.BookServiceImpl;
import org.apache.cxf.customer.book.GetAnotherBook;
import org.apache.cxf.customer.book.GetBook;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.junit.BeforeClass;
import org.junit.Test;


public class RestClientServerBookTest extends AbstractBusClientServerTestBase {
    static final Logger LOG = Logger.getLogger(RestClientServerBookTest.class.getName());
    
    public static class MyServer extends AbstractBusTestServerBase {

        protected void run() {
            BookServiceImpl serviceObj = new BookServiceImpl();
            JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
            sf.setServiceClass(BookService.class);
            // Use the HTTP Binding which understands the Java Rest Annotations
            sf.setBindingId(HttpBindingFactory.HTTP_BINDING_ID);
            sf.setAddress("http://localhost:9080/xml/");
            sf.getServiceFactory().setInvoker(new BeanInvoker(serviceObj));

            // Turn the "wrapped" style off. This means that CXF won't generate
            // wrapper XML elements and we'll have prettier XML text. This
            // means that we need to stick to one request and one response
            // parameter though.
            sf.getServiceFactory().setWrapped(false);

            sf.create();
        }

        public static void main(String[] args) {
            try {
                MyServer s = new MyServer();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                LOG.info("done!");
            }
        }
    }
    
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(MyServer.class));
    }

    @Test
    public void testGetBookWithXmlRootElement() throws Exception {
        JaxWsProxyFactoryBean sf = new JaxWsProxyFactoryBean();
        sf.setServiceClass(BookService.class);

        // Turn off wrapped mode to make our xml prettier
        sf.getServiceFactory().setWrapped(false);

        // Use the HTTP Binding which understands the Java Rest Annotations
        sf.getClientFactoryBean().setBindingId(HttpBindingFactory.HTTP_BINDING_ID);
        sf.setAddress("http://localhost:9080/xml/");
        BookService bs = (BookService)sf.create();
        GetBook getBook = new GetBook();
        getBook.setId(123);
        Book book = bs.getBook(getBook);
        assertEquals(book.getId(), (long)123);
        assertEquals(book.getName(), "CXF in Action");
    }
    
    @Test
    public void testGetBookWithOutXmlRootElement() throws Exception {
        JaxWsProxyFactoryBean sf = new JaxWsProxyFactoryBean();
        sf.setServiceClass(BookService.class);

        // Turn off wrapped mode to make our xml prettier
        sf.getServiceFactory().setWrapped(false);

        // Use the HTTP Binding which understands the Java Rest Annotations
        sf.getClientFactoryBean().setBindingId(HttpBindingFactory.HTTP_BINDING_ID);
        sf.setAddress("http://localhost:9080/xml/");
        BookService bs = (BookService)sf.create();
        GetAnotherBook getAnotherBook = new GetAnotherBook();
        getAnotherBook.setId(123);
        Book book = bs.getAnotherBook(getAnotherBook);
        assertEquals(book.getId(), (long)123);
        assertEquals(book.getName(), "CXF in Action");
    }
}
