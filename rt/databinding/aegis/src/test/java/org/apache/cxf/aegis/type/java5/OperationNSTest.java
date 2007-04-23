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
package org.apache.cxf.aegis.type.java5;

import java.util.List;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.junit.Before;
import org.junit.Test;

public class OperationNSTest extends AbstractAegisTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceClass(NotificationLogImpl.class);
        sf.setAddress("local://NotificationLogImpl");
        sf.getServiceFactory().setDataBinding(new AegisDatabinding());

        sf.create();
    }

    @Test
    public void testWSDL() throws Exception {
        Document wsdl = getWSDLDocument("NotificationService");

        addNamespace("xsd", XmlConstants.XSD);
        assertValid("//xsd:element[@name='Notify']", wsdl);
    }

    @WebService(name = "NotificationLog", targetNamespace = "http://www.sics.se/NotificationLog")
    public static interface NotificationLog {

        @WebMethod(operationName = "Notify", action = "")
        @Oneway
        void notify(@WebParam(name = "Notify",
                               targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                               Document notify);

        @WebMethod(operationName = "query", action = "")
        @WebResult(name = "queryResponse",
                   targetNamespace = "http://www.sics.se/NotificationLog")
        List<Document> query(@WebParam(name = "xpath",
                                       targetNamespace = "http://www.sics.se/NotificationLog")
                             String xpath);

        @WebMethod(operationName = "Notify2", action = "")
        @Oneway
        void notify2(@WebParam(name = "Notify",
             targetNamespace = "http://docs.oasis-open.org/wsn/2004/"
                 + "06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
             Document notify);
    }

    @WebService(endpointInterface = "org.apache.cxf.aegis.type.java5.OperationNSTest$NotificationLog",
                serviceName = "NotificationService")
    public static class NotificationLogImpl implements NotificationLog {

        public void notify(Document notify) {
        }

        public void notify2(Document notify) {
        }

        public List<Document> query(String xpath) {
            return null;
        }
    }
}
