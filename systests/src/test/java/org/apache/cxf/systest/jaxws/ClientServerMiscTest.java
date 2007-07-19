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

package org.apache.cxf.systest.jaxws;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import org.apache.cxf.anonymous_complex_type.AnonymousComplexType;
import org.apache.cxf.anonymous_complex_type.AnonymousComplexTypeService;
import org.apache.cxf.anonymous_complex_type.RefSplitName;
import org.apache.cxf.anonymous_complex_type.RefSplitNameResponse;
import org.apache.cxf.anonymous_complex_type.SplitName;
import org.apache.cxf.anonymous_complex_type.SplitNameResponse.Names;
import org.apache.cxf.jaxb_element_test.JaxbElementTest;
import org.apache.cxf.jaxb_element_test.JaxbElementTest_Service;
import org.apache.cxf.ordered_param_holder.ComplexStruct;
import org.apache.cxf.ordered_param_holder.OrderedParamHolder;
import org.apache.cxf.ordered_param_holder.OrderedParamHolder_Service;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientServerMiscTest extends AbstractBusClientServerTestBase {


    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(ServerMisc.class, true));
    }

    @Test
    public void testAnonymousComplexType() throws Exception {

        AnonymousComplexTypeService actService = new AnonymousComplexTypeService();
        assertNotNull(actService);
        QName portName = new QName("http://cxf.apache.org/anonymous_complex_type/",
            "anonymous_complex_typeSOAP");
        AnonymousComplexType act = actService.getPort(portName, AnonymousComplexType.class);

        try {
            Names reply = act.splitName("Tom Li");
            assertNotNull("no response received from service", reply);
            assertEquals("Tom", reply.getFirst());
            assertEquals("Li", reply.getSecond());
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    @Test
    public void testRefAnonymousComplexType() throws Exception {

        AnonymousComplexTypeService actService = new AnonymousComplexTypeService();
        assertNotNull(actService);
        QName portName = new QName("http://cxf.apache.org/anonymous_complex_type/",
            "anonymous_complex_typeSOAP");
        AnonymousComplexType act = actService.getPort(portName, AnonymousComplexType.class);

        try {
            SplitName name = new SplitName();
            name.setName("Tom Li");
            RefSplitName refName = new RefSplitName();
            refName.setSplitName(name);
            RefSplitNameResponse reply = act.refSplitName(refName);
            assertNotNull("no response received from service", reply);
            assertEquals("Tom", reply.getSplitNameResponse().getNames().getFirst());
            assertEquals("Li", reply.getSplitNameResponse().getNames().getSecond());
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    @Test
    public void testMinOccursAndNillableJAXBElement() throws Exception {

        JaxbElementTest_Service service = new JaxbElementTest_Service();
        assertNotNull(service);
        JaxbElementTest port = service.getPort(JaxbElementTest.class);

        try {

            String response = port.newOperation("hello");
            assertNotNull(response);
            assertEquals("in=hello", response);

            response = port.newOperation(null);
            assertNotNull(response);
            assertEquals("in=null", response);

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }
    
    @Test
    public void testOrderedParamHolder() throws Exception {
        OrderedParamHolder_Service service = new OrderedParamHolder_Service();
        OrderedParamHolder port = service.getOrderedParamHolderSOAP();
        
        try {
            Holder<ComplexStruct> part3 = new Holder<ComplexStruct>();
            part3.value = new ComplexStruct();
            part3.value.setElem1("elem1");
            part3.value.setElem2("elem2");
            part3.value.setElem3(0);
            Holder<Integer> part2 = new Holder<Integer>();
            part2.value = 0;
            Holder<String> part1 = new Holder<String>();
            part1.value = "part1";
            
            port.orderedParamHolder(part3, part2, part1);
            
            assertNotNull(part3.value);
            assertEquals("check value", "return elem1", part3.value.getElem1());
            assertEquals("check value", "return elem2", part3.value.getElem2());
            assertEquals("check value", 1, part3.value.getElem3());
            assertNotNull(part2.value);
            assertEquals("check value", 1, part2.value.intValue());
            assertNotNull(part1.value);
            assertEquals("check value", "return part1", part1.value);
            
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }
    
    @Test
    public void testStringListOut() throws Exception {
        QName portName = new QName("http://cxf.apache.org/systest/jaxws/DocLitWrappedCodeFirstService", 
                                   "DocLitWrappedCodeFirstServicePort");
        QName servName = new QName("http://cxf.apache.org/systest/jaxws/DocLitWrappedCodeFirstService", 
                                   "DocLitWrappedCodeFirstService");
        
        Service service = Service.create(new URL(ServerMisc.DOCLIT_CODEFIRST_URL + "?wsdl"),
                                      servName);
        DocLitWrappedCodeFirstService port = service.getPort(portName,
                                                             DocLitWrappedCodeFirstService.class);
        List<String> rev = new ArrayList<String>(Arrays.asList(DocLitWrappedCodeFirstServiceImpl.DATA));
        Collections.reverse(rev);
        
        
        String s;
        
        String arrayOut[] = port.arrayOutput();
        assertNotNull(arrayOut);
        assertEquals(3, arrayOut.length);
        for (int x = 0; x < 3; x++) {
            assertEquals(DocLitWrappedCodeFirstServiceImpl.DATA[x], arrayOut[x]);
        }
        
        List<String> listOut = port.listOutput();
        assertNotNull(listOut);
        assertEquals(3, listOut.size());
        for (int x = 0; x < 3; x++) {
            assertEquals(DocLitWrappedCodeFirstServiceImpl.DATA[x], listOut.get(x));
        }
        
        s = port.arrayInput(DocLitWrappedCodeFirstServiceImpl.DATA);
        assertEquals("string1string2string3", s);
        
        s = port.listInput(java.util.Arrays.asList(DocLitWrappedCodeFirstServiceImpl.DATA));
        assertEquals("string1string2string3", s);
        
        s = port.multiListInput(Arrays.asList(DocLitWrappedCodeFirstServiceImpl.DATA),
                                rev,
                                "Hello", 24);
        assertEquals("string1string2string3string3string2string1Hello24", s);
        
        s = port.listInput(new ArrayList<String>());
        assertEquals("", s);

        s = port.listInput(null);
        assertEquals("", s);

        s = port.multiListInput(Arrays.asList(DocLitWrappedCodeFirstServiceImpl.DATA),
                                        rev,
                                        null, 24);
        assertEquals("string1string2string3string3string2string1<null>24", s);
        
    }
}
