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

package org.apache.cxf.jaxws;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;

import javax.xml.ws.WebServiceException;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.jaxws.spi.ProviderImpl;
import org.apache.cxf.service.model.EndpointInfo;

import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.GreeterImpl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EndpointReferenceTest extends AbstractJaxWsTest {

    private final String address = "http://localhost:9000/SoapContext/SoapPort";
    //private Destination d;
    private final QName serviceName = new QName("http://apache.org/hello_world_soap_http", "SOAPService");
    private final QName portName = new QName("http://apache.org/hello_world_soap_http", "SoapPort");

    @Before
    public void setUp() throws Exception {
        super.setUpBus();

        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        ei.setAddress(address);

        //d = localTransport.getDestination(ei);
    }

    @Test
    @Ignore("Not implemented yet")
    public void testBindingProviderSOAPBinding() throws Exception {
        javax.xml.ws.Service s = javax.xml.ws.Service
            .create(new QName("http://apache.org/hello_world_soap_http", "SoapPort"));
        assertNotNull(s);

        Greeter greeter = s.getPort(Greeter.class);
        BindingProvider bindingProvider = (BindingProvider)greeter;

        EndpointReference er = bindingProvider.getEndpointReference();
        assertNotNull(er);

        //If the BindingProvider instance has a binding that is either SOAP 1.1/HTTP or SOAP
        //1.2/HTTP, then a W3CEndpointReference MUST be returned.
        assertTrue(er instanceof W3CEndpointReference);
    }

    @Test
    @Ignore("Not implemented yet")
    public void testBindingProviderXMLBinding() throws Exception {
        javax.xml.ws.Service s = javax.xml.ws.Service
            .create(new QName("http://apache.org/hello_world_soap_http", "SoapPort"));
        assertNotNull(s);

        Greeter greeter = s.getPort(Greeter.class);
        BindingProvider bindingProvider = (BindingProvider)greeter;

        //If the binding is XML/HTTP an java.lang.UnsupportedOperationException MUST be thrown.
        try {
            bindingProvider.getEndpointReference();
            fail("Did not get expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // do nothing
        }
    }

    /*
     * Any JAX-WS supported epr metadata MUST match the Service instance¡¯s
     * ServiceName, otherwise a WebServiceExeption MUST be thrown. Any JAX-WS
     * supported epr metadata MUST match the PortName for the sei, otherwise a
     * WebServiceException MUST be thrown. If the Service instance has an
     * associated WSDL, its WSDL MUST be used to determine any binding
     * information, anyWSDL in a JAX-WS suppported epr metadata MUST be ignored.
     * If the Service instance does not have a WSDL, then any WSDL inlined in
     * the JAX-WS supported metadata of the epr MUST be used to determine
     * binding information. If there is not enough metadata in the Service
     * instance or in the epr metadata to determine a port, then a
     * WebServiceException MUST be thrown.
     */
    @Test
    @Ignore("Not implemented yet")
    public void testServiceGetPortUsingEndpointReference() throws Exception {
        javax.xml.ws.Service s = javax.xml.ws.Service
            .create(new QName("http://apache.org/hello_world_soap_http", "SoapPort"));

        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        DOMSource erXML = new DOMSource(doc);
        EndpointReference endpointReference = EndpointReference.readFrom(erXML);

        WebServiceFeature[] wfs = new WebServiceFeature[] {};

        Greeter greeter = s.getPort(endpointReference, Greeter.class, wfs);

        String response = greeter.greetMe("John");
        assertEquals("Hello John", response);
    }

    @Test
    @Ignore("Not implemented yet")
    public void testEndpointReferenceGetPort() throws Exception {
        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        DOMSource erXML = new DOMSource(doc);
        EndpointReference endpointReference = EndpointReference.readFrom(erXML);

        WebServiceFeature[] wfs = new WebServiceFeature[] {};

        Greeter greeter = endpointReference.getPort(Greeter.class, wfs);

        String response = greeter.greetMe("John");
        assertEquals("Hello John", response);
    }    
    
    @Test
    @Ignore("Not implemented yet")
    public void testEndpointGetEndpointReferenceSOAPBinding() throws Exception {
        GreeterImpl greeter = new GreeterImpl();
        EndpointImpl endpoint = new EndpointImpl(getBus(), greeter, (String)null);

        endpoint.publish("http://localhost:8080/test");

        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        Element referenceParameters = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(),
                                                                           "wsa:ReferenceParameters",
                                                                           "wsa:ReferenceParameters");
        EndpointReference endpointReference = endpoint.getEndpointReference(referenceParameters);
        assertNotNull(endpointReference);

        assertTrue(endpointReference instanceof W3CEndpointReference);

        //A returned W3CEndpointReferenceMUST also contain the specified referenceParameters.
        //W3CEndpointReference wer = (W3CEndpointReference)endpointReference;

        endpoint.stop();        
    }
    
    @Test
    @Ignore("Not implemented yet")
    public void testEndpointGetEndpointReferenceXMLBinding() throws Exception {
        GreeterImpl greeter = new GreeterImpl();
        EndpointImpl endpoint = new EndpointImpl(getBus(), greeter, (String)null);

        endpoint.publish("http://localhost:8080/test");

        try {
            InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
            Document doc = XMLUtils.parse(is);
            Element referenceParameters = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(),
                                                                               "wsa:ReferenceParameters",
                                                                               "wsa:ReferenceParameters");
            endpoint.getEndpointReference(referenceParameters);

            fail("Did not get expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            //do nothing
        }

        endpoint.stop();        
    }
    
    @Test
    @Ignore("Not implemented yet")
    public void testEndpointGetEndpointReferenceW3C() throws Exception {
        GreeterImpl greeter = new GreeterImpl();
        EndpointImpl endpoint = new EndpointImpl(getBus(), greeter, (String)null);

        endpoint.publish("http://localhost:8080/test");

        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        Element referenceParameters = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(),
                                                                           "wsa:ReferenceParameters",
                                                                           "wsa:ReferenceParameters");
        EndpointReference endpointReference = endpoint.getEndpointReference(W3CEndpointReference.class,
                                                                            referenceParameters);
        assertNotNull(endpointReference);

        assertTrue(endpointReference instanceof W3CEndpointReference);

        //A returned W3CEndpointReferenceMUST also contain the specified referenceParameters.
        //W3CEndpointReference wer = (W3CEndpointReference)endpointReference;

        endpoint.stop();        
    }
    
    
    @Test
    @Ignore("Not implemented yet")
    public void testEndpointGetEndpointReferenceInvalid() throws Exception {
        GreeterImpl greeter = new GreeterImpl();
        EndpointImpl endpoint = new EndpointImpl(getBus(), greeter, (String)null);

        endpoint.publish("http://localhost:8080/test");

        try {
            InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
            Document doc = XMLUtils.parse(is);
            Element referenceParameters = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(),
                                                                               "wsa:ReferenceParameters",
                                                                               "wsa:ReferenceParameters");
            endpoint.getEndpointReference(MyEndpointReference.class, referenceParameters);

            fail("Did not get expected WebServiceException");
        } catch (WebServiceException e) {
            // do nothing
        }

        endpoint.stop();        
    }
    
    @Test
    @Ignore("Not implemented yet")
    public void testProviderReadEndpointReference() throws Exception {
        ProviderImpl provider = new ProviderImpl();

        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        DOMSource erXML = new DOMSource(doc);
        EndpointReference endpointReference = provider.readEndpointReference(erXML);
        assertNotNull(endpointReference);

        assertTrue(endpointReference instanceof W3CEndpointReference);        
    }
    
    @Test
    @Ignore("Not implemented yet")
    public void testProviderCreateW3CEndpointReference() throws Exception {
        ProviderImpl provider = new ProviderImpl();

        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        Element referenceParameter = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(),
                                                                          "wsa:ReferenceParameters",
                                                                          "wsa:ReferenceParameters");
        List<Element> referenceParameters = new ArrayList<Element>();
        referenceParameters.add(referenceParameter);

        Element metadata = XMLUtils.fetchElementByNameAttribute(doc.getDocumentElement(), "wsa:metadata",
                                                                "wsa:metadata");
        List<Element> metadataList = new ArrayList<Element>();
        metadataList.add(metadata);

        W3CEndpointReference endpointReference = provider
            .createW3CEndpointReference("http://localhost:8080/test", serviceName, portName, metadataList,
                                        "wsdlDocumentLocation", referenceParameters);
        assertNotNull(endpointReference);
    }
    
    @Test
    @Ignore("Not implemented yet")
    public void testProviderGetPort() throws Exception {
        ProviderImpl provider = new ProviderImpl();
        
        InputStream is = getClass().getResourceAsStream("resources/hello_world_soap_http_infoset.xml");
        Document doc = XMLUtils.parse(is);
        DOMSource erXML = new DOMSource(doc);
        EndpointReference endpointReference = EndpointReference.readFrom(erXML);        

        WebServiceFeature[] wfs = new WebServiceFeature[] {};
        
        Greeter greeter = provider.getPort(endpointReference, Greeter.class, wfs);        

        String response = greeter.greetMe("John");
        assertEquals("Hello John", response);
    }
    
    final class MyEndpointReference extends EndpointReference {
        protected MyEndpointReference() {
        }

        public void writeTo(Result result) {
            // NOT_IMPLEMENTED
        }
    }

}
