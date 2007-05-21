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

package org.apache.cxf.tools.validator.internal;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.validator.internal.model.XNode;
import org.junit.Test;

public class WSDLRefValidatorTest extends TestCase {

    @Test
    public void testNoService() throws Exception {
        String wsdl = getClass().getResource("resources/b.wsdl").toURI().toString();
        WSDLRefValidator validator = new WSDLRefValidator(wsdl);
        assertFalse(validator.isValid());
        ValidationResult results = validator.getValidationResults();
        assertEquals(1, results.getErrors().size());
        String t = results.getErrors().pop();
        assertEquals("WSDL document does not define any services", t);
    }

    @Test
    public void testWSDLImport1() throws Exception {
        String wsdl = getClass().getResource("resources/a.wsdl").toURI().toString();
        WSDLRefValidator validator = new WSDLRefValidator(wsdl);
        validator.isValid();
        ValidationResult results = validator.getValidationResults();
        assertEquals(2, results.getErrors().size());
        String t = results.getErrors().pop();
        String text = "{http://apache.org/hello_world/messages}[portType:GreeterA][operation:sayHi]";
        Message msg = new Message("FAILED_AT_POINT",
                                  WSDLRefValidator.LOG,
                                  8,
                                  2,
                                  new File(new java.net.URI(wsdl)).toString(),
                                  text);
        assertEquals(msg.toString(), t);
    }
    

    @Test
    public void testWSDLImport2() throws Exception {
        String wsdl = getClass().getResource("resources/physicalpt.wsdl").toURI().toString();
        WSDLRefValidator validator = new WSDLRefValidator(wsdl);
        assertTrue(validator.isValid());
        String expected = "/wsdl:definitions[@targetNamespace='http://schemas.apache.org/yoko/idl/OptionsPT']"
            + "/wsdl:portType[@name='foo.bar']";

        Set<String> xpath = new HashSet<String>();

        for (XNode node : validator.vNodes) {
            xpath.add(node.toString());
        }
        assertTrue(xpath.contains(expected));
    }

    @Test
    public void testNoTeypRef() throws Exception {
        String wsdl = getClass().getResource("resources/NoTypeRef.wsdl").toURI().toString();
        WSDLRefValidator validator = new WSDLRefValidator(wsdl);
        assertFalse(validator.isValid());
        assertEquals(3, validator.getValidationResults().getErrors().size());

        String t = validator.getValidationResults().getErrors().pop();

        String expected = "Part <header_info> in Message "
            + "<{http://apache.org/samples/headers}inHeaderRequest>"
            + " referenced Type <{http://apache.org/samples/headers}SOAPHeaderInfo> "
            + "can not be found in the schemas";
        assertEquals(expected, t);
    }
}
