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

package org.apache.cxf.tools.common.model;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.junit.Assert;
import org.junit.Test;

public class JAnnotationTest extends Assert {
    @Test
    public void testSimpleForm() {
        JAnnotation annotation = new JAnnotation(WebService.class);
        assertEquals("@WebService", annotation.toString());
    }

    @Test
    public void testStringForm() {
        JAnnotation annotation = new JAnnotation(WebService.class);
        annotation.addElement(new JAnnotationElement("name", "AddNumbersPortType"));
        annotation.addElement(new JAnnotationElement("targetNamespace", "http://example.com/"));
        assertEquals("@WebService(name = \"AddNumbersPortType\", targetNamespace = \"http://example.com/\")", 
                     annotation.toString());
    }

    @Test
    public void testEnum() {
        JAnnotation annotation = new JAnnotation(SOAPBinding.class);
        annotation.addElement(new JAnnotationElement("parameterStyle", 
                                                            SOAPBinding.ParameterStyle.BARE));
        assertEquals("@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)", annotation.toString());
    }
    

    @Test
    public void testPrimitive() {
        JAnnotation annotation = new JAnnotation(WebParam.class);
        annotation.addElement(new JAnnotationElement("header", true, true));
        annotation.addElement(new JAnnotationElement("mode", Mode.INOUT));
        assertEquals("@WebParam(header = true, mode = WebParam.Mode.INOUT)", annotation.toString());
    }

    @Test
    public void testAddSame() {
        JAnnotation annotation = new JAnnotation(WebParam.class);
        annotation.addElement(new JAnnotationElement("header", true, true));
        annotation.addElement(new JAnnotationElement("header", false, true));
        annotation.addElement(new JAnnotationElement("mode", Mode.INOUT));
        annotation.addElement(new JAnnotationElement("mode", Mode.OUT));
        assertEquals("@WebParam(header = false, mode = WebParam.Mode.OUT)", annotation.toString());
    }
}
