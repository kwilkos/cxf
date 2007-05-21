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
package org.apache.cxf.tools.validator;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.apache.cxf.tools.common.ToolTestBase;
import org.junit.Before;
import org.junit.Test;

public class WSDLValidationTest extends ToolTestBase {
    @Before    
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testValidateUniqueBody() {
        try {

            String[] args = new String[] {"-verbose", getLocation("/validator_wsdl/doc_lit_bare.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Non Unique Body Parts Error should be discovered: " + getStdErr(),
                       getStdErr().indexOf("Non unique body part") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateMixedStyle() {
        try {

            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_mixed_style.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Mixed style. Error should have been discovered: " + getStdErr(),
                       getStdErr().indexOf("Mixed style, invalid WSDL") > -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateTypeElement() {
        try {

            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_doc_lit_type.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Must refer to type element error should have been discovered: " + getStdErr(),
                       getStdErr().indexOf("using the element attribute") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateAttribute() {
        try {

            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_error_attribute.wsdl")};
            WSDLValidator.main(args);
            String expected = "WSDLException (at /wsdl:definitions/wsdl:message[1]/wsdl:part): "
                + "faultCode=INVALID_WSDL: Encountered illegal extension attribute 'test'. "
                + "Extension attributes must be in a namespace other than WSDL's";
            assertTrue("Attribute error should be discovered: " + getStdErr(),
                       getStdErr().indexOf(expected) > -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateReferenceError() throws Exception {

        try {

            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_error_reference.wsdl")};
            WSDLValidator.main(args);
            assertTrue(getStdErr().indexOf("[147,3]") != -1);
            assertTrue(getStdErr().indexOf("Caused by {http://apache.org/hello_world_soap_http}"
                                           + "[binding:Greeter_SOAPBinding1] not exist.") != -1);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    @Test
    public void testBug305872() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/bug305872/http.xsd")};
            WSDLValidator.main(args);
            String expected = "Expected element '{http://schemas.xmlsoap.org/wsdl/}definitions'.";
            assertTrue("Tools should check if this file is a wsdl file: " + getStdErr(),
                       getStdErr().indexOf(expected) > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testImportWsdlValidation() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_import.wsdl")};
            WSDLValidator.main(args);
            
            assertTrue("Is not valid wsdl!: " + getStdOut(),
                       getStdOut().indexOf("Passed Validation") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testImportSchemaValidation() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/hello_world_schema_import.wsdl")};
            WSDLValidator.main(args);
            
            assertTrue("Is not valid wsdl: " + getStdOut(),
                       getStdOut().indexOf("Passed Validation") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testWSIBP2210() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/soapheader.wsdl")};
            WSDLValidator.main(args);
            assertTrue(getStdErr().indexOf("WSI-BP-1.0 R2210") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWSIBPR2726() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/jms_test.wsdl")};
            WSDLValidator.main(args);
            assertTrue(getStdErr().indexOf("WSI-BP-1.0 R2726") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWSIBPR2205() throws Exception {
        try {
            String[] args = new String[] {"-verbose",
                                          getLocation("/validator_wsdl/jms_test2.wsdl")};
            WSDLValidator.main(args);
            assertTrue(getStdErr().indexOf("WSI-BP-1.0 R2205") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getLocation(String wsdlFile) throws Exception {
        Enumeration<URL> e = WSDLValidationTest.class.getClassLoader().getResources(wsdlFile);
        while (e.hasMoreElements()) {
            URL u = e.nextElement();
            File f = new File(u.toURI());
            if (f.exists() && f.isDirectory()) {
                return f.toString();
            }
        }

        return WSDLValidationTest.class.getResource(wsdlFile).toURI().getPath();
    }
}
